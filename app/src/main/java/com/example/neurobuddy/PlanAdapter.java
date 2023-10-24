package com.example.neurobuddy;

import static android.content.Intent.getIntent;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurobuddy.Plan.Plan;
import com.example.neurobuddy.Plan.SpecificPlanActivity;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> plans;
    private int selectedDay;
    private String selectedDate, type = null;
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-d"));

    public void clearData() {
        plans.clear();
        notifyDataSetChanged();
    }

    public PlanAdapter(List<Plan> plans, int selectedDay, String selectedDate) {
        this.plans = plans;
        this.selectedDay = selectedDay;
        this.selectedDate = selectedDate;
    }

    public interface PlanAdapterListener {
        void fetchAndDisplayPlans(int dayOfWeek);
    }

    private PlanAdapterListener listener;

    public void setListener(PlanAdapterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan currentPlan = plans.get(position);

        holder.textTitle.setText(currentPlan.getTitle());
        holder.textDescription.setText(currentPlan.getDescription());

        if (currentPlan.getSelectedTime() != null) {
            holder.textTime.setText("Czas: " + currentPlan.getSelectedTime());
            holder.textTime.setVisibility(View.VISIBLE);
        } else {
            holder.textTime.setVisibility(View.GONE);
        }

        if (currentPlan.getSelectedDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            LocalDate planDate = LocalDate.parse(currentPlan.getSelectedDate(), formatter);

            holder.textDate.setText("Data: " + planDate.format(formatter));
            holder.textDate.setVisibility(View.VISIBLE);
        } else {
            holder.textDate.setVisibility(View.GONE);
        }

        if (currentPlan.getDuration() != null) {
            holder.durationText.setText("Czas trwania: " + currentPlan.getDuration());
            holder.durationText.setVisibility(View.VISIBLE);
        } else {
            holder.durationText.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            deletePlanFromDatabase(currentPlan);
        });

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(currentPlan.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCheckedStatus(currentPlan, isChecked);
        });

        holder.checkBox.setVisibility(View.VISIBLE);

        DayOfWeek todayDayOfWeek = LocalDate.now().getDayOfWeek();
        int adjustedDayOfWeek = (todayDayOfWeek.getValue());

        if (currentPlan.getType() != null && currentPlan.getSelectedDate() != null) {
            type = currentPlan.getType();
            String date = currentPlan.getSelectedDate();
            if (type.equals("Specific")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                LocalDate specificSelectedDate = LocalDate.parse(date, formatter);

                LocalDate today = LocalDate.now();

                Log.v("selectedDay", String.valueOf(selectedDay) + " : " + adjustedDayOfWeek);

                if ((specificSelectedDate.isAfter(today) || specificSelectedDate.equals(today))) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                }
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
        } else if (!selectedDate.equals(today) ||  !(adjustedDayOfWeek == selectedDay)) {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textTime, textDate, durationText;
        CheckBox checkBox;
        Button btnDelete;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textTime = itemView.findViewById(R.id.textTime);
            textDate = itemView.findViewById(R.id.textDate);
            durationText = itemView.findViewById(R.id.durationText);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void updateCheckedStatus(Plan currentPlan, boolean isChecked) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference plansRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("plans");

        plansRef.orderByChild("ndId").equalTo(currentPlan.getNdId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String planId = snapshot.getKey();

                            Log.v("planId", userId);
                            Log.v("planId", planId);

                            updateCheckedStatusInDatabase(planId, isChecked);

                            updatePoints(isChecked);
                            if (listener != null) {
                                listener.fetchAndDisplayPlans(selectedDay);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error querying database: " + databaseError.getMessage());
                    }
                });
    }

    private void updatePoints(boolean isChecked) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentPoints = dataSnapshot.getValue(Integer.class);

                    int updatedPoints = isChecked ? currentPoints + 15 : currentPoints - 15;

                    userRef.child("points").setValue(updatedPoints);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching points: " + databaseError.getMessage());
            }
        });
    }


    private void deletePlanFromDatabase(Plan currentPlan) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference plansRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("plans");

        plansRef.orderByChild("ndId").equalTo(currentPlan.getNdId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String planId = snapshot.getKey();

                            Log.v("planId", userId);
                            assert planId != null;
                            Log.v("planId", planId);

                            plansRef.child(planId).removeValue((databaseError, databaseReference) -> {
                                if (databaseError == null) {
                                    Log.v("Firebase", "Plan został usunięty");
                                    if (listener != null) {
                                        listener.fetchAndDisplayPlans(selectedDay);
                                    }
                                } else {
                                    Log.e("Firebase", "Error: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error querying database: " + databaseError.getMessage());
                    }
                });
    }

    private void updateCheckedStatusInDatabase(String planId, boolean isChecked) {
        if (planId != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference planRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("plans")
                    .child(planId);

            planRef.child("checked").setValue(isChecked, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    Log.v("Firebase", "Checked status updated successfully");
                } else {
                    Log.e("Firebase", "Error updating checked status: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("Firebase", "Error: planId is null");
        }
    }
}
