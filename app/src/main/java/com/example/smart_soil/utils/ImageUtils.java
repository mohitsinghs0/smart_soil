package com.example.smart_soil.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    /**
     * Creates a temporary image file in the app's cache directory.
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Returns a content URI for the given file using FileProvider.
     */
    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    /**
     * Compresses the image at the given path to a maximum size (e.g., 1MB) and returns the compressed file.
     */
    public static File compressImage(Context context, File originalFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            File compressedFile = new File(context.getCacheDir(), "compressed_" + originalFile.getName());
            FileOutputStream out = new FileOutputStream(compressedFile);
            
            // Compress to 80% quality to reduce size significantly while maintaining visibility
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            return compressedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return originalFile; // Return original if compression fails
        }
    }
}
