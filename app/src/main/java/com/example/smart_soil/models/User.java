package com.example.smart_soil.models;

import java.io.Serializable;

public class User implements Serializable {
    public int id;
    public String name;
    public String email;
    public String mobile;
    public String gender;
    public String token;
    public boolean isActive;
    public long created_at;

    public User() {}

    public User(String name, String email, String mobile, String gender) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
    }
}
