package com.example.smart_soil.repository;

import android.content.Context;

import com.example.smart_soil.database.AppDatabase;
import com.example.smart_soil.database.UserEntity;
import com.example.smart_soil.database.UserDao;
import com.example.smart_soil.requests.RegisterRequest;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.services.ApiService;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.SharedPrefsManager;

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
    
    public UserRepository(Context context, SharedPrefsManager prefsManager) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.prefsManager = prefsManager;
    }

    private ApiService getApiService() {
        return RetrofitClient.getApiService();
    }
    
    public void registerUser(RegisterRequest request, RegistrationCallback callback) {
        getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success) {
                        new Thread(() -> {
                            UserEntity userEntity = new UserEntity(
                                authResponse.name, request.email, request.mobile,
                                request.gender, request.password
                            );
                            userEntity.server_id = authResponse.user_id;
                            userEntity.token = authResponse.token;
                            userDao.insertUser(userEntity);
                            
                            prefsManager.saveToken(authResponse.token);
                            prefsManager.saveUserId(authResponse.user_id);
                            prefsManager.saveUserName(authResponse.name);
                            prefsManager.saveUserEmail(request.email);
                            
                            callback.onSuccess(authResponse);
                        }).start();
                    } else {
                        callback.onError(authResponse.message);
                    }
                } else {
                    callback.onError("Registration failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                handleFailure(t, callback::onError);
            }
        });
    }
    
    public void loginUser(LoginRequest request, LoginCallback callback) {
        getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success) {
                        new Thread(() -> {
                            UserEntity existingUser = userDao.getUserByEmail(request.email);
                            if (existingUser != null) {
                                existingUser.token = authResponse.token;
                                existingUser.server_id = authResponse.user_id;
                                existingUser.name = authResponse.name;
                                existingUser.last_login = System.currentTimeMillis();
                                userDao.updateUser(existingUser);
                            } else {
                                UserEntity userEntity = new UserEntity(
                                    authResponse.name, request.email, "", "", request.password
                                );
                                userEntity.server_id = authResponse.user_id;
                                userEntity.token = authResponse.token;
                                userEntity.last_login = System.currentTimeMillis();
                                userDao.insertUser(userEntity);
                            }
                            
                            prefsManager.saveToken(authResponse.token);
                            prefsManager.saveUserId(authResponse.user_id);
                            prefsManager.saveUserName(authResponse.name);
                            prefsManager.saveUserEmail(request.email);
                            
                            callback.onSuccess(authResponse);
                        }).start();
                    } else {
                        callback.onError(authResponse.message);
                    }
                } else {
                    callback.onError("Login failed (Server Error)");
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
            listener.onError("Cannot connect to server. Check your internet or if the Server IP is correct.");
        } else if (t instanceof SocketTimeoutException) {
            listener.onError("Server connection timed out. The server might be waking up or slow.");
        } else if (t instanceof SocketException && t.getMessage().contains("reset")) {
            listener.onError("Connection reset by server. This usually happens if the URL/Port is incorrect or the server is rejecting the request.");
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
