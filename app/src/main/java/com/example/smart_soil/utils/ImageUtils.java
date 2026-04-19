package com.example.smart_soil.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    /**
     * TASK 1: Automated Image Preprocessor
     * Automatically Center Crops and Resizes any bitmap to 224x224.
     */
    public static Bitmap processImageForModel(Bitmap originalBitmap) {
        if (originalBitmap == null) return null;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        int size = Math.min(width, height);

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // 1. Center Crop to Square
        Bitmap squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size);

        // 2. Resize to Model Input Shape (224x224)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, 224, 224, true);
        
        // Clean up intermediate bitmap if it's different
        if (squareBitmap != originalBitmap && squareBitmap != resizedBitmap) {
            squareBitmap.recycle();
        }

        return resizedBitmap;
    }

    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static Intent getCameraIntent(Context context, Uri photoUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }
}
