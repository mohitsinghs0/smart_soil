package com.example.smart_soil.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.HistoryAdapter;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class HistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private Spinner farmSpinner;
    private MaterialButton exportPdfButton;
    private List<Farm> farmList = new ArrayList<>();
    private List<SoilTest> testList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        recyclerView = findViewById(R.id.history_recycler_view);
        farmSpinner = findViewById(R.id.farm_spinner_history);
        exportPdfButton = findViewById(R.id.export_pdf_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, testList);
        recyclerView.setAdapter(adapter);

        // Load Farms
        loadFarms();

        // Export PDF
        exportPdfButton.setOnClickListener(v -> {
            if (testList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Exporting to PDF (Coming Soon)...", Toast.LENGTH_SHORT).show();
                // TODO: Implement iTextPDF export
            }
        });

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_history);
    }

    private void loadFarms() {
        RetrofitClient.getApiService(this).getFarms(getAuthToken()).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    farmList = response.body();
                    setupFarmSpinner();
                } else {
                    Toast.makeText(HistoryActivity.this, "Failed to load farms", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "Load farms failure");
                Toast.makeText(HistoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFarmSpinner() {
        List<String> farmNames = new ArrayList<>();
        for (Farm farm : farmList) {
            farmNames.add(farm.name);
        }

        if (farmNames.isEmpty()) {
            farmNames.add("No farms available");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(spinnerAdapter);

        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!farmList.isEmpty()) {
                    loadSoilTests(farmList.get(position).id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Auto-select farm if passed from intent
        int targetFarmId = getIntent().getIntExtra("farm_id", -1);
        if (targetFarmId != -1) {
            for (int i = 0; i < farmList.size(); i++) {
                if (farmList.get(i).id == targetFarmId) {
                    farmSpinner.setSelection(i);
                    // loadSoilTests will be triggered by onItemSelected
                    return;
                }
            }
        }
        
        if (!farmList.isEmpty()) {
            loadSoilTests(farmList.get(0).id);
        }
    }

    private void loadSoilTests(int farmId) {
        // Corrected Query: Supabase needs 'eq.ID' for query parameters in filter
        RetrofitClient.getApiService(this).getSoilTests(getAuthToken(), "eq." + farmId).enqueue(new Callback<List<SoilTest>>() {
            @Override
            public void onResponse(Call<List<SoilTest>> call, Response<List<SoilTest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    testList.clear();
                    testList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (testList.isEmpty()) {
                        Toast.makeText(HistoryActivity.this, "No history found for this farm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Timber.e("History Load Fail: %d - %s", response.code(), response.message());
                    Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SoilTest>> call, Throwable t) {
                Timber.e(t, "Load soil tests failure");
                Toast.makeText(HistoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
