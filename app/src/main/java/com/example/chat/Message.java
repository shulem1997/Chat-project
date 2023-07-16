package com.example.chat;

import android.os.Build;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class Message {
    private int id;

    private int chatId;
    private UserChatJson sender;
    private String created;
    private String content;

    private boolean isSender;

    public Message(String content, UserChatJson sender) {
        this.sender = sender;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.created = LocalDateTime.now().toString();
        }
        this.content = content;
    }
    public String getContent() {
        return this.content;
    }

    public int getId() {
        return this.id;
    }

    public UserChatJson getSender() {
        return this.sender;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
    public int getChatId() {
        return this.chatId;
    }

    public void setCreated(String created) {
        this.created = created;
    }
    public String getCreated(){
        return this.created;
    }

    public void setIsSender(String logged) {
        if(sender.getUsername().equals(logged))
            this.isSender=true;
        else
            this.isSender = false;
    }
    public boolean getIsSender() {
        return this.isSender;
    }

    public String getSenderName() {
        return this.sender.getUsername();
    }


}

