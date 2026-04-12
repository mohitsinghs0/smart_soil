package com.example.smart_soil.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.repository.FarmRepository;
import com.example.smart_soil.utils.SharedPrefsManager;
import java.util.List;

public class FarmViewModel extends AndroidViewModel {
    private final FarmRepository repository;
    private final LiveData<List<FarmEntity>> allFarms;

    public FarmViewModel(@NonNull Application application) {
        super(application);
        SharedPrefsManager prefsManager = new SharedPrefsManager(application);
        String token = prefsManager.getToken();
        repository = new FarmRepository(application, token);
        allFarms = repository.getLocalFarms();
    }

    public LiveData<List<FarmEntity>> getAllFarms() {
        return allFarms;
    }

    public void refreshFarms() {
        repository.getFarms();
    }

    public void addFarm(FarmEntity farm) {
        repository.addFarm(farm);
    }
}
