package com.example.smart_soil.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import timber.log.Timber;

/**
 * SoilImageValidator — validates whether an image contains soil/earth samples or test tubes.
 * Updated to be more permissive for greyish/dark soil and test tube samples.
 */
public class SoilImageValidator {

    // Minimum % of pixels that must match soil-like characteristics.
    // Lowered to 10% to accommodate test tubes which might only occupy a small part of the frame.
    private static final float SOIL_PIXEL_THRESHOLD = 0.10f;

    // Thumbnail size for fast processing
    private static final int SAMPLE_SIZE = 100;

    public static class ValidationResult {
        public final boolean isValid;
        public final String  errorMessage;
        public final float   soilPixelRatio;

        ValidationResult(boolean isValid, String errorMessage, float ratio) {
            this.isValid        = isValid;
            this.errorMessage   = errorMessage;
            this.soilPixelRatio = ratio;
        }
    }

    /**
     * Main entry point — validates if the bitmap contains soil or test tube.
     */
    public static ValidationResult validate(Bitmap bitmap) {
        if (bitmap == null) {
            return new ValidationResult(false, "No image provided.", 0f);
        }
        return analyzeWithHeuristic(bitmap);
    }

    private static ValidationResult analyzeWithHeuristic(Bitmap bitmap) {
        // Step 1: Scale down for fast processing
        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, SAMPLE_SIZE, SAMPLE_SIZE, true);

        int totalPixels = SAMPLE_SIZE * SAMPLE_SIZE;
        int soilPixels  = 0;

        float[] hsv = new float[3];

        // Step 2: Count soil-colored and neutral pixels
        for (int x = 0; x < SAMPLE_SIZE; x++) {
            for (int y = 0; y < SAMPLE_SIZE; y++) {
                int pixel = thumb.getPixel(x, y);

                Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv);

                float hue = hsv[0];
                float sat = hsv[1];
                float val = hsv[2];

                // --- BROAD DETECTION LOGIC ---
                
                // 1. Earthy tones: Broadened to 0-90 degrees (Red, Brown, Orange, Tan, Yellow, Olive)
                // Most soil falls in the 0-50 range, but some clay/silt can be more yellow/greenish.
                boolean isEarthy = (hue >= 0 && hue <= 90);
                
                // 2. Neutral/Grey tones: Low saturation (covers grey soil, glass, petri dishes, white labs)
                // Soil often has very low saturation, especially if it's dry or greyish.
                boolean isNeutral = (sat <= 0.40f);
                
                // 3. Dark tones: Low value (covers black soil and deep shadows in soil texture)
                boolean isDark = (val <= 0.25f);
                
                // 4. Highlighted tones: High value + low saturation (reflections on glass/test tubes)
                boolean isReflective = (val > 0.85f && sat < 0.20f);

                // Combine conditions: If a pixel is Earthy OR Neutral OR Dark OR Reflective, it's a candidate.
                boolean isCandidate = isEarthy || isNeutral || isDark || isReflective;

                // Exclude "Vivid Artificial" colors: 
                // High saturation in the Blue/Purple/Pink range (definitely not soil)
                boolean isVividArtificial = (sat > 0.60f && (hue > 150 && hue < 330));

                if (isCandidate && !isVividArtificial) {
                    soilPixels++;
                }
            }
        }

        thumb.recycle();

        float ratio = (float) soilPixels / totalPixels;
        Timber.d("Soil Validation: %d/%d pixels matched. Ratio: %.2f", soilPixels, totalPixels, ratio);

        // Step 3: Decision
        if (ratio >= SOIL_PIXEL_THRESHOLD) {
            return new ValidationResult(true, null, ratio);
        } else {
            return new ValidationResult(
                false,
                "Invalid image detected.\n\nPlease upload an image of soil or a soil testing sample (test tube, petri dish, or farm soil).\n\nSelfies, bright objects, and unrelated images are not accepted.",
                ratio
            );
        }
    }
}
