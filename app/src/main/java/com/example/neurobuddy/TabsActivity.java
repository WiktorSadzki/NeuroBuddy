package com.example.neurobuddy;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neurobuddy.Login.MainActivity;
import com.example.neurobuddy.Plan.SpecificPlanActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TabsActivity extends AppCompatActivity implements ValueEventListener {

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;

    TextView no_account;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        no_account = findViewById(R.id.account);

        Button sign_out = findViewById(R.id.signOutButton);
        sign_out.setOnClickListener(this::onClick);

        Button add_plan = findViewById(R.id.addPlan);
        add_plan.setOnClickListener(this::onClick);

        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() == null) {
                no_account.setText("Użytkownik nie jest zalogowany!");
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("login").getValue(String.class);
                            no_account.setText("Witaj " + username + "!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TabsActivity.this,
                                "Wystąpił błąd! Spróbuj ponownie później.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 1000);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.signOutButton) {
            logOutUser();
        } else if (id == R.id.addPlan) {
            toSpecificPlanActivity();
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void logOutUser() {
        if (mAuth.getCurrentUser() == null) {
            toMainActivity();
        } else {
            FirebaseAuth.getInstance().signOut();
            toMainActivity();
        }
    }

    private void toSpecificPlanActivity() {
        Intent intent = new Intent(this, SpecificPlanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        // Data changed, update UI if needed
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        // Handle errors from database
        Toast.makeText(TabsActivity.this,
                "Wystąpił błąd! Spróbuj ponownie później.",
                Toast.LENGTH_LONG).show();
    }
}