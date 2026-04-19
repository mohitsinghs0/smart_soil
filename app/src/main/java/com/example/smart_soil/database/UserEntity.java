package com.example.smart_soil.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String server_id;  // ID from Supabase (UUID)
    public String name;
    public String email;
    public String mobile;
    public String gender;
    public String password;
    public String token;     // Access Token from Supabase
    public String refresh_token; // Refresh Token
    public long created_at;
    public long last_login;

    public UserEntity() {}

    @Ignore
    public UserEntity(String name, String email, String mobile, String gender, String password) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
        this.password = password;
        this.created_at = System.currentTimeMillis();
    }
}
