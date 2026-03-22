package com.example.smart_soil.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract UserDao userDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smart_soil_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
