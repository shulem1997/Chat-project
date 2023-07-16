package com.example.chat;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {User.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDB extends RoomDatabase {
    public abstract UserDao UserDao();
}
