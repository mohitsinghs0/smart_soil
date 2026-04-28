package com.example.smart_soil.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * SoilMLAnalyzer handles image preprocessing using OpenCV and inference using
 * a multi-input, multi-output TFLite model for Nitrogen, pH, and SOC prediction.
 */
public class SoilMLAnalyzer implements AutoCloseable {

    private static final String MODEL_PATH = "smart_soil_calibrated.tflite";
    private static final int INPUT_IMAGE_SIZE = 128;
    private static final int FLOAT_SIZE = 4;
    private static final int PIXEL_SIZE = 3;

    // Inverse Min-Max Scaler Constants
    private static final float MIN_N = 0.016f;
    private static final float MAX_N = 0.245f;
    private static final float MIN_PH = 6.14f;
    private static final float MAX_PH = 7.26f;
    private static final float MIN_SOC = 0.137145f;
    private static final float MAX_SOC = 2.06094f;

    private Interpreter interpreter;

    static {
        if (!OpenCVLoader.initDebug()) {
            Timber.e("OpenCV initialization failed.");
        } else {
            Timber.d("OpenCV initialization succeeded.");
        }
    }

    public SoilMLAnalyzer(Context context) throws IOException {
        initializeInterpreter(context);
    }

    private void initializeInterpreter(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);

        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_PATH);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            interpreter = new Interpreter(modelBuffer, options);
        } catch (IOException e) {
            Timber.e(e, "Failed to load TFLite model: %s", MODEL_PATH);
            throw e;
        }
    }

    /**
     * Analyzes soil properties using the provided bitmap.
     *
     * @param bitmap Input image of the soil sample.
     * @return SoilResult containing Nitrogen, pH, and SOC in actual scale.
     */
    public SoilResult analyzeSoil(Bitmap bitmap) {
        if (interpreter == null || bitmap == null) {
            return null;
        }

        // 1. Convert Bitmap to OpenCV Mat (BGR)
        Mat fullMat = new Mat();
        Utils.bitmapToMat(bitmap, fullMat);
        Imgproc.cvtColor(fullMat, fullMat, Imgproc.COLOR_RGBA2BGR);

        // 2. Crop the exact center 50%
        int width = fullMat.cols();
        int height = fullMat.rows();
        int cropW = width / 2;
        int cropH = height / 2;
        int startX = (width - cropW) / 2;
        int startY = (height - cropH) / 2;
        Rect roi = new Rect(startX, startY, cropW, cropH);
        Mat croppedMat = new Mat(fullMat, roi);

        // 3. Resize to 128x128
        Mat resizedMat = new Mat();
        Imgproc.resize(croppedMat, resizedMat, new Size(INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE));

        // 4. Extract LAB color stats
        Mat labMat = new Mat();
        Imgproc.cvtColor(resizedMat, labMat, Imgproc.COLOR_BGR2Lab);
        Scalar meanLab = Core.mean(labMat);
        float[][] labInput = new float[1][3];
        labInput[0][0] = (float) meanLab.val[0];
        labInput[0][1] = (float) meanLab.val[1];
        labInput[0][2] = (float) meanLab.val[2];

        // 5. Convert resized Mat to RGB for ByteBuffer
        Mat rgbMat = new Mat();
        Imgproc.cvtColor(resizedMat, rgbMat, Imgproc.COLOR_BGR2RGB);

        // 6. Create normalized ByteBuffer
        ByteBuffer imageByteBuffer = ByteBuffer.allocateDirect(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * PIXEL_SIZE * FLOAT_SIZE);
        imageByteBuffer.order(ByteOrder.nativeOrder());
        imageByteBuffer.rewind();

        for (int row = 0; row < INPUT_IMAGE_SIZE; row++) {
            for (int col = 0; col < INPUT_IMAGE_SIZE; col++) {
                double[] pixel = rgbMat.get(row, col);
                imageByteBuffer.putFloat((float) (pixel[0] / 255.0));
                imageByteBuffer.putFloat((float) (pixel[1] / 255.0));
                imageByteBuffer.putFloat((float) (pixel[2] / 255.0));
            }
        }

        // 7. Prepare Multi-Input Array (TFLite expects labInput at index 0)
        Object[] inputArray = {labInput, imageByteBuffer};

        // 8. Prepare Outputs
        float[][] outNitrogen = new float[1][1];
        float[][] outPh = new float[1][1];
        float[][] outSoc = new float[1][1];
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outNitrogen);
        outputMap.put(1, outPh);
        outputMap.put(2, outSoc);

        // 9. Run Inference
        try {
            interpreter.runForMultipleInputsOutputs(inputArray, outputMap);
        } catch (Exception e) {
            Timber.e(e, "Inference failed");
            return null;
        } finally {
            // Cleanup Mats
            fullMat.release();
            croppedMat.release();
            resizedMat.release();
            labMat.release();
            rgbMat.release();
        }

        // 10. Post-processing (Inverse Min-Max)
        float actualN = (outNitrogen[0][0] * (MAX_N - MIN_N)) + MIN_N;
        float actualPh = (outPh[0][0] * (MAX_PH - MIN_PH)) + MIN_PH;
        float actualSoc = (outSoc[0][0] * (MAX_SOC - MIN_SOC)) + MIN_SOC;

        return new SoilResult(actualN, actualPh, actualSoc);
    }

    @Override
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class SoilResult {
        public final float nitrogen, ph, soc;
        public SoilResult(float nitrogen, float ph, float soc) {
            this.nitrogen = nitrogen;
            this.ph = ph;
            this.soc = soc;
        }
        @Override
        public String toString() {
            return String.format(Locale.US, "N:%.3f pH:%.3f SOC:%.3f", nitrogen, ph, soc);
        }
    }
}
