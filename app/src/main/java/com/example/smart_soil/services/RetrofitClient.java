package com.example.smart_soil.services;

import android.os.Build;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RetrofitClient {
    
    // Default IP - This should match your computer's current local IP
    private static String ACTUAL_COMPUTER_IP = "192.168.0.106";
    private static final String EMULATOR_IP = "10.0.2.2";
    private static final String PORT = "8080";

    private static String baseUrl = null;

    public static String getBaseUrl() {
        if (baseUrl != null) return baseUrl;

        // Enhanced emulator detection
        boolean isEmulator = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");

        String ip = isEmulator ? EMULATOR_IP : ACTUAL_COMPUTER_IP;
        baseUrl = "http://" + ip + ":" + PORT + "/";
        Timber.d("Using Base URL: %s (Is Emulator: %b)", baseUrl, isEmulator);
        return baseUrl;
    }

    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
            
            Gson gson = new GsonBuilder()
                .setLenient()
                .create();
            
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
        baseUrl = newUrl.endsWith("/") ? newUrl : newUrl + "/";
        retrofit = null; // Force recreation of Retrofit instance
        Timber.i("Base URL manually updated to: %s", baseUrl);
    }

    /**
     * Helper to update just the IP while keeping the default port
     */
    public static void updateIp(String ip) {
        setBaseUrl("http://" + ip + ":" + PORT + "/");
    }
}
