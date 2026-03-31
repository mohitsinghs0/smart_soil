package com.example.smart_soil.services;

import com.example.smart_soil.models.AIChatMessage;
import com.example.smart_soil.models.AIChatSession;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.models.User;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.RegisterRequest;

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
    
    // Auth Endpoints
    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("/api/auth/profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @PUT("/api/auth/profile")
    Call<User> updateProfile(@Header("Authorization") String token, @Body User user);
    
    // Farm Endpoints
    @GET("/api/farms")
    Call<List<Farm>> getFarms(@Header("Authorization") String token);
    
    @POST("/api/farms")
    Call<Farm> createFarm(@Header("Authorization") String token, @Body Farm farm);
    
    @PUT("/api/farms/{id}")
    Call<Farm> updateFarm(@Header("Authorization") String token, @Path("id") int farmId, @Body Farm farm);
    
    @DELETE("/api/farms/{id}")
    Call<Void> deleteFarm(@Header("Authorization") String token, @Path("id") int farmId);
    
    // Soil Test Endpoints
    @GET("/api/soil-tests")
    Call<List<SoilTest>> getSoilTests(@Header("Authorization") String token, @Query("farm_id") int farmId);
    
    @Multipart
    @POST("/api/soil-tests")
    Call<SoilTest> createSoilTest(
        @Header("Authorization") String token,
        @Part MultipartBody.Part image,
        @Part("farm_id") int farmId,
        @Part("soc") double soc,
        @Part("nitrogen") double nitrogen,
        @Part("phosphorus") double phosphorus,
        @Part("potassium") double potassium,
        @Part("ph") double ph,
        @Part("recommended_crops") String recommendedCrops,
        @Part("overall_score") Integer overallScore
    );
    
    @DELETE("/api/soil-tests/{id}")
    Call<Void> deleteSoilTest(@Header("Authorization") String token, @Path("id") int testId);

    // AI Chat Endpoints
    @POST("/api/ai-chat/sessions")
    Call<AIChatSession> createAIChatSession(@Header("Authorization") String token, @Body Map<String, Long> payload);

    @GET("/api/ai-chat/sessions")
    Call<List<AIChatSession>> getAIChatSessions(@Header("Authorization") String token);

    @POST("/api/ai-chat/messages")
    Call<AIChatMessage> sendAIChatMessage(@Header("Authorization") String token, @Body AIChatMessage message);

    @GET("/api/ai-chat/sessions/{sessionId}/messages")
    Call<List<AIChatMessage>> getAIChatMessages(@Header("Authorization") String token, @Path("sessionId") Long sessionId);
}
