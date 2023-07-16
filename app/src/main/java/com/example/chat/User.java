package com.example.chat;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity
public class User {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String username;
    private String password;
    private String displayName;
    private String profilePic;
    private ArrayList<Chat> chats;
    private ArrayList<Message> messages;

    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.chats = new ArrayList<Chat>();
        this.messages = new ArrayList<Message>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public void addMessage(Message msg) {
        this.messages.add(msg);
    }
    public void addChat(Chat chat) {
        this.chats.add(chat);
    }
    public ArrayList<Chat> getChats() {
        return this.chats;
    }
    public ArrayList<Message> getMessages() {
        return this.messages;
    }
    public void setMessages(ArrayList<Message> msg) {
        this.messages = new ArrayList<>(msg);
    }
    public void setChats(ArrayList<Chat> cht) {
        this.chats = new ArrayList<>(cht);
    }

    public String getProfilePic() {return this.profilePic;}
    public void setProfilePic(String pic) {
        this.profilePic = pic;
    }

}

