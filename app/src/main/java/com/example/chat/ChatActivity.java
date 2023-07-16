package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.chat.databinding.ActivityChatBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    
    private User logged;
    private AppDB db;
    private UserDao userDao;
    private RecyclerView msgs;
    private Chat chat;
    private ArrayList<Message> msgList;
    private ArrayList<User> contact;
    private MessageAdapter adapter;
    private ContactAdapter contactAdapter;
    private int chatId;
    private Socket mSocket;
    {
        try {
            String server=Settings.getServer();
            if (server==null)
                server="http://10.0.2.2:5000";
            ;          mSocket = IO.socket(server);
        } catch (URISyntaxException e) {}
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    loadMessages();
                }
            });
        }
    };
    private ActivityChatBinding binding;
    private String token;
    private String messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSocket.connect();
        //TextView chatWith = binding.chatWith;



        db = Room.databaseBuilder(getApplicationContext(), AppDB.class, "users")
                .allowMainThreadQueries().build();
        String[] arr = getIntent().getExtras().getStringArray("chatInfo");
        String username = arr[0];
        chatId = Integer.parseInt(arr[1]);
        userDao = db.UserDao();
        logged = userDao.get(username);
        chat = getChat();

        contact = new ArrayList<>();
        contact.add(chat.getUser());
        contactAdapter = new ContactAdapter(contact);
        binding.chatWith.setAdapter(contactAdapter);

        handleMessages();
        //getMessagesFromServer(logged.getUsername(), logged.getPassword());

        binding.btnBack.setOnClickListener(View-> {
            Intent intent = new Intent(this, ContactsActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }

    private void handleMessages() {
        msgs = binding.messages;

        msgList = setMsgsArray();
        adapter = new MessageAdapter(msgList, logged.getUsername());

        msgs.setAdapter(adapter);

        binding.btnSend.setOnClickListener(view-> {
            sendMsg();
            adapter.notifyDataSetChanged();
            System.out.println("ko");
        });
    }


    private void sendMsg() {
        //server request here

        mSocket.emit("new message", binding.etInput.getText().toString());
        AddMessagesToServer(logged.getUsername(), logged.getPassword(), binding.etInput.getText().toString());
        binding.etInput.setText("");
        getMessagesFromServer(logged.getUsername(), logged.getPassword());

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }
    private void loadMessages() {

        // Assuming you have a list of messages called "messageList"

        getMessagesFromServer(logged.getUsername(),logged.getPassword());

        adapter.notifyDataSetChanged();
    }
    private ArrayList<Message> setMsgsArray() {
        ArrayList<Message> chatmsg = logged.getMessages();
        ArrayList<Message> list = new ArrayList<>();
        for(Message m: chatmsg) {
            if(m.getChatId() == chatId) {
                m.setIsSender(logged.getUsername());
                list.add(m);
            }
        }
        return list;
    }

    private Chat getChat() {
        ArrayList<Chat> cht = logged.getChats();
        for (Chat c: cht) {
            if(c.getId() == chatId)
                return c;
        }
        return null;
    }


    private void getMessagesFromServer(String username, String password){
        getToken(username, password);
        AtomicInteger responseCode = new AtomicInteger();
        final StringBuilder[] responseBody = {new StringBuilder()};
        Thread thread = new Thread(new Runnable() {
            // Variable to hold the response body


            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:5000/api/Chats/" + chatId + "/Messages"); // Replace with your API endpoint

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "bearer " + token);
                    responseCode.set(connection.getResponseCode());




                    StringBuilder res;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;

                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        res = new StringBuilder(response.toString()); // Assign the response to the variable
                    }
                    responseBody[0] = res;
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });

// Start the thread
        thread.start();

        try {
            // Wait for the thread to finish
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(responseCode.get()==200) {
            Gson gson = new Gson();
            ArrayList<Message> msg = gson.fromJson(responseBody[0].toString(), new TypeToken<ArrayList<Message>>() {}.getType());
            msgList.clear();
            for(Message m: msg) {
                m.setChatId(chatId);
                msgList.add(m);
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong Messages", Toast.LENGTH_LONG).show();
        }
    }
    private void AddMessagesToServer(String username, String password, String message){
        getToken(username, password);
        Thread thread = new Thread(new Runnable() {
            private StringBuilder responseBody; // Variable to hold the response body

            public StringBuilder getResponseBody() {
                return responseBody;
            }

            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:5000/api/Chats/"+chatId+"/Messages"); // Replace with your API endpoint

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                    connection.setDoOutput(true);

                    String requestBody = "{\"msg\": \"" + message + "\"}";

                    try {
                        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                        outputStream.writeBytes(requestBody);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    StringBuilder responseBody;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;

                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        responseBody = new StringBuilder(response.toString()); // Assign the response to the variable
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });

// Start the thread
        thread.start();

        try {
            // Wait for the thread to finish
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getToken(String username, String password) {
        AtomicInteger responseCode = new AtomicInteger();
        Thread thread = new Thread(new Runnable() {
            private StringBuilder responseBody; // Variable to hold the response body

            public StringBuilder getResponseBody() {
                return responseBody;
            }

            @Override
            public void run() {
                try {
                    URL url = new URL(Settings.getServer() + "/api/Tokens/"); // Replace with your API endpoint

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    String requestBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

                    try {
                        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                        outputStream.writeBytes(requestBody);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    responseCode.set(connection.getResponseCode());

                    StringBuilder responseBody;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;

                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        responseBody = new StringBuilder(response.toString()); // Assign the response to the variable
                    }
                    token = responseBody.toString();

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });

// Start the thread
        thread.start();

        try {
            // Wait for the thread to finish
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (responseCode.get() == 200) {

        } else {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
    }
}