package com.example.smart_soil.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.HistoryAdapter;
import com.example.smart_soil.models.SoilTest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private LineChart historyChart;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<SoilTest> testList;
    private Spinner farmSpinner;
    private MaterialButton exportPdfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyChart = findViewById(R.id.history_chart);
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        farmSpinner = findViewById(R.id.farm_spinner_history);
        exportPdfButton = findViewById(R.id.export_pdf_button);
        
        exportPdfButton.setOnClickListener(v -> Toast.makeText(this, "Export to PDF coming soon!", Toast.LENGTH_SHORT).show());

        setupFarmSpinner();
        setupRecyclerView();
        setupChart();
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
    
    private void setupRecyclerView() {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create dummy data
        testList = new ArrayList<>();
        SoilTest test1 = new SoilTest(1, "", 1.01, 285.9, 20.1, 150.5, 5.5, Arrays.asList("Rice", "Jowar"));
        test1.test_date = "09/03/2026";
        SoilTest test2 = new SoilTest(1, "", 2.18, 343.2, 30.5, 200.0, 7.0, Arrays.asList("Wheat", "Soybean"));
        test2.test_date = "15/02/2026";
        testList.add(test1);
        testList.add(test2);

        historyAdapter = new HistoryAdapter(this, testList);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void setupChart() {
        // 1. Create data entries
        ArrayList<Entry> socEntries = new ArrayList<>();
        socEntries.add(new Entry(0, 1.01f));
        socEntries.add(new Entry(1, 2.18f));

        ArrayList<Entry> nitrogenEntries = new ArrayList<>();
        nitrogenEntries.add(new Entry(0, 285.9f));
        nitrogenEntries.add(new Entry(1, 343.2f));

        ArrayList<Entry> phEntries = new ArrayList<>();
        phEntries.add(new Entry(0, 5.5f));
        phEntries.add(new Entry(1, 7.0f));

        // 2. Create data sets
        LineDataSet socDataSet = new LineDataSet(socEntries, "SOC (%)");
        socDataSet.setColor(ContextCompat.getColor(this, R.color.status_high));
        socDataSet.setCircleColor(ContextCompat.getColor(this, R.color.status_high));

        LineDataSet nitrogenDataSet = new LineDataSet(nitrogenEntries, "Nitrogen");
        nitrogenDataSet.setColor(ContextCompat.getColor(this, R.color.status_medium));
        nitrogenDataSet.setCircleColor(ContextCompat.getColor(this, R.color.status_medium));
        
        LineDataSet phDataSet = new LineDataSet(phEntries, "pH");
        phDataSet.setColor(ContextCompat.getColor(this, R.color.status_low));
        phDataSet.setCircleColor(ContextCompat.getColor(this, R.color.status_low));

        // 3. Create LineData object
        LineData lineData = new LineData(socDataSet, nitrogenDataSet, phDataSet);
        
        // 4. Configure chart appearance
        historyChart.setData(lineData);
        historyChart.getDescription().setEnabled(false);
        historyChart.getLegend().setEnabled(true);
        historyChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        historyChart.getXAxis().setGranularity(1f);
        historyChart.getAxisRight().setEnabled(false);
        
        // Format X-axis labels to show dates
        final String[] dates = new String[]{"15/02", "09/03"};
        historyChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return dates[(int) value];
            }
        });

        // 5. Refresh chart
        historyChart.invalidate();
    }
}
