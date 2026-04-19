package com.example.smart_soil.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.database.SoilTestDao;
import com.example.smart_soil.database.SoilTestEntity;
import com.example.smart_soil.models.SoilPredictionResponse;
import com.example.smart_soil.services.ApiService;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.Resource;
import com.example.smart_soil.utils.SharedPrefsManager;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoilTestViewModel extends AndroidViewModel {
    private final SoilTestDao soilTestDao;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final String authToken;

    private final MutableLiveData<Resource<SoilPredictionResponse>> predictionResult = new MutableLiveData<>();

    public SoilTestViewModel(@NonNull Application application) {
        super(application);
        SmartSoilDatabase db = SmartSoilDatabase.getInstance(application);
        soilTestDao = db.soilTestDao();
        apiService = RetrofitClient.getApiService(application);
        executor = Executors.newSingleThreadExecutor();
        authToken = "Bearer " + new SharedPrefsManager(application).getToken();
    }

    public LiveData<Resource<SoilPredictionResponse>> getPredictionResult() {
        return predictionResult;
    }

    public void predictSoil(File imageFile) {
        predictionResult.setValue(Resource.loading(null));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        apiService.predictSoil(authToken, body).enqueue(new Callback<SoilPredictionResponse>() {
            @Override
            public void onResponse(Call<SoilPredictionResponse> call, Response<SoilPredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    predictionResult.setValue(Resource.success(response.body()));
                    
                    // Optionally save to local DB here if you have a farmId
                } else {
                    predictionResult.setValue(Resource.error("Prediction failed: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<SoilPredictionResponse> call, Throwable t) {
                predictionResult.setValue(Resource.error("Network error: " + t.getMessage(), null));
            }
        });
    }

    public LiveData<List<SoilTestEntity>> getTestsForFarm(int farmId) {
        return soilTestDao.getTestsForFarm(farmId);
    }

    public void addSoilTest(SoilTestEntity test) {
        executor.execute(() -> soilTestDao.insert(test));
    }
}
