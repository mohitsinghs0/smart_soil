package com.example.smart_soil.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.tensorflow.lite.Interpreter;
import timber.log.Timber;

/**
 * Bulletproof Soil ML Analyzer.
 * Fixes random values by using fresh ByteBuffers and strict normalization per call.
 */
public class SoilMLAnalyzer implements AutoCloseable {

    private static final String MODEL_PATH = "soil_model_v2.tflite";
    private static final int INPUT_SIZE = 224;
    private static final int PIXEL_SIZE = 3;
    private static final int FLOAT_SIZE = 4;

    private Interpreter interpreter;

    public SoilMLAnalyzer(Context context) throws IOException {
        initializeInterpreter(context);
    }

    private void initializeInterpreter(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        ByteBuffer modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        
        interpreter = new Interpreter(modelBuffer, options);
    }

    /**
     * TASK 2: Bulletproof Inference
     * Ensures fresh memory state for every single prediction.
     */
    public SoilResult analyzeSoil(Bitmap bitmap, float kitValue) {
        if (interpreter == null || bitmap == null) return null;

        // 1. Fresh Buffer Every Time to eliminate stale state
        ByteBuffer imgByteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE * FLOAT_SIZE);
        imgByteBuffer.order(ByteOrder.nativeOrder());

        // 2. Strict Normalization
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue : intValues) {
            imgByteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // R
            imgByteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);  // G
            imgByteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);         // B
        }

        // 3. Prepare Inputs
        float[][] kitInput = {{ kitValue }};
        Object[] inputs = {imgByteBuffer, kitInput};

        // 4. Fresh Output Containers
        float[][] outN = new float[1][1];
        float[][] outP = new float[1][1];
        float[][] outK = new float[1][1];

        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, outN);
        outputs.put(1, outP);
        outputs.put(2, outK);

        try {
            interpreter.runForMultipleInputsOutputs(inputs, outputs);
            return new SoilResult(outN[0][0], outP[0][0], outK[0][0]);
        } catch (Exception e) {
            Timber.e(e, "Inference error");
            return null;
        }
    }

    @Override
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class SoilResult {
        public final float n, p, k;
        public SoilResult(float n, float p, float k) { this.n = n; this.p = p; this.k = k; }
        @Override
        public String toString() { return String.format(Locale.US, "N:%.2f P:%.2f K:%.2f", n, p, k); }
    }
}
