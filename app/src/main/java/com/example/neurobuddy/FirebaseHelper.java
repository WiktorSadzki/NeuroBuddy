package com.example.neurobuddy;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.neurobuddy.Plan.DataItem;
import com.example.neurobuddy.Plan.Plan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    public interface SavePlanCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public static <T extends DataItem> void saveDataToDb(String dataType, T dataItem, SavePlanCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        String itemId = userRef.child(dataType).push().getKey();
        DatabaseReference itemRef = userRef.child(dataType).child(itemId);

        itemRef.setValue(dataItem, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                callback.onSuccess();
            } else {
                callback.onFailure(databaseError.getMessage());
            }
        });
    }

    public static void deleteExistingPlans(String planType, Runnable callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        DatabaseReference plansRef = userRef.child("plans");

        plansRef.orderByChild("planType").equalTo(planType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot planSnapshot : dataSnapshot.getChildren()) {
                    planSnapshot.getRef().removeValue();
                }
                callback.run(); //Wykonaj wstawinie planów po usunięciu starych
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DeletePlans", "Error: " + databaseError.getMessage());
            }
        });
    }

    public static void deleteGrades(Runnable callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        DatabaseReference plansRef = userRef.child("grades");

        plansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot planSnapshot : dataSnapshot.getChildren()) {
                    planSnapshot.getRef().removeValue();
                }
                callback.run(); //Wykonaj wstawinie planów po usunięciu starych
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DeletePlans", "Error: " + databaseError.getMessage());
            }
        });
    }
}
