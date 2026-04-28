package com.example.smart_soil.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserEntity.class, FarmEntity.class, SoilTestEntity.class}, version = 3, exportSchema = false)
public abstract class SmartSoilDatabase extends RoomDatabase {

    private static volatile SmartSoilDatabase instance;

    public abstract UserDao userDao();
    public abstract FarmDao farmDao();
    public abstract SoilTestDao soilTestDao();

    public static SmartSoilDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (SmartSoilDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            SmartSoilDatabase.class, "smart_soil_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
