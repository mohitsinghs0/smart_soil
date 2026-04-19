package com.example.smart_soil.services;

import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest body);
}
