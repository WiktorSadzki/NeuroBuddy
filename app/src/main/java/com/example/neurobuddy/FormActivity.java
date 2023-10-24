package com.example.neurobuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neurobuddy.Plan.DataItem;
import com.example.neurobuddy.Plan.Plan;
import com.example.neurobuddy.Plan.SpecificPlanActivity;
import com.google.firebase.auth.FirebaseAuth;

public class FormActivity extends AppCompatActivity {

    private EditText howManyHoursEditText;
    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private TextView timeRangeTextView;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        howManyHoursEditText = findViewById(R.id.howManyHours);
        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        timeRangeTextView = findViewById(R.id.timeRangeTextView);
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFormDataToFirebase();
            }
        });
    }

    private void saveFormDataToFirebase() {
        String nameString = howManyHoursEditText.getText().toString();
        int name;

        try {
            name = Integer.parseInt(nameString); // Zamień wprowadzony czas na int
        } catch (NumberFormatException e) {
            Log.v("Error", "Error, invalid value: " + e);
            return;
        }
        int startHour = startTimePicker.getCurrentHour();
        int startMinute = startTimePicker.getCurrentMinute();
        int endHour = endTimePicker.getCurrentHour();
        int endMinute = endTimePicker.getCurrentMinute();

        if(startHour*60 + startMinute >= endHour*60 + endMinute){
            Toast.makeText(FormActivity.this, "Czas początkowy nie może być większy od czasu końcowego!", Toast.LENGTH_SHORT).show();
            return;
        }

        FormData formData = new FormData(name, startHour, startMinute, endHour, endMinute);

        FirebaseHelper.saveDataToDb("forms", formData, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(FormActivity.this, "Dane poprawie zapisane", Toast.LENGTH_SHORT).show();
                toTabsActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });
    }

    private void toTabsActivity(){
        Intent intent = new Intent(getApplicationContext(), TabsActivity.class);
        startActivity(intent);
    }
}
