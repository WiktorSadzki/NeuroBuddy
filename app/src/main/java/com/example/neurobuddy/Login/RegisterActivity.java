package com.example.neurobuddy.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.example.neurobuddy.TabsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextView go_back;
    private EditText edit_login_register, edit_email_register, edit_password_register;
    private Button register_button, choose_image_button;
    private ImageView show_image;
    private ProgressBar progress_bar;
    private final int PICK_IMAGE_REQUEST = 22;
    private Uri filePath;
    private String path;
    private String userId;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        go_back = findViewById(R.id.goBack);
        go_back.setOnClickListener(this);

        register_button = findViewById(R.id.registerButton);
        register_button.setOnClickListener(this);

        choose_image_button = findViewById(R.id.chooseImageButton);
        choose_image_button.setOnClickListener(this);

        show_image = findViewById(R.id.profileImageView);
        show_image.setOnClickListener(this);

        edit_login_register = findViewById(R.id.loginRegister);
        edit_email_register = findViewById(R.id.email);
        edit_password_register = findViewById(R.id.passwordRegister);

        progress_bar = findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.goBack) {
            goBackMain();
        } else if (id == R.id.registerButton) {
            registerUser();
        } else if (id == R.id.chooseImageButton) {
            SelectImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {

            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Zapisywanie zdjęcia...");
            progressDialog.show();

            path = "images/" + UUID.randomUUID().toString();

            StorageReference ref
                    = storageReference
                    .child(path);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                // Wymiary zdjęcia
                int maxWidth = 360;
                int maxHeight = 360;

                float scale = Math.min(((float) maxWidth) / bitmap.getWidth(), ((float) maxHeight) / bitmap.getHeight());

                int newWidth = Math.round(bitmap.getWidth() * scale);
                int newHeight = Math.round(bitmap.getHeight() * scale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Zdjęcie dodane pomyślnie!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            path = "images/default.png";
        }
    }


    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie profilowe"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                show_image.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerUser() {
        uploadImage();

        TextInputLayout til0 = findViewById(R.id.textInputLayout1);
        TextInputLayout til1 = findViewById(R.id.textInputLayout2);
        TextInputLayout til2 = findViewById(R.id.textInputLayout3);

        String email = edit_email_register.getText().toString().trim();
        String password = edit_password_register.getText().toString().trim();
        String login = edit_login_register.getText().toString().trim();

        if (email.isEmpty() || login.isEmpty() || password.isEmpty()) {
            if (login.isEmpty()) {
                til0.setError("Podanie loginu jest wymagane");
                edit_login_register.requestFocus();
            }   else{til0.setError(null);}

            if (email.isEmpty()) {
                til1.setError("Podanie e-mail jest wymagane!");
                edit_email_register.requestFocus();
            }   else{til1.setError(null);}

            if (password.isEmpty()) {
                til2.setError("Podanie hasła jest wymagane!");
                edit_password_register.requestFocus();
            }
            else if (password.length() < 6){
                til2.setError(null);
                til2.setError("Hasło jest za krótkie! (min. 6 znaków)");
                edit_password_register.requestFocus();
            } else{til2.setError(null);}
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            til1.setError("Wproawdzono błędny adres e-mail!");
            til1.requestFocus();
            return;
        }

        progress_bar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progress_bar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            RegisterUsers user = new RegisterUsers(login, email);

                            user.setPoints(0);
                            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                            // Save user data to the database
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(userId)
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Pomyślnie zarejestrowano. Zweryfikuj swój adres e-mail.",
                                                        Toast.LENGTH_LONG).show();

                                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                                                userRef.child("path").setValue(path);

                                                logIn();
                                            } else {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Wystąpił problem, spróbuj ponownie później.",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Wystąpił problem, spróbuj ponownie później.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        progress_bar.setVisibility(View.GONE);
    }

    private void goBackMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void logIn(){
        Intent intent = new Intent(getApplicationContext(), TabsActivity.class);
        startActivity(intent);
    }
}