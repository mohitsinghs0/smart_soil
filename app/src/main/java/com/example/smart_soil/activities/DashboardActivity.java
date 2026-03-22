package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.adapters.FarmAdapter;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.utils.NavigationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DashboardActivity extends BaseActivity {

    private RecyclerView farmsRecyclerView;
    private FarmAdapter farmAdapter;
    private List<Farm> farmList = new ArrayList<>();
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

        // Set welcome message
        String userName = prefsManager.getUserName();
        welcomeMessage.setText(getString(R.string.welcome, userName));

        // Setup RecyclerView
        farmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        farmAdapter = new FarmAdapter(this, farmList);
        farmsRecyclerView.setAdapter(farmAdapter);

        // Set click listener for FAB
        fabAddFarm.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddFarmActivity.class);
            startActivity(intent);
        });

        // Setup Custom Nav
        NavigationHelper.setupCustomNav(this, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFarms();
    }

    private void loadFarms() {
        RetrofitClient.getApiService().getFarms(getAuthToken()).enqueue(new Callback<List<Farm>>() {
            @Override
            public void onResponse(Call<List<Farm>> call, Response<List<Farm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    farmList.clear();
                    farmList.addAll(response.body());
                    farmAdapter.notifyDataSetChanged();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<Farm>> call, Throwable t) {
                Timber.e(t, "Load farms failure");
            }
        });
    }

    private void updateUI() {
        if (farmList.isEmpty()) {
            farmsRecyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            farmsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }
}
