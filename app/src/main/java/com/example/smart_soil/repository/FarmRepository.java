package com.example.smart_soil.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smart_soil.database.FarmDao;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.ApiService;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmRepository {
    private final FarmDao farmDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final String authToken;

    public FarmRepository(Application application, String authToken) {
        SmartSoilDatabase db = SmartSoilDatabase.getInstance(application);
        this.farmDao = db.farmDao();
        this.apiService = RetrofitClient.getApiService();
        this.executor = Executors.newSingleThreadExecutor();
        this.authToken = "Bearer " + authToken;
    }

    public LiveData<Resource<List<FarmEntity>>> getFarms() {
        MutableLiveData<Resource<List<FarmEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // 1. Fetch from Local Room DB immediately
        LiveData<List<FarmEntity>> localData = farmDao.getAllFarms();
        
        // 2. Fetch from Network in background
        apiService.getFarms(authToken).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        // Update Room with fresh data from Server
                        for (Farm farm : response.body()) {
                            FarmEntity entity = new FarmEntity(farm.getName(), farm.getLocation(), farm.getCropType());
                            entity.setServerId(String.valueOf(farm.getId()));
                            entity.setSynced(true);
                            farmDao.insert(entity);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                // Network failed, but Room data is already being observed by UI via LiveData
            }
        });

        return result; // UI will observe the Room LiveData directly for the "Source of Truth"
    }

    public LiveData<List<FarmEntity>> getLocalFarms() {
        return farmDao.getAllFarms();
    }

    public void addFarm(FarmEntity farm) {
        executor.execute(() -> {
            long localId = farmDao.insert(farm);
            
            // Try to sync with server
            Farm farmModel = new Farm();
            farmModel.setName(farm.getName());
            farmModel.setLocation(farm.getLocation());
            farmModel.setCropType(farm.getCropType());

            apiService.createFarm(authToken, farmModel).enqueue(new Callback<Farm>() {
                @Override
                public void onResponse(Call<Farm> call, Response<Farm> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> {
                            farm.setId((int) localId);
                            farm.setServerId(String.valueOf(response.body().getId()));
                            farm.setSynced(true);
                            farmDao.update(farm);
                        });
                    }
                }

                @Override
                public void onFailure(Call<Farm> call, Throwable t) {
                    // Stay unsynced, retry later
                }
            });
        });
    }
}
