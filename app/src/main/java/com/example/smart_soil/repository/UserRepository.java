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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UserRepository {
    
    private final UserDao userDao;
    private final ApiService apiService;
    private final SharedPrefsManager prefsManager;
    
    public UserRepository(Context context, SharedPrefsManager prefsManager) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.apiService = RetrofitClient.getApiService();
        this.prefsManager = prefsManager;
    }
    
    /**
     * Register user with API and save to local database
     */
    public void registerUser(RegisterRequest request, RegistrationCallback callback) {
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.success) {
                        // Save to local database
                        UserEntity userEntity = new UserEntity(
                            authResponse.name,
                            request.email,
                            request.mobile,
                            request.gender,
                            request.password
                        );
                        userEntity.server_id = authResponse.user_id;
                        userEntity.token = authResponse.token;
                        
                        // Insert to database (async)
                        new Thread(() -> {
                            userDao.insertUser(userEntity);
                            
                            // Also save to SharedPrefs for quick access
                            prefsManager.saveToken(authResponse.token);
                            prefsManager.saveUserId(authResponse.user_id);
                            prefsManager.saveUserName(authResponse.name);
                            prefsManager.saveUserEmail(request.email);
                            
                            // Call callback on main thread
                            callback.onSuccess(authResponse);
                        }).start();
                    } else {
                        callback.onError(authResponse.message);
                    }
                } else {
                    callback.onError("Registration failed. Please try again.");
                    Timber.e("Registration error: %s", response.message());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Timber.e(t, "Registration API error");
            }
        });
    }
    
    /**
     * Login user with API and save to local database
     */
    public void loginUser(LoginRequest request, LoginCallback callback) {
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.success) {
                        // Check if user exists locally
                        new Thread(() -> {
                            UserEntity existingUser = userDao.getUserByEmail(request.email);
                            
                            if (existingUser != null) {
                                // Update existing user
                                existingUser.token = authResponse.token;
                                existingUser.server_id = authResponse.user_id;
                                existingUser.name = authResponse.name;
                                existingUser.last_login = System.currentTimeMillis();
                                userDao.updateUser(existingUser);
                            } else {
                                // Create new user entry
                                UserEntity userEntity = new UserEntity(
                                    authResponse.name,
                                    request.email,
                                    "",
                                    "",
                                    request.password
                                );
                                userEntity.server_id = authResponse.user_id;
                                userEntity.token = authResponse.token;
                                userEntity.last_login = System.currentTimeMillis();
                                userDao.insertUser(userEntity);
                            }
                            
                            // Save to SharedPrefs
                            prefsManager.saveToken(authResponse.token);
                            prefsManager.saveUserId(authResponse.user_id);
                            prefsManager.saveUserName(authResponse.name);
                            prefsManager.saveUserEmail(request.email);
                            
                            // Call callback
                            callback.onSuccess(authResponse);
                        }).start();
                    } else {
                        callback.onError(authResponse.message);
                    }
                } else {
                    callback.onError("Login failed. Please try again.");
                    Timber.e("Login error: %s", response.message());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Timber.e(t, "Login API error");
            }
        });
    }
    
    /**
     * Get current user from local database
     */
    public UserEntity getCurrentUser() {
        try {
            return userDao.getCurrentUser();
        } catch (Exception e) {
            Timber.e(e, "Error fetching current user");
            return null;
        }
    }
    
    /**
     * Clear all users (logout)
     */
    public void clearAllUsers() {
        new Thread(() -> {
            userDao.deleteAllUsers();
            prefsManager.clearAll();
        }).start();
    }
    
    // Callbacks
    public interface RegistrationCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }
    
    public interface LoginCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }
}
