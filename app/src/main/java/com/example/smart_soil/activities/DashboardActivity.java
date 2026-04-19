package com.example.smart_soil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smart_soil.R;
import com.example.smart_soil.adapters.FarmAdapter;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.databinding.ActivityDashboardBinding;
import com.example.smart_soil.utils.NavigationHelper;
import com.example.smart_soil.viewmodels.FarmViewModel;

public class DashboardActivity extends BaseActivity {

    private ActivityDashboardBinding binding;
    private FarmViewModel viewModel;
    private FarmAdapter farmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FarmViewModel.class);

        setupUI();
        observeViewModel();
        
        NavigationHelper.setupCustomNav(this, -1);
    }

    private void setupUI() {
        binding.welcomeMessage.setText(getString(R.string.welcome, prefsManager.getUserName()));

        binding.farmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        farmAdapter = new FarmAdapter(new FarmAdapter.OnFarmClickListener() {
            @Override
            public void onFarmClick(FarmEntity farm) {}

            @Override
            public void onTestClick(FarmEntity farm) {
                Intent intent = new Intent(DashboardActivity.this, SoilTestActivity.class);
                intent.putExtra("farm_id", farm.getId());
                intent.putExtra("farm_name", farm.getName());
                startActivity(intent);
            }

            @Override
            public void onHistoryClick(FarmEntity farm) {
                Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                intent.putExtra("farm_id", farm.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(FarmEntity farm) {
                Intent intent = new Intent(DashboardActivity.this, AddFarmActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("edit_mode", true);
                intent.putExtra("farm_id", farm.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(FarmEntity farm) {
                viewModel.deleteFarm(farm);
                Toast.makeText(DashboardActivity.this, "Farm deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });
        binding.farmsRecyclerView.setAdapter(farmAdapter);

        binding.fabAddFarm.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AddFarmActivity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getAllFarms().observe(this, farms -> {
            if (farms == null || farms.isEmpty()) {
                binding.farmsRecyclerView.setVisibility(View.GONE);
                binding.emptyStateContainer.setVisibility(View.VISIBLE);
            } else {
                binding.farmsRecyclerView.setVisibility(View.VISIBLE);
                binding.emptyStateContainer.setVisibility(View.GONE);
                farmAdapter.submitList(farms);
            }
        });

        viewModel.refreshFarms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshFarms();
    }
}
