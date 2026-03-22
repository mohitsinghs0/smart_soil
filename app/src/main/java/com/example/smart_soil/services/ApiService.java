package com.example.smart_soil.services;

import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.requests.AuthResponse;
import com.example.smart_soil.requests.LoginRequest;
import com.example.smart_soil.requests.RegisterRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
        @Part("farm_id") RequestBody farmId,
        @Part("soc") RequestBody soc,
        @Part("nitrogen") RequestBody nitrogen,
        @Part("phosphorus") RequestBody phosphorus,
        @Part("potassium") RequestBody potassium,
        @Part("ph") RequestBody ph,
        @Part("recommended_crops") RequestBody recommendedCrops
    );
    
    @DELETE("/api/soil-tests/{id}")
    Call<Void> deleteSoilTest(@Header("Authorization") String token, @Path("id") int testId);
}
