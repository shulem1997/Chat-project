package com.example.chat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.IO;
import io.socket.client.Socket;


public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private String token;
    private User logged;
    private UserDao userDao;
    private AppDB db;
    private Socket mSocket;
    {
        try {
            String server=Settings.getServer();
            if (server==null)
                server="http://10.0.2.2:5000";
 ;          mSocket = IO.socket(server);
        } catch (URISyntaxException e) {}
    }
    private ActivityResultLauncher<Intent> startActivityLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Task<String> s = FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this, instanceIdresult->{
            String refreshedToken = String.valueOf(FirebaseMessaging.getInstance().getToken());
            Log.d(TAG, "Refreshed token: " + refreshedToken);
        });
        Settings.setServer("http://10.0.2.2:5000");
        Settings.setTheme("light");
        mSocket.connect();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        FloatingActionButton settings = findViewById(R.id.settingsButton);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLogin();
            }
        });
//        settings.setOnClickListener(view-> {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//
//        });
      TextView register = findViewById(R.id.linkToRegister);
        register.setOnClickListener(view-> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);

        });
        startActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        View mainLayout = findViewById(R.id.main_layout);
                        if (Settings.getTheme().equals("dark")) {

                            int drawableResId = R.drawable.dark_background; // Replace with your own resource ID
                            Drawable backgroundDrawable = getDrawable(drawableResId);
                            mainLayout.setBackground(backgroundDrawable);
                        }
                        else{
                            int drawableResId = R.drawable.background; // Replace with your own resource ID
                            Drawable backgroundDrawable = getDrawable(drawableResId);
                            mainLayout.setBackground(backgroundDrawable);
                        }
                    }
                });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the settings activity
                Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                startActivityLauncher.launch(intent);
            }
        });




    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        View mainLayout = findViewById(R.id.main_layout);
//        if(Settings.getTheme()=="dark") {
//             int drawableResId = R.drawable.dark_background; // Replace with your own resource ID
//            Drawable backgroundDrawable = ContextCompat.getDrawable(this, drawableResId);
//            mainLayout.setBackground(backgroundDrawable);
//
//        }
//    }
    @Override
    protected void onResume() {
        super.onResume();
        View mainLayout = findViewById(R.id.main_layout);
        if(Settings.getTheme()=="dark") {
            int drawableResId = R.drawable.dark_background; // Replace with your own resource ID
            Drawable backgroundDrawable = ContextCompat.getDrawable(this, drawableResId);
            mainLayout.setBackground(backgroundDrawable);

        }
    }

    private void clickLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        AtomicInteger responseCode = new AtomicInteger();
        Thread thread = new Thread(new Runnable() {
            private StringBuilder responseBody; // Variable to hold the response body

            public StringBuilder getResponseBody() {
                return responseBody;
            }

            @Override
            public void run() {
                try {
                    URL url = new URL(Settings.getServer()+"/api/Tokens/"); // Replace with your API endpoint

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
                    token=responseBody.toString();

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

            Intent intent = new Intent(this, ContactsActivity.class);
            //String[] arr = new String[] {username, password, token};
            getLogged(username, password);
            getChats();
            getMessages();
            saveLogged(logged);
            intent.putExtra("username", username);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong username or password", Toast.LENGTH_LONG).show();
        }
    }


    private void getLogged(String username, String password) {
        AtomicInteger responseCode = new AtomicInteger();
        final StringBuilder[] responseBody = {new StringBuilder()};
        Thread thread = new Thread(new Runnable() {
             // Variable to hold the response body


            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:5000/api/Users/" + username); // Replace with your API endpoint

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
            UserJson userJson = gson.fromJson(responseBody[0].toString(), UserJson.class);
            logged = new User(username, password, userJson.getDisplayName());
            logged.setProfilePic(userJson.getProfilePic());

        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong getLogged", Toast.LENGTH_LONG).show();
        }
    }

    private void getChats() {
        AtomicInteger responseCode = new AtomicInteger();
        final StringBuilder[] responseBody = {new StringBuilder()};
        Thread thread = new Thread(new Runnable() {
            // Variable to hold the response body


            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:5000/api/Chats");

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

            Type arrayListType = new TypeToken<ArrayList<Chat>>() {}.getType();

            ArrayList<Chat> chats = gson.fromJson(responseBody[0].toString(), arrayListType);

            logged.setChats(chats);

        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong getChats", Toast.LENGTH_LONG).show();
        }
    }

    private void getMessages() {
        ArrayList<Message> msgs = new ArrayList<>();
        ArrayList<Chat> chats = logged.getChats();
        for(Chat cht: chats) {
            getMessagesPerChat(msgs, cht.getId());
        }
        logged.setMessages(msgs);
    }

    private void getMessagesPerChat(ArrayList<Message> msgs, int chat) {
        AtomicInteger responseCode = new AtomicInteger();
        final StringBuilder[] responseBody = {new StringBuilder()};
        Thread thread = new Thread(new Runnable() {
            // Variable to hold the response body


            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:5000/api/Chats/" + chat + "/Messages"); // Replace with your API endpoint

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
            ArrayList<Message> msg = gson.fromJson(responseBody[0].toString(), new TypeToken<ArrayList<Message>>() {}.getType());;
            for(Message m: msg) {
                m.setChatId(chat);
            }
            msgs.addAll(msg);

        }
        else{
            Toast.makeText(getApplicationContext(), "Wrong Messages", Toast.LENGTH_LONG).show();
        }
    }

    private void saveLogged(User user) {
        db = Room.databaseBuilder(getApplicationContext(), AppDB.class, "users")
                .allowMainThreadQueries().build();
        userDao = db.UserDao();
        userDao.deleteAll();
        userDao.insert(logged);
    }
}
