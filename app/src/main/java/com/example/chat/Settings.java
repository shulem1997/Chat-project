package com.example.chat;

public class Settings {
    private static String server;
    private static String theme;
    public static String getServer() {
        return server;
    }

    public static void setTheme(String theme) {
        Settings.theme = theme;
    }
    public static String getTheme(){
        return theme;
    }

    public static void setServer(String value) {
        server = value;
    }
}
