package com.example.smart_soil.services;

import com.example.smart_soil.models.AIChatMessage;
import com.example.smart_soil.models.AIChatSession;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilPredictionResponse;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.models.SoilTestRequest;
import com.example.smart_soil.models.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    // Profiles
    @GET("rest/v1/profiles?select=*")
    Call<List<User>> getProfile(@Header("Authorization") String token);

    @PUT("rest/v1/profiles")
    Call<Void> updateProfile(@Header("Authorization") String token, @Query("id") String userId, @Body User user);
    
    // Farms
    @GET("rest/v1/farms?select=*")
    Call<List<Farm>> getFarms(@Header("Authorization") String token);
    
    @POST("rest/v1/farms")
    Call<List<Farm>> createFarm(@Header("Authorization") String token, @Body Map<String, Object> farm);
    
    @PUT("rest/v1/farms")
    Call<Void> updateFarm(@Header("Authorization") String token, @Query("id") String farmId, @Body Farm farm);
    
    @DELETE("rest/v1/farms")
    Call<Void> deleteFarm(@Header("Authorization") String token, @Query("id") String farmId);
    
    // Soil Tests
    @GET("rest/v1/soil_tests?select=*")
    Call<List<SoilTest>> getSoilTests(@Header("Authorization") String token, @Query("farm_id") String farmId);
    
    @POST("rest/v1/soil_tests")
    Call<List<SoilTest>> createSoilTest(@Header("Authorization") String token, @Body SoilTestRequest body);

    @DELETE("rest/v1/soil_tests")
    Call<Void> deleteSoilTest(@Header("Authorization") String token, @Query("id") String testId);

    // AI Chat
    @POST("rest/v1/ai_chat_sessions")
    Call<List<AIChatSession>> createAIChatSession(@Header("Authorization") String token, @Body Map<String, Object> payload);

    @GET("rest/v1/ai_chat_sessions?select=*")
    Call<List<AIChatSession>> getAIChatSessions(@Header("Authorization") String token);

    @POST("rest/v1/ai_chat_messages")
    Call<List<AIChatMessage>> sendAIChatMessage(@Header("Authorization") String token, @Body AIChatMessage message);

    @GET("rest/v1/ai_chat_messages?select=*")
    Call<List<AIChatMessage>> getAIChatMessages(@Header("Authorization") String token, @Query("session_id") String sessionId);

    // Notifications
    @GET("rest/v1/notifications?select=*")
    Call<List<Map<String, Object>>> getNotifications(@Header("Authorization") String token);

    // Image Prediction
    @Multipart
    @POST("predict")
    Call<SoilPredictionResponse> predictSoil(@Header("Authorization") String token, @Part MultipartBody.Part image);
}
