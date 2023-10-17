package com.example.neurobuddy.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neurobuddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity implements View.OnClickListener {

    private EditText forgotEmailText;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;
    private TextView go_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        forgotEmailText = (EditText) findViewById(R.id.email);
        resetPasswordButton = (Button) findViewById(R.id.resetPasswordButton);
        go_back = (TextView) findViewById(R.id.goBack);

        mAuth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.goBack) {
            goBackMain();
        } else if (id == R.id.resetPasswordButton) {
            resetPassword();
        }
    }

    private void resetPassword() {
        String email = forgotEmailText.getText().toString().trim();
        if (email.isEmpty()) {
            TextInputLayout til = (TextInputLayout) findViewById(R.id.textInputLayout1);
            til.setError("Nie podano adresu e-mail!");
            til.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            TextInputLayout til = (TextInputLayout) findViewById(R.id.textInputLayout1);
            til.setError("Wproawdzono błędny adres e-mail!");
            til.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(PasswordReset.this,"Wiadomość weryfikacyjna została wysłana.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(PasswordReset.this,"Coś poszło nie tak. Spróbuj ponownie później.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goBackMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}