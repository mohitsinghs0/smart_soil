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
    
    /**
     * 🚀 TODO: REPLACE THIS URL WITH THE ONE FROM YOUR RAILWAY DASHBOARD
     * Go to Railway -> Backend Service -> Settings -> Public Networking -> Copy Public URL
     */
    private static final String RAILWAY_URL = "https://smart-soil-backend-production.up.railway.app/";
    
    // Local development fallbacks
    private static final String EMULATOR_IP = "10.0.2.2";
    private static final String DEFAULT_COMPUTER_IP = "192.168.0.106";
    private static final String PORT = "8080";

    private static String baseUrl = null;

    public static String getBaseUrl() {
        if (baseUrl != null) return baseUrl;

        // Force Production URL for testing
        // Change to 'false' if you want to use your local computer server
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
        
        // Ensure URL ends with /
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        Timber.d("Using Base URL: %s", baseUrl);
        return baseUrl;
    }

    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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
        retrofit = null;
        Timber.i("Base URL manually updated to: %s", baseUrl);
    }
}
