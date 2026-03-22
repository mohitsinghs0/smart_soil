package com.example.smart_soil.requests;

public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String mobile;
    public String gender;

    public RegisterRequest(String name, String email, String password, String mobile, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.gender = gender;
    }
}
