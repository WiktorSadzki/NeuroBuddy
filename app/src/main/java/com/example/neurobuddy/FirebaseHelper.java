package com.example.neurobuddy;

import com.example.neurobuddy.Plan.Plan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;

public class FirebaseHelper {

    public interface SavePlanCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public static void savePlanToFirebase(String title, String description, String selectedDate, SavePlanCallback callback) {
        // Create a Plan object
        Plan plan = new Plan();
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setSelectedDate(selectedDate);

        // Save the plan to Firebase under the user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        String planId = userRef.child("plans").push().getKey();
        DatabaseReference planRef = userRef.child("plans").child(planId);

        planRef.setValue(plan, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    // Data saved successfully
                    callback.onSuccess();
                } else {
                    // Error occurred
                    callback.onFailure(databaseError.getMessage());
                }
            }
        });
    }
}
