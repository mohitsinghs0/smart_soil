package com.example.smart_soil.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
            
            // Call the updated analyzeSoil without the kitValue parameter
            SoilMLAnalyzer.SoilResult result = mlAnalyzer.analyzeSoil(modelInput);

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
        binding.tvResN.setText(String.format(Locale.US, "Nitrogen (N): %.2f", result.nitrogen));
        binding.tvResP.setText(String.format(Locale.US, "pH: %.2f", result.ph));
        binding.tvResK.setText(String.format(Locale.US, "SOC: %.2f", result.soc));
        binding.tvResPh.setText("Status: Local Analysis Complete");
        
        // Update the SOC Color Scale gauge based on result
        updateSOCGauge(result.soc);
    }

    /**
     * Updates the visual SOC Color Reference Scale.
     * Highlights the block corresponding to the predicted SOC value.
     */
    private void updateSOCGauge(float socValue) {
        View[] blocks = {
                binding.vSoc1, binding.vSoc2, binding.vSoc3, binding.vSoc4,
                binding.vSoc5, binding.vSoc6, binding.vSoc7
        };

        String[] hexColors = {
                "#F4D03F", "#EB984E", "#D35400", "#A04000",
                "#6E2C00", "#4E342E", "#212121"
        };

        int activeIndex = -1;
        if (socValue < 0.2f) activeIndex = 0;
        else if (socValue < 0.5f) activeIndex = 1;
        else if (socValue < 0.7f) activeIndex = 2;
        else if (socValue < 1.0f) activeIndex = 3;
        else if (socValue < 1.2f) activeIndex = 4;
        else if (socValue < 1.5f) activeIndex = 5;
        else activeIndex = 6;

        float density = getResources().getDisplayMetrics().density;
        int strokeWidth = (int) (3 * density);

        for (int i = 0; i < blocks.length; i++) {
            View block = blocks[i];
            int color = Color.parseColor(hexColors[i]);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setColor(color);
            
            if (i == activeIndex) {
                block.setAlpha(1.0f);
                shape.setStroke(strokeWidth, Color.WHITE);
            } else {
                block.setAlpha(0.3f);
                shape.setStroke(0, Color.TRANSPARENT);
            }
            block.setBackground(shape);
        }
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
