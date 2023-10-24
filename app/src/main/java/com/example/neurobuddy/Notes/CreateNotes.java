package com.example.neurobuddy.Notes;

import static com.google.api.LabelDescriptor.ValueType.valueOf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.neurobuddy.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CreateNotes extends AppCompatActivity {
    String fileName = "polish-words.txt";
    private ImageView picturePreview;
    private EditText noteText;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notes);

        picturePreview = findViewById(R.id.picturePreview);

        noteText = findViewById(R.id.note);

        Button b = findViewById(R.id.camera);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");

            // Recognize text in the image
            recognizeText(image);
        }
    }

    private void recognizeText(Bitmap bitmap) {
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);



        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = textRecognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String recognizedText = visionText.getText();

                        Log.v("NotesText", recognizedText);

                        try {
                            String line = "", mostSimilar = "";
                            double procentage = 0.0;

                            FileReader fileReader =
                                    new FileReader(fileName);
                            BufferedReader bufferedReader =
                                    new BufferedReader(fileReader);

                            String[] text = recognizedText.split(" ");

                            StringBuilder modifiedText = new StringBuilder();

                            while((line = bufferedReader.readLine()) != null) {
                                for (int i = 0; i < text.length; i++) {
                                    String part = text[i];
                                    if (similarity(part, line) >= procentage) {
                                        procentage = similarity(recognizedText, mostSimilar);
                                        if(procentage>0.6){
                                            text[i] = mostSimilar;
                                        }

                                        String similar = String.valueOf(similarity(recognizedText, mostSimilar));
                                        System.out.println(recognizedText + " : " + similar + " : " + mostSimilar);
                                    }

                                    modifiedText.append(text[i]);

                                    if (i < text.length - 1) {
                                        modifiedText.append(" ");
                                    }
                                }

                                String modifiedRecognizedText = modifiedText.toString();
                                noteText.setText(modifiedRecognizedText);
                            }
                        } catch (IOException ignored) {

                        }
                        picturePreview.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TextRecognition", "Error recognizing text: " + e.getMessage());
                    }
                });
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0;}
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showMessageOKCancel("You need to allow access permissions",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestCameraPermission();
                                }
                            });
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(CreateNotes.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}