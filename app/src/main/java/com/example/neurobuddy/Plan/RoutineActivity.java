package com.example.neurobuddy.Plan;

import static com.example.neurobuddy.SpinnerUtil.isCurrentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.example.neurobuddy.SpinnerUtil;
import com.example.neurobuddy.TabsActivity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class RoutineActivity extends AppCompatActivity {

    private final Map<String, Boolean> selectedDaysMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        Spinner activityTypeSpinner = findViewById(R.id.activityTypeSpinner);
        activityTypeSpinner.post(() -> activityTypeSpinner.setSelection(1));
        SpinnerUtil.setupSpinner(
                this,
                activityTypeSpinner,
                R.array.activity_types,
                position -> updateRecyclerView(position)
        );

        checkAndRequestPermissions();

        handleCheckboxChanges();

        findViewById(R.id.cancel).setOnClickListener(v -> {
            startActivity(new Intent(this, TabsActivity.class));
            finish();
        });

        findViewById(R.id.planSubmit).setOnClickListener(v -> {
            savePlanToFirebase();
            toTabsActivity();
        });
    }

    private void updateRecyclerView(int selectedPosition) {
        switch (selectedPosition) {
            case 0:
                if (!isCurrentActivity(this, SpecificPlanActivity.class)) {
                    Intent intent = new Intent(this, SpecificPlanActivity.class);
                    startActivity(intent);
                }
                break;
            case 2:
                if (!isCurrentActivity(this, SchoolplanUrl.class)) {
                    Intent intent = new Intent(this, SchoolplanUrl.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    private void handleCheckboxChanges() {
        CheckBox mondayCheckBox = findViewById(R.id.monday);
        CheckBox tuesdayCheckBox = findViewById(R.id.tuesday);
        CheckBox wednesdayCheckBox = findViewById(R.id.wednesday);
        CheckBox thursdayCheckBox = findViewById(R.id.thursday);
        CheckBox fridayCheckBox = findViewById(R.id.friday);
        CheckBox saturdayCheckBox = findViewById(R.id.saturday);
        CheckBox sundayCheckBox = findViewById(R.id.sunday);

        mondayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("1", isChecked));
        tuesdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("2", isChecked));
        wednesdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("3", isChecked));
        thursdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("4", isChecked));
        fridayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("5", isChecked));
        saturdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("6", isChecked));
        sundayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> selectedDaysMap.put("7", isChecked));
    }
    public Map<String, Boolean> getSelectedDaysMap() {
        return selectedDaysMap;
    }

    private void savePlanToFirebase() {
        EditText planTitleEditText = findViewById(R.id.planTitle);
        EditText planDescriptionEditText = findViewById(R.id.planDescription);
        String title = planTitleEditText.getText().toString();
        String description = planDescriptionEditText.getText().toString();

        TimePicker timePicker = findViewById(R.id.timePicker);
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String selectedTime = String.format("%02d:%02d", hour, minute);

        StringBuilder selectedDaysStringBuilder = new StringBuilder();

        for (Map.Entry<String, Boolean> entry : selectedDaysMap.entrySet()) {
            String day = entry.getKey();
            boolean isChecked = entry.getValue();

            if (isChecked) {
                selectedDaysStringBuilder.append(day);
            }
        }

        String selectedDaysString = selectedDaysStringBuilder.toString();

        Plan plan = new Plan();
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setSelectedDays(selectedDaysString);
        plan.setSelectedTime(selectedTime);
        plan.setChecked(false);
        plan.setNdId(UUID.randomUUID().toString());
        plan.setType("Routine");

        Spinner planTypeSpinner = findViewById(R.id.planType);
        plan.setPlanType(translateToEnglish(planTypeSpinner.getSelectedItem().toString()));

        Spinner durationSpinner = findViewById(R.id.durationSpinner);
        String selectedDuration = durationSpinner.getSelectedItem().toString();
        plan.setDuration(convertDurationToMinutes(selectedDuration));
        FirebaseHelper.saveDataToDb("plans", plan, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(RoutineActivity.this, "Plan dodany poprawnie", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RoutineActivity.this, "Nie udało się dodać planu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertDurationToMinutes(String duration) {
        if ("Not specified".equals(duration) || "Nieokreślony".equals(duration)) {
            return null;
        } else if ("5 minutes".equals(duration) || "5 minut".equals(duration)) {
            return "5";
        } else if ("15 minutes".equals(duration) || "15 minut".equals(duration)) {
            return "15";
        } else if ("30 minutes".equals(duration) || "30 minut".equals(duration)) {
            return "30";
        } else if ("1 hour".equals(duration) || "1 godzina".equals(duration)) {
            return "60";
        } else if ("2 hours".equals(duration) || "2 godziny".equals(duration)) {
            return "120";
        } else if ("3 hours".equals(duration) || "3 godziny".equals(duration)) {
            return "180";
        } else if ("4 hours".equals(duration) || "4 godziny".equals(duration)) {
            return "240";
        } else {
            return null;
        }
    }

    private static String translateToEnglish(String polishValue) {
        switch (polishValue) {
            case "Nieokreślony":
                return "Not specified";
            case "Nauka":
                return "Learning";
            case "Praca":
                return "Work";
            case "Ćwiczenia":
                return "Exercise";
            case "Inne":
                return "Other";
            case "Sprawdzian":
                return "Exam";
            default:
                return polishValue;
        }
    }

    private void toTabsActivity(){
        Intent intent = new Intent(getApplicationContext(), TabsActivity.class);
        startActivity(intent);
    }
}

