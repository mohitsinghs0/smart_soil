package com.example.smart_soil.services;

import android.content.Context;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RetrofitClient {

    private static volatile Retrofit retrofit = null;
    private static volatile Retrofit weatherRetrofit = null;
    private static ApiService apiService = null;
    private static AuthApiService authApiService = null;
    private static WeatherApiService weatherApiService = null;

    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/";

    private RetrofitClient() {
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    SharedPrefsManager prefsManager = new SharedPrefsManager(context);

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .addInterceptor(chain -> {
                                Request original = chain.request();
                                Request.Builder builder = original.newBuilder()
                                        .header("apikey", SupabaseConfig.ANON_KEY)
                                        .header("Prefer", "return=representation");
                                
                                String token = prefsManager.getToken();
                                if (token != null && !token.isEmpty()) {
                                    builder.header("Authorization", "Bearer " + token);
                                } else {
                                    builder.header("Authorization", "Bearer " + SupabaseConfig.ANON_KEY);
                                }
                                return chain.proceed(builder.build());
                            })
                            .authenticator((route, response) -> {
                                if (response.code() == 401) {
                                    String refreshToken = prefsManager.getRefreshToken();
                                    if (refreshToken != null) {
                                        AuthResponse newAuth = refreshAccessTokenSync(refreshToken);
                                        if (newAuth != null) {
                                            prefsManager.saveToken(newAuth.accessToken);
                                            prefsManager.saveRefreshToken(newAuth.refreshToken);
                                            return response.request().newBuilder()
                                                    .header("Authorization", "Bearer " + newAuth.accessToken)
                                                    .build();
                                        } else {
                                            prefsManager.clearAll();
                                        }
                                    }
                                }
                                return null;
                            })
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build();

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(SupabaseConfig.BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static Retrofit getWeatherClient() {
        if (weatherRetrofit == null) {
            synchronized (RetrofitClient.class) {
                if (weatherRetrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build();

                    weatherRetrofit = new Retrofit.Builder()
                            .baseUrl(WEATHER_BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return weatherRetrofit;
    }

    private static AuthResponse refreshAccessTokenSync(String refreshToken) {
        OkHttpClient client = new OkHttpClient();
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("refresh_token", refreshToken);
        
        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SupabaseConfig.BASE_URL + "auth/v1/token?grant_type=refresh_token")
                .post(body)
                .header("apikey", SupabaseConfig.ANON_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return new Gson().fromJson(response.body().string(), AuthResponse.class);
            }
        } catch (IOException e) {
            Timber.e(e, "Token refresh failed");
        }
        return null;
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = getClient(context).create(ApiService.class);
        }
        return apiService;
    }

    public static AuthApiService getAuthApiService(Context context) {
        if (authApiService == null) {
            authApiService = getClient(context).create(AuthApiService.class);
        }
        return authApiService;
    }

    public static WeatherApiService getWeatherApiService() {
        if (weatherApiService == null) {
            weatherApiService = getWeatherClient().create(WeatherApiService.class);
        }
        return weatherApiService;
    }

    public static String getBaseUrl() {
        return SupabaseConfig.BASE_URL;
    }
}
