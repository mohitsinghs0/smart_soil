package com.example.smart_soil.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageStandardizer {

    // ── Fixed output settings — DO NOT CHANGE these values
    public static final int TARGET_WIDTH   = 1024;
    public static final int TARGET_HEIGHT  = 768;
    public static final int JPEG_QUALITY   = 85;   // 0-100

    /**
     * Standardizes any bitmap to fixed 1024x768 JPEG at 85% quality.
     * Call this after BOTH gallery pick AND camera capture.
     *
     * @param original  Raw bitmap from camera or gallery
     * @param context   Android context for cache dir
     * @return          File pointing to standardized JPEG saved in cache
     */
    public static File standardize(Bitmap original, Context context) throws IOException {
        // Step 1: Resize to 1024x768 (center crop to maintain aspect ratio)
        Bitmap cropped = centerCrop(original, TARGET_WIDTH, TARGET_HEIGHT);

        // Step 2: Compress to JPEG 85%
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);

        // Step 3: Save to cache file
        File outputFile = new File(context.getCacheDir(), "soil_standardized_" +
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(baos.toByteArray());
        fos.flush();
        fos.close();

        // Cleanup
        if (!cropped.equals(original)) cropped.recycle();

        return outputFile;
    }

    /**
     * Load bitmap from URI (gallery picks) then standardize
     */
    public static File standardizeFromUri(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap original = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();
        if (original == null) throw new IOException("Cannot decode image from URI");
        return standardize(original, context);
    }

    /**
     * Center-crops bitmap to target dimensions without stretching
     */
    private static Bitmap centerCrop(Bitmap src, int targetW, int targetH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        float scaleW = (float) targetW / srcW;
        float scaleH = (float) targetH / srcH;
        float scale  = Math.max(scaleW, scaleH);

        int scaledW = Math.round(srcW * scale);
        int scaledH = Math.round(srcH * scale);

        // Scale up
        Bitmap scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);

        // Center crop
        int startX = (scaledW - targetW) / 2;
        int startY = (scaledH - targetH) / 2;

        Bitmap cropped = Bitmap.createBitmap(scaled, startX, startY, targetW, targetH);
        if (!scaled.equals(src)) scaled.recycle();

        return cropped;
    }
}
