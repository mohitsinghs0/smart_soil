package com.example.smart_soil.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    
    @Insert
    long insertUser(UserEntity user);
    
    @Update
    void updateUser(UserEntity user);
    
    @Delete
    void deleteUser(UserEntity user);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE server_id = :userId LIMIT 1")
    UserEntity getUserByServerId(int userId);
    
    @Query("SELECT * FROM users LIMIT 1")
    UserEntity getCurrentUser();
    
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
    
    @Query("UPDATE users SET last_login = :timestamp WHERE email = :email")
    void updateLastLogin(String email, long timestamp);
}
