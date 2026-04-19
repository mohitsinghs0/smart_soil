package com.example.smart_soil.repository;

import android.content.Context;

import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.database.UserEntity;
import com.example.smart_soil.database.UserDao;
import com.example.smart_soil.requests.RegisterRequest;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.services.AuthApiService;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UserRepository {
    
    private final UserDao userDao;
    private final SharedPrefsManager prefsManager;
    private final Context context;
    
    public UserRepository(Context context, SharedPrefsManager prefsManager) {
        this.context = context;
        SmartSoilDatabase db = SmartSoilDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.prefsManager = prefsManager;
    }

    private AuthApiService getAuthApiService() {
        return RetrofitClient.getAuthApiService(context);
    }
    
    public void registerUser(RegisterRequest request, RegistrationCallback callback) {
        getAuthApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    new Thread(() -> {
                        UserEntity userEntity = new UserEntity(
                            request.name, request.email, request.mobile,
                            request.gender, request.password
                        );
                        userEntity.server_id = authResponse.user.id;
                        userEntity.token = authResponse.accessToken;
                        userEntity.refresh_token = authResponse.refreshToken;
                        userDao.insertUser(userEntity);
                        
                        prefsManager.saveToken(authResponse.accessToken);
                        prefsManager.saveRefreshToken(authResponse.refreshToken);
                        prefsManager.saveUserId(authResponse.user.id);
                        prefsManager.saveUserName(request.name);
                        prefsManager.saveUserEmail(request.email);
                        
                        callback.onSuccess(authResponse);
                    }).start();
                } else {
                    String errorMsg = "Registration failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            JsonObject errorObj = new Gson().fromJson(errorBodyString, JsonObject.class);
                            if (errorObj.has("msg")) {
                                errorMsg = errorObj.get("msg").getAsString();
                            } else if (errorObj.has("message")) {
                                errorMsg = errorObj.get("message").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e, "Error parsing error body");
                    }
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                handleFailure(t, callback::onError);
            }
        });
    }
    
    public void loginUser(LoginRequest request, LoginCallback callback) {
        getAuthApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    new Thread(() -> {
                        UserEntity existingUser = userDao.getUserByEmail(request.email);
                        if (existingUser != null) {
                            existingUser.token = authResponse.accessToken;
                            existingUser.refresh_token = authResponse.refreshToken;
                            existingUser.server_id = authResponse.user.id;
                            existingUser.last_login = System.currentTimeMillis();
                            userDao.updateUser(existingUser);
                        } else {
                            UserEntity userEntity = new UserEntity(
                                "User", request.email, "", "", request.password
                            );
                            userEntity.server_id = authResponse.user.id;
                            userEntity.token = authResponse.accessToken;
                            userEntity.refresh_token = authResponse.refreshToken;
                            userEntity.last_login = System.currentTimeMillis();
                            userDao.insertUser(userEntity);
                        }
                        
                        prefsManager.saveToken(authResponse.accessToken);
                        prefsManager.saveRefreshToken(authResponse.refreshToken);
                        prefsManager.saveUserId(authResponse.user.id);
                        prefsManager.saveUserEmail(request.email);
                        
                        callback.onSuccess(authResponse);
                    }).start();
                } else {
                    String errorMsg = "Login failed: Incorrect email or password.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            JsonObject errorObj = new Gson().fromJson(errorBodyString, JsonObject.class);
                            if (errorObj.has("msg")) {
                                errorMsg = errorObj.get("msg").getAsString();
                            } else if (errorObj.has("error_description")) {
                                errorMsg = errorObj.get("error_description").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        Timber.e(e, "Error parsing error body");
                    }
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                handleFailure(t, callback::onError);
            }
        });
    }

    private void handleFailure(Throwable t, ErrorListener listener) {
        Timber.e(t, "API Call Failed");
        if (t instanceof ConnectException) {
            listener.onError("Cannot connect to Supabase. Check your internet connection.");
        } else if (t instanceof SocketTimeoutException) {
            listener.onError("Connection timed out. Please try again.");
        } else {
            listener.onError("Network error: " + t.getLocalizedMessage());
        }
    }

    public UserEntity getCurrentUser() {
        return userDao.getCurrentUser();
    }
    
    public void clearAllUsers() {
        new Thread(() -> {
            userDao.deleteAllUsers();
            prefsManager.clearAll();
        }).start();
    }
    
    public interface RegistrationCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }
    
    public interface LoginCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    private interface ErrorListener {
        void onError(String message);
    }
}
