package com.example.smart_soil.services;

import android.os.Build;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RetrofitClient {
    
    /**
     * ✅ UPDATED: The domain from your Railway screenshot
     * It must be the Public Domain for the BACKEND SERVICE, not the MySQL one.
     */
    private static final String RAILWAY_URL = "https://mysql-production-e753.up.railway.app/";
    
    private static final String EMULATOR_IP = "10.0.2.2";
    private static final String DEFAULT_COMPUTER_IP = "192.168.0.106";
    private static final String PORT = "8080";

    private static String baseUrl = null;

    public static String getBaseUrl() {
        if (baseUrl != null) return baseUrl;

        // Set to true to use Railway
        boolean isProduction = true;

        if (isProduction) {
            baseUrl = RAILWAY_URL;
        } else {
            boolean isEmulator = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator");

            String ip = isEmulator ? EMULATOR_IP : DEFAULT_COMPUTER_IP;
            baseUrl = "http://" + ip + ":" + PORT + "/";
        }
        
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        return baseUrl;
    }

    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Connection", "close")
                            .addHeader("Accept", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
            
            Gson gson = new GsonBuilder().setLenient().create();
            
            retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    public static void setBaseUrl(String newUrl) {
        if (!newUrl.startsWith("http")) newUrl = "https://" + newUrl;
        baseUrl = newUrl.endsWith("/") ? newUrl : newUrl + "/";
        retrofit = null;
        Timber.i("Base URL manually updated to: %s", baseUrl);
    }
}
