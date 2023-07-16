package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.chat.databinding.ActivityContactsBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ContactsActivity extends AppCompatActivity implements OnItemClickListener{
    private ActivityContactsBinding binding;
    private AppDB db;
    private RecyclerView lvChats;
    private List<User> contacts;
    private ArrayList<Chat> chatList;
    private ContactAdapter adapter;
    private UserDao userDao;
    private User logged;
    private String token;
    private String chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = Room.databaseBuilder(getApplicationContext(), AppDB.class, "users")
                .allowMainThreadQueries().build();

        userDao = db.UserDao();
        logged = userDao.get(getIntent().getExtras().getString("username"));
        handlePosts();
        binding.btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(this, FormActivity.class);
            intent.putExtra("username", logged.getUsername());
            startActivity(intent);
        });
        binding.btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatsFromServer(logged);

    }


    private void handlePosts() {
        lvChats = binding.lvChats;
        contacts = new ArrayList<>();
        adapter = new ContactAdapter(contacts);
        adapter.setOnItemClickListener(this);
        loadChats(logged);
        loadChatsFromServer(logged);
        lvChats.setAdapter(adapter);

    }
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, ChatActivity.class);
        String[] arr = new String[]{logged.getUsername(), chatList.get(position).getIdStr()};
        intent.putExtra("chatInfo", arr);
        startActivity(intent);
    }

    private void loadChats(User logged) {
        contacts.clear();
        chatList = userDao.get(logged.getUsername()).getChats();
        for (Chat chat : chatList) {
            contacts.add(chat.getUser());
        }

        adapter.notifyDataSetChanged();
    }
    private void loadChatsFromServer(User logged) {
        getChatsFormServer(logged.getUsername(), logged.getPassword());
        contacts.clear();
        for (Chat chat : chatList) {
            contacts.add(chat.getUser());
        }
        adapter.notifyDataSetChanged();
    }

    private void getChatsFormServer(String username, String password) {
        Thread thread = new Thread(new Runnable() {
            private StringBuilder responseBody; // Variable to hold the response body

            public StringBuilder getResponseBody() {
                return responseBody;
            }

            @Override
            public void run() {
                getToken(username, password);
                try {
                    URL url = new URL(Settings.getServer() + "/api/Chats/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        StringBuilder responseBody;
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            StringBuilder response = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            chats = response.toString();
                            Gson gson = new Gson();

                            Type arrayListType = new TypeToken<ArrayList<Chat>>() {}.getType();

                            chatList = gson.fromJson(chats, arrayListType);

                            logged.setChats(chatList);
                            userDao.update(logged);
                        }
                    } else {
                        // Handle the error case
                        System.out.println("HTTP GET request failed with response code: " + responseCode);
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(e.toString());
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

