package com.example.smart_soil.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 10.0.2.2 is the special IP to access your computer's localhost from the Android Emulator.
    // If using a physical device, replace 10.0.2.2 with your computer's local IP address.
    private static final String BASE_URL = "http://10.0.2.2:8080/"; 
    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
            
            Gson gson = new GsonBuilder()
                .setLenient()
                .create();
            
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    public static void setBaseUrl(String baseUrl) {
        retrofit = null;
        final String newBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build();
        
        Gson gson = new GsonBuilder()
            .setLenient()
            .create();
        
        retrofit = new Retrofit.Builder()
            .baseUrl(newBase)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    }
}
