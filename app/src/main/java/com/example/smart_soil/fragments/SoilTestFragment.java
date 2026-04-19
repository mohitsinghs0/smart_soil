package com.example.smart_soil.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.smart_soil.ml.SoilMLAnalyzer;
import com.example.smart_soil.utils.ImageUtils;
import com.example.smart_soil.viewmodels.SoilTestViewModel;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import timber.log.Timber;

public class SoilTestFragment extends Fragment {

    private FragmentSoilTestBinding binding;
    private SoilTestViewModel viewModel;
    private SoilMLAnalyzer mlAnalyzer;
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
                    photoUri = uri;
                    binding.ivSoilPreview.setImageURI(uri);
                    binding.btnAnalyze.setEnabled(true);
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
        
        try {
            mlAnalyzer = new SoilMLAnalyzer(requireContext());
        } catch (IOException e) {
            Timber.e(e, "Failed to init ML Analyzer");
        }

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

        binding.btnAnalyze.setOnClickListener(v -> runLocalInference());

        observeViewModel();
    }

    private void runLocalInference() {
        if (photoUri == null || mlAnalyzer == null) return;

        try {
            Bitmap bitmap;
            if (photoFile != null && photoFile.exists()) {
                bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), photoUri);
            }

            if (bitmap == null) return;

            // Automated Preprocessing: Automated Center Crop + Resize to 224x224
            Bitmap modelInput = ImageUtils.processImageForModel(bitmap);
            
            // Bulletproof Inference with fresh ByteBuffer
            SoilMLAnalyzer.SoilResult result = mlAnalyzer.analyzeSoil(modelInput, 1.0f);

            if (result != null) {
                displayResults(result);
                // Optional: Sync with server
                if (photoFile != null) viewModel.predictSoil(photoFile);
            }
        } catch (IOException e) {
            Timber.e(e, "Error processing image");
        }
    }

    private void displayResults(SoilMLAnalyzer.SoilResult result) {
        binding.cvResults.setVisibility(View.VISIBLE);
        binding.tvResN.setText(String.format(Locale.US, "Nitrogen (N): %.2f", result.n));
        binding.tvResP.setText(String.format(Locale.US, "Phosphorus (P): %.2f", result.p));
        binding.tvResK.setText(String.format(Locale.US, "Potassium (K): %.2f", result.k));
        binding.tvResPh.setText("Status: Local Analysis Complete");
    }

    private void observeViewModel() {
        viewModel.getPredictionResult().observe(getViewLifecycleOwner(), resource -> {
            // Server sync observation
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mlAnalyzer != null) mlAnalyzer.close();
        binding = null;
    }
}
