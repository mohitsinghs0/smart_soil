package com.example.smart_soil.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.HistoryAdapter;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private Spinner farmSpinner;
    private MaterialButton exportPdfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        recyclerView = findViewById(R.id.history_recycler_view);
        farmSpinner = findViewById(R.id.farm_spinner_history);
        exportPdfButton = findViewById(R.id.export_pdf_button);

        // Setup spinner
        setupFarmSpinner();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadHistoryData();

        // Export PDF
        exportPdfButton.setOnClickListener(v -> {
            Toast.makeText(this, "Exporting to PDF...", Toast.LENGTH_SHORT).show();
        });

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, R.id.btn_nav_history);
    }

    private void setupFarmSpinner() {
        List<String> farmNames = new ArrayList<>();
        farmNames.add("All Farms");
        farmNames.add("Rice - Kondhana");
        farmNames.add("Soybean Plot - Wakad");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        farmSpinner.setAdapter(adapter);
    }

    private void loadHistoryData() {
        // Dummy data for history
        List<SoilTest> dummyList = new ArrayList<>();
        
        SoilTest test1 = new SoilTest();
        test1.test_date = "22/03/2026";
        test1.soc = 1.58;
        test1.nitrogen = 173.9;
        test1.ph = 7.0;
        test1.recommended_crops = Arrays.asList("Wheat", "Soybean", "Rice");
        
        SoilTest test2 = new SoilTest();
        test2.test_date = "15/03/2026";
        test2.soc = 1.01;
        test2.nitrogen = 285.9;
        test2.ph = 5.5;
        test2.recommended_crops = Arrays.asList("Rice", "Cotton");

        dummyList.add(test1);
        dummyList.add(test2);

        adapter = new HistoryAdapter(this, dummyList);
        recyclerView.setAdapter(adapter);
    }
}
