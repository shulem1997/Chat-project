package com.example.chat;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<Chat> fromStringChat(String value) {
        Type listType = new TypeToken<ArrayList<Chat>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayChats(ArrayList<Chat> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
    @TypeConverter
    public static ArrayList<Message> fromStringMsg(String value) {
        Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayMsg(ArrayList<Message> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}

