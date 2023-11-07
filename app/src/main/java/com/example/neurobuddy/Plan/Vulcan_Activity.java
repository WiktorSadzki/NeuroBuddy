package com.example.neurobuddy.Plan;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurobuddy.Plan.Grade;
import com.example.neurobuddy.Plan.GradeAdapter;
import com.example.neurobuddy.Plan.Vulcan_synchronize_activity;
import com.example.neurobuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vulcan_Activity extends AppCompatActivity {

    private RecyclerView recyclerViewGrades;
    private GradeAdapter gradeAdapter;
    private List<Grade> gradeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vulcan);

        recyclerViewGrades = findViewById(R.id.recyclerViewGrades);
        recyclerViewGrades.setLayoutManager(new LinearLayoutManager(this));
        gradeList = new ArrayList<>();
        gradeAdapter = new GradeAdapter(gradeList);
        recyclerViewGrades.setAdapter(gradeAdapter);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.child("grades").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, List<Grade>> subjectGradeMap = new HashMap<>();

                    for (DataSnapshot gradeSnapshot : dataSnapshot.getChildren()) {
                        Grade grade = gradeSnapshot.getValue(Grade.class);

                        String subject = grade.getSubject();
                        if (!subjectGradeMap.containsKey(subject)) {
                            subjectGradeMap.put(subject, new ArrayList<>());
                        }
                        subjectGradeMap.get(subject).add(grade);
                    }

                    for (Map.Entry<String, List<Grade>> entry : subjectGradeMap.entrySet()) {
                        String subject = entry.getKey();
                        List<Grade> grades = entry.getValue();

                        Log.d("Subject", subject);

                        for (Grade grade : grades) {
                            Log.d("Grade", "Grade: " + grade.getGrade());
                        }

                        gradeList.addAll(grades);
                    }
                    gradeAdapter.notifyDataSetChanged();
                } else { //Jeżeli nie zarejestrowano użytkownika przejdź do formularza
                    Intent intent = new Intent(Vulcan_Activity.this, Vulcan_synchronize_activity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
            }
        });
    }
}
