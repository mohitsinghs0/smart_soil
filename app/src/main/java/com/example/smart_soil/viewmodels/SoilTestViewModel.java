package com.example.smart_soil.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.smart_soil.database.SmartSoilDatabase;
import com.example.smart_soil.database.SoilTestDao;
import com.example.smart_soil.database.SoilTestEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoilTestViewModel extends AndroidViewModel {
    private final SoilTestDao soilTestDao;
    private final ExecutorService executor;

    public SoilTestViewModel(@NonNull Application application) {
        super(application);
        SmartSoilDatabase db = SmartSoilDatabase.getInstance(application);
        soilTestDao = db.soilTestDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<SoilTestEntity>> getTestsForFarm(int farmId) {
        return soilTestDao.getTestsForFarm(farmId);
    }

    public void addSoilTest(SoilTestEntity test) {
        executor.execute(() -> soilTestDao.insert(test));
    }
}
