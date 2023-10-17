package com.example.neurobuddy.Plan;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CalendarView;
import android.widget.Spinner;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.example.neurobuddy.SpinnerUtil;
import com.example.neurobuddy.TabsActivity;

public class SpecificPlanActivity extends AppCompatActivity {

    private Button saveButton;
    private EditText planTitleEditText;
    private EditText planDescriptionEditText;
    private CalendarView calendarView;

    // Declare selectedDate as a field
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_plan);

        saveButton = findViewById(R.id.planSubmit);
        planTitleEditText = findViewById(R.id.planTitle);
        planDescriptionEditText = findViewById(R.id.planDescription);
        calendarView = findViewById(R.id.calendarView);

        Spinner activityTypeSpinner = findViewById(R.id.activityTypeSpinner);

        activityTypeSpinner.setSelection(2);

        SpinnerUtil.setupSpinner(
                this,
                activityTypeSpinner,
                R.array.activity_types,
                new SpinnerUtil.SpinnerItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position) {
                        updateRecyclerView(position);
                    }
                });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Assign the selected date to the field
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            }
        });

        saveButton.setOnClickListener(view -> savePlan());
    }

    private void savePlan() {
        String title = planTitleEditText.getText().toString();
        String description = planDescriptionEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(selectedDate)) {
            return;
        }

        // Call the FirebaseHelper method to save the plan to Firebase
        FirebaseHelper.savePlanToFirebase(title, description, selectedDate, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                // Data saved successfully
                Intent intent = new Intent(getApplicationContext(), TabsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle failure
                Log.e("Firebase", "Error saving data: " + errorMessage);
                // You might want to show a Toast or some other UI indication of the failure
            }
        });
    }


    private void updateRecyclerView(int selectedPosition) {
        switch (selectedPosition) {
            case 0:
                if (!isCurrentActivity(SpecificPlanActivity.class)) {
                    Intent intent = new Intent(this, SpecificPlanActivity.class);
                    startActivity(intent);
                }
                break;
            case 1:
                if (!isCurrentActivity(RoutineActivity.class)) {
                    Intent intent = new Intent(this, RoutineActivity.class);
                    startActivity(intent);
                    break;
                }
            default:
                // Update RecyclerView logic for other cases
                break;
        }
    }

    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().equals(activityClass);
    }
}
