package com.example.smart_soil.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.smart_soil.database.FarmDao;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.ApiService;
import com.example.smart_soil.services.RetrofitClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FarmRepository {
    private final FarmDao farmDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final String authToken;
    private final Application application;

    public FarmRepository(Application application, String authToken) {
        this.application = application;
        SmartSoilDatabase db = SmartSoilDatabase.getInstance(application);
        this.farmDao = db.farmDao();
        this.apiService = RetrofitClient.getApiService(application);
        this.executor = Executors.newSingleThreadExecutor();
        this.authToken = "Bearer " + authToken;
    }

    public void refreshFarms() {
        apiService.getFarms(authToken).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        for (Farm farm : response.body()) {
                            String serverId = String.valueOf(farm.id);
                            FarmEntity existing = farmDao.getFarmByServerId(serverId);
                            
                            FarmEntity entity = new FarmEntity(
                                farm.name, 
                                farm.village, 
                                farm.city, 
                                farm.district, 
                                farm.state, 
                                farm.soil_type, 
                                farm.crop_type, 
                                farm.latitude != null ? farm.latitude : 0.0, 
                                farm.longitude != null ? farm.longitude : 0.0, 
                                farm.area != null ? farm.area : 0.0
                            );
                            entity.setServerId(serverId);
                            entity.setSynced(true);
                            
                            if (existing != null) {
                                entity.setId(existing.getId());
                                farmDao.update(entity);
                            } else {
                                farmDao.insert(entity);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "Failed to refresh farms from Supabase");
            }
        });
    }

    public LiveData<List<FarmEntity>> getLocalFarms() {
        return farmDao.getAllFarms();
    }

    public void addFarm(FarmEntity farm) {
        executor.execute(() -> {
            long localId = farmDao.insert(farm);
            
            Map<String, Object> farmData = new HashMap<>();
            farmData.put("name", farm.getName());
            farmData.put("village", farm.getVillage());
            farmData.put("city", farm.getCity());
            farmData.put("district", farm.getDistrict());
            farmData.put("state", farm.getState());
            farmData.put("soil_type", farm.getSoilType());
            farmData.put("crop_type", farm.getCropType());
            farmData.put("lat", farm.getLatitude());
            farmData.put("lng", farm.getLongitude());
            farmData.put("area", farm.getArea());

            apiService.createFarm(authToken, farmData).enqueue(new Callback<List<Farm>>() {
                @Override
                public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        executor.execute(() -> {
                            farm.setId((int) localId);
                            farm.setServerId(String.valueOf(response.body().get(0).id));
                            farm.setSynced(true);
                            farmDao.update(farm);
                        });
                    } else {
                        Timber.e("Failed to add farm to Supabase: %s", response.message());
                    }
                }

                @Override
                public void onFailure(Call<List<Farm>> call, Throwable t) {
                    Timber.e(t, "Failed to add farm to Supabase");
                }
            });
        });
    }

    public void deleteFarm(FarmEntity farm) {
        executor.execute(() -> {
            farmDao.delete(farm);
            
            if (farm.isSynced() && farm.getServerId() != null) {
                apiService.deleteFarm(authToken, "eq." + farm.getServerId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Timber.e("Failed to delete farm from Supabase: %d", response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Timber.e(t, "Error deleting farm from Supabase");
                    }
                });
            }
        });
    }
}
