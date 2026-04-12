package com.example.smart_soil.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smart_soil.databinding.FragmentSoilTestBinding;
import com.example.smart_soil.utils.ImageUtils;
import com.example.smart_soil.viewmodels.SoilTestViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoilTestFragment extends Fragment {

    private FragmentSoilTestBinding binding;
    private SoilTestViewModel viewModel;
    private File photoFile;
    private Uri photoUri;

    private final ActivityResultLauncher<Uri> takePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoFile != null) {
                    binding.ivSoilPreview.setImageURI(photoUri);
                    binding.btnAnalyze.setEnabled(true);
                }
            }
    );

    private final ActivityResultLauncher<String> pickGallery = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        photoFile = ImageUtils.createImageFile(requireContext());
                        copyUriToFile(uri, photoFile);
                        binding.ivSoilPreview.setImageURI(uri);
                        binding.btnAnalyze.setEnabled(true);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error selecting image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSoilTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SoilTestViewModel.class);

        binding.btnCamera.setOnClickListener(v -> {
            try {
                photoFile = ImageUtils.createImageFile(requireContext());
                photoUri = ImageUtils.getUriForFile(requireContext(), photoFile);
                takePicture.launch(photoUri);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error creating file", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnGallery.setOnClickListener(v -> pickGallery.launch("image/*"));

        binding.btnAnalyze.setOnClickListener(v -> {
            if (photoFile != null) {
                File compressed = ImageUtils.compressImage(requireContext(), photoFile);
                viewModel.predictSoil(compressed);
            }
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getPredictionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.cvResults.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.cvResults.setVisibility(View.VISIBLE);
                    binding.btnAnalyze.setEnabled(true);
                    if (resource.data != null) {
                        binding.tvResN.setText("Nitrogen (N): " + resource.data.getNitrogen());
                        binding.tvResP.setText("Phosphorus (P): " + resource.data.getPhosphorus());
                        binding.tvResK.setText("Potassium (K): " + resource.data.getPotassium());
                        binding.tvResPh.setText("pH Level: " + resource.data.getPh());
                        binding.tvResCrop.setText("Recommended Crop: " + resource.data.getRecommendedCrop());
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAnalyze.setEnabled(true);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void copyUriToFile(Uri uri, File destFile) throws IOException {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
