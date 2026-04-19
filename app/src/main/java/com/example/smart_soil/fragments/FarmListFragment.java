package com.example.smart_soil.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smart_soil.activities.AddFarmActivity;
import com.example.smart_soil.activities.HistoryActivity;
import com.example.smart_soil.activities.SoilTestActivity;
import com.example.smart_soil.adapters.FarmAdapter;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.databinding.FragmentFarmListBinding;
import com.example.smart_soil.viewmodels.FarmViewModel;

public class FarmListFragment extends Fragment {

    private FragmentFarmListBinding binding;
    private FarmViewModel viewModel;
    private FarmAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFarmListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FarmViewModel.class);
        adapter = new FarmAdapter(new FarmAdapter.OnFarmClickListener() {
            @Override
            public void onFarmClick(FarmEntity farm) {}

            @Override
            public void onTestClick(FarmEntity farm) {
                Intent intent = new Intent(getContext(), SoilTestActivity.class);
                intent.putExtra("farm_id", farm.getId());
                intent.putExtra("farm_name", farm.getName());
                startActivity(intent);
            }

            @Override
            public void onHistoryClick(FarmEntity farm) {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                intent.putExtra("farm_id", farm.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(FarmEntity farm) {
                Intent intent = new Intent(getContext(), AddFarmActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("farm_id", farm.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(FarmEntity farm) {
                viewModel.deleteFarm(farm);
                Toast.makeText(getContext(), "Farm deleted", Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvFarms.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshFarms();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.btnRetry.setOnClickListener(v -> viewModel.refreshFarms());

        observeViewModel();
        viewModel.refreshFarms();
    }

    private void observeViewModel() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getAllFarms().observe(getViewLifecycleOwner(), farms -> {
            binding.progressBar.setVisibility(View.GONE);
            if (farms == null || farms.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvFarms.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvFarms.setVisibility(View.VISIBLE);
                adapter.submitList(farms);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
