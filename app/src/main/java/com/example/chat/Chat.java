package com.example.chat;



public class Chat {
    private int id;
    private User user;
    private Message lastMessage;

    public Chat(User user) {
        //this.id = id;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getIdStr() {
        return String.valueOf(id);
    }
}


