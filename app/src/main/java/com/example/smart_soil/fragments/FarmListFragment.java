package com.example.smart_soil.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smart_soil.adapters.FarmEntityAdapter;
import com.example.smart_soil.databinding.FragmentFarmListBinding;
import com.example.smart_soil.viewmodels.FarmViewModel;

public class FarmListFragment extends Fragment {

    private FragmentFarmListBinding binding;
    private FarmViewModel viewModel;
    private FarmEntityAdapter adapter;

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
        adapter = new FarmEntityAdapter(requireContext());
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
        binding.progress_bar.setVisibility(View.VISIBLE);
        viewModel.getAllFarms().observe(getViewLifecycleOwner(), farms -> {
            binding.progress_bar.setVisibility(View.GONE);
            if (farms == null || farms.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvFarms.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvFarms.setVisibility(View.VISIBLE);
                adapter.setFarms(farms);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
