package com.example.neurobuddy.Plan;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurobuddy.Login.RegisterUsers;
import com.example.neurobuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private ImageView stImageView, ndImageView, rdImageView;
    private TextView stText, ndText, rdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        stImageView = findViewById(R.id.stImageView);
        stText = findViewById(R.id.stText);
        ndImageView = findViewById(R.id.ndImageView);
        ndText = findViewById(R.id.ndText);
        rdImageView = findViewById(R.id.rdimageView);
        rdText = findViewById(R.id.rdText);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = userRef.orderByChild("points").limitToLast(3);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d("Firebase", "Snapshot: " + userSnapshot.toString());
                    try {
                        RegisterUsers user = userSnapshot.getValue(RegisterUsers.class);
                        if (user != null) {
                            loadUserDetails(i, user);
                            i++;
                        } else {
                            Log.e("Firebase", "User is null for snapshot: " + userSnapshot.toString());
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "Error converting snapshot to RegisterUsers: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });

        query = userRef.orderByChild("points").limitToLast(10);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<RegisterUsers> userList = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        RegisterUsers user = userSnapshot.getValue(RegisterUsers.class);
                        if (user != null) {
                            userList.add(user);
                        } else {
                            Log.e("Firebase", "User is null for snapshot: " + userSnapshot.toString());
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "Error converting snapshot to RegisterUsers: " + e.getMessage());
                    }
                }

                Collections.reverse(userList);

                UserAdapter userAdapter = new UserAdapter(userList);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void loadUserDetails(int position, RegisterUsers user) {
        switch (position) {
            case 2:
                stText.setText(user.getLogin());
                loadProfilePicture(user.getPath(), stImageView);
                break;
            case 1:
                ndText.setText(user.getLogin());
                loadProfilePicture(user.getPath(), ndImageView);
                break;
            case 0:
                rdText.setText(user.getLogin());
                loadProfilePicture(user.getPath(), rdImageView);
                break;
        }
    }

    private void loadProfilePicture(String path, ImageView imageView) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(path);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri).resize(360, 360).centerCrop().into(imageView);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase", "Error loading profile picture: " + exception.getMessage());
        });
    }
}
