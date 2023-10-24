package com.example.neurobuddy;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;


import com.example.neurobuddy.R;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SignLanguage extends AppCompatActivity {
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int numClasses = 26;
    private static final int inputSize = 224;
    private static final int numChannels = 3;
    private Interpreter tflite;
    private PreviewView cameraPreview;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_language);

        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        cameraPreview = findViewById(R.id.cameraPreview);

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("signlanguage.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String processOutput(ByteBuffer inputBuffer) {
        float[][] outputValues = new float[1][numClasses];
        tflite.run(inputBuffer, outputValues);

        int maxIndex = 0;
        float maxProbability = outputValues[0][0];
        for (int i = 1; i < outputValues[0].length; i++) {
            if (outputValues[0][i] > maxProbability) {
                maxIndex = i;
                maxProbability = outputValues[0][i];
            }
        }

        char recognizedLetter = (char) ('A' + maxIndex);

        return String.valueOf(recognizedLetter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");

            // Convert the Bitmap to a ByteBuffer
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(image);

            // Process the output using the TensorFlow Lite model
            String recognizedLetter = processOutput(inputBuffer);
            Log.v("recognizedLetter", recognizedLetter);
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * numChannels);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
            }
        }

        return byteBuffer;
    }

}
