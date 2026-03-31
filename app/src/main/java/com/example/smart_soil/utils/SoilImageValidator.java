package com.example.smart_soil.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * SoilImageValidator — validates whether an image contains soil/earth samples.
 *
 * ARCHITECTURE NOTE (for future ML model integration):
 * When madam's ML model is ready, ONLY replace the body of
 * `analyzeWithHeuristic()` method with ML model inference.
 * The public API (validate() method signature) stays the same.
 * SoilTestActivity.java will require ZERO changes.
 */
public class SoilImageValidator {

    // ── Tunable thresholds ─────────────────────────────────────────────
    // Minimum % of pixels that must be "earthy/soil" colored
    private static final float SOIL_PIXEL_THRESHOLD = 0.25f; // 25%

    // HSV ranges for soil/earth colors
    private static final float HUE_MIN   = 10f;   // degrees
    private static final float HUE_MAX   = 45f;   // degrees (brown/tan range)
    private static final float SAT_MIN   = 0.10f; // 10% — not too gray
    private static final float SAT_MAX   = 0.75f; // 75% — not too vivid/neon
    private static final float VAL_MIN   = 0.10f; // 10% — not pure black
    private static final float VAL_MAX   = 0.85f; // 85% — not pure white

    // Thumbnail size for fast processing
    private static final int SAMPLE_SIZE = 100;

    // ── Public result class ────────────────────────────────────────────
    public static class ValidationResult {
        public final boolean isValid;
        public final String  errorMessage;
        public final float   soilPixelRatio; // for debugging

        ValidationResult(boolean isValid, String errorMessage, float ratio) {
            this.isValid        = isValid;
            this.errorMessage   = errorMessage;
            this.soilPixelRatio = ratio;
        }
    }

    /**
     * Main entry point — call this after image is captured/selected.
     * This method signature will NOT change when ML model is integrated.
     *
     * @param bitmap  The standardized bitmap (output of ImageStandardizer)
     * @return        ValidationResult with isValid flag and error message
     */
    public static ValidationResult validate(Bitmap bitmap) {
        if (bitmap == null) {
            return new ValidationResult(false, "No image provided. Please select or capture a soil image.", 0f);
        }
        // ── Currently using color heuristic ──
        // ── Replace ONLY this line when ML model is ready: ──
        return analyzeWithHeuristic(bitmap);
        // ── Future: return analyzeWithMLModel(bitmap); ──
    }

    /**
     * TEMPORARY: Color-based heuristic validator.
     * Detects earthy/brown tones dominant in soil images.
     *
     * ⚠️ REPLACE THIS METHOD BODY with ML model when available.
     * Method signature must stay: private static ValidationResult analyzeWithHeuristic(Bitmap bitmap)
     */
    private static ValidationResult analyzeWithHeuristic(Bitmap bitmap) {
        // Step 1: Scale down for fast processing
        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, SAMPLE_SIZE, SAMPLE_SIZE, true);

        int totalPixels = SAMPLE_SIZE * SAMPLE_SIZE;
        int soilPixels  = 0;

        float[] hsv = new float[3];

        // Step 2: Count soil-colored pixels
        for (int x = 0; x < SAMPLE_SIZE; x++) {
            for (int y = 0; y < SAMPLE_SIZE; y++) {
                int pixel = thumb.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                Color.RGBToHSV(r, g, b, hsv);

                float hue = hsv[0];
                float sat = hsv[1];
                float val = hsv[2];

                // Check if pixel is in earthy/soil color range
                if (hue >= HUE_MIN && hue <= HUE_MAX
                        && sat >= SAT_MIN && sat <= SAT_MAX
                        && val >= VAL_MIN && val <= VAL_MAX) {
                    soilPixels++;
                }
            }
        }

        thumb.recycle();

        float ratio = (float) soilPixels / totalPixels;

        // Step 3: Decision
        if (ratio >= SOIL_PIXEL_THRESHOLD) {
            return new ValidationResult(true, null, ratio);
        } else {
            return new ValidationResult(
                false,
                "Invalid image detected.\n\nPlease upload an image of soil or a soil testing sample (test tube, petri dish, or farm soil).\n\nSelfies, objects, and unrelated images are not accepted.",
                ratio
            );
        }
    }
}
