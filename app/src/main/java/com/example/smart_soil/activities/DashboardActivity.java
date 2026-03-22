package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.FarmAdapter;
import com.example.smart_soil.models.Farm;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private RecyclerView farmsRecyclerView;
    private FarmAdapter farmAdapter;
    private List<Farm> farmList;
    private TextView welcomeMessage;
    private FloatingActionButton fabAddFarm;
    private LinearLayout emptyStateContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        welcomeMessage = findViewById(R.id.welcome_message);
        farmsRecyclerView = findViewById(R.id.farms_recycler_view);
        fabAddFarm = findViewById(R.id.fab_add_farm);
        emptyStateContainer = findViewById(R.id.empty_state_container);

        // Set welcome message (using placeholder name)
        welcomeMessage.setText(getString(R.string.welcome, "Mohit Singh"));

        // Setup RecyclerView
        farmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create dummy data
        createDummyFarms();

        // Initialize and set adapter
        farmAdapter = new FarmAdapter(this, farmList);
        farmsRecyclerView.setAdapter(farmAdapter);

        // Check for empty state
        if (farmList.isEmpty()) {
            farmsRecyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            farmsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }

        // Set click listener for FAB
        fabAddFarm.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddFarmActivity.class);
            startActivity(intent);
        });
    }

    private void createDummyFarms() {
        farmList = new ArrayList<>();
        
        Farm farm1 = new Farm("Rice", "Kondhana", "Pune", "Maharashtra", "Rice", 18.5204, 73.8567);
        farmList.add(farm1);
        
        Farm farm2 = new Farm("Soybean Plot", "Wakad", "Pune", "Maharashtra", "Soybean", 18.59, 73.75);
        farmList.add(farm2);
    }
}
