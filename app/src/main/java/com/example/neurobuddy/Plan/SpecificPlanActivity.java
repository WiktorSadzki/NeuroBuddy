package com.example.neurobuddy.Plan;

import static com.example.neurobuddy.SpinnerUtil.isCurrentActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.example.neurobuddy.SpinnerUtil;
import com.example.neurobuddy.TabsActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class SpecificPlanActivity extends AppCompatActivity {

    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_plan);

        Button saveButton = findViewById(R.id.planSubmit);
        Button cancelButton = findViewById(R.id.cancel);

        CalendarView calendarView = findViewById(R.id.calendarView);

        Spinner activityTypeSpinner = findViewById(R.id.activityTypeSpinner);
        activityTypeSpinner.post(() -> activityTypeSpinner.setSelection(0));
        SpinnerUtil.setupSpinner(
                this,
                activityTypeSpinner,
                R.array.activity_types,
                position -> updateRecyclerView(position)
        );

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                Log.v("specific", selectedDate);
            }
        });

        findViewById(R.id.cancel).setOnClickListener(v -> {
            startActivity(new Intent(this, TabsActivity.class));
            finish();
        });

        findViewById(R.id.planSubmit).setOnClickListener(v -> {
            try {
                savePlan();
                toTabsActivity();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void savePlan() throws ParseException {
        EditText planTitleEditText = findViewById(R.id.planTitle);
        EditText planDescriptionEditText = findViewById(R.id.planDescription);
        String title = planTitleEditText.getText().toString();
        String description = planDescriptionEditText.getText().toString();
        Log.v("specific", title);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(selectedDate)) {
            return;
        }

        TimePicker timePicker = findViewById(R.id.timePicker);
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String selectedTime = String.format("%02d:%02d", hour, minute);
        Log.v("specific", selectedTime);

        SimpleDateFormat sdf = new SimpleDateFormat("d-M-yyyy");
        Date date = sdf.parse(selectedDate);
        Log.v("specific", String.valueOf(date));

        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.setFirstDayOfWeek(Calendar.MONDAY);

        int dayOfWeek = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        Plan plan = new Plan(title, description, String.valueOf(dayOfWeek), selectedTime, String.valueOf(selectedDate));

        Spinner durationSpinner = findViewById(R.id.durationSpinner);
        String selectedDuration = durationSpinner.getSelectedItem().toString();
        plan.setDuration(convertDurationToMinutes(selectedDuration));

        Spinner planTypeSpinner = findViewById(R.id.planType);
        plan.setPlanType(translateToEnglish(planTypeSpinner.getSelectedItem().toString()));

        FirebaseHelper.saveDataToDb("plans", plan, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SpecificPlanActivity.this, "Plan dodany poprawnie", Toast.LENGTH_SHORT).show();
                toTabsActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(SpecificPlanActivity.this, "Nie udało się dodać planu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView(int selectedPosition) {
        switch (selectedPosition) {
            case 1:
                if (!isCurrentActivity(this, RoutineActivity.class)) {
                    Intent intent = new Intent(this, RoutineActivity.class);
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
