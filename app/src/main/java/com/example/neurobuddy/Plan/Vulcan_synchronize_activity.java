package com.example.neurobuddy.Plan;

import static com.example.neurobuddy.FirebaseHelper.deleteExistingPlans;
import static com.example.neurobuddy.FirebaseHelper.deleteGrades;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class Vulcan_synchronize_activity extends AppCompatActivity {

    private EditText ipAddressEditText;
    private Button confirmButton;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vulcan_synchronize);

        ipAddressEditText = findViewById(R.id.ipAdress);
        confirmButton = findViewById(R.id.confirm_button);
        textView = findViewById(R.id.textView);

        confirmButton.setOnClickListener(v -> {
            String serverIP = ipAddressEditText.getText().toString();
            int serverPort = 5000;

            new Thread(() -> {
                try {
                    Socket socket = new Socket(serverIP, serverPort);

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Dane otrzymano pomyślnie");

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();

                    Log.v("HttpRequestTask", response);

                    String[] parts = response.split("\\}\\{");

                    JSONArray jsonExamsArray = new JSONArray();
                    JSONArray jsonGradesArray = new JSONArray();

                    for (String part : parts) {
                        try {
                            if (!part.contains("{")) {
                                part = "{" + part;
                            }
                            if (!part.contains("}")) {
                                part = part + "}";
                            }
                            Log.v("HttpRequestTask", part);
                            JSONObject jsonObject = new JSONObject(part);
                            if(part.contains("\"key\": \"exams\"")) {
                                jsonExamsArray.put(jsonObject);
                            } else if (part.contains("\"key\": \"grades\"")) {
                                jsonGradesArray.put(jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.v("HttpRequestTask", String.valueOf(jsonExamsArray));


                    try {
                        for (int i = 0; i < jsonExamsArray.length(); i++) {
                            JSONObject jsonObject = jsonExamsArray.getJSONObject(i);
                            String title = jsonObject.getString("title");
                            String description = jsonObject.getString("description");
                            String selectedDate = jsonObject.getString("selectedDate");
                            String selectedDays = jsonObject.getString("selectedDays");
                            String ndId = jsonObject.getString("ndId");
                            String planType = "VulcanExam";

                            deleteExistingPlans(planType, () -> {
                                savePlans(title, description, selectedDate, selectedDays, ndId, planType);
                            });
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        for (int i = 0; i < jsonGradesArray.length(); i++) {
                            JSONObject jsonObject = jsonGradesArray.getJSONObject(i);
                            int grade = Integer.parseInt(jsonObject.getString("grade"));
                            String description = jsonObject.getString("description");
                            String selectedDate = jsonObject.getString("selectedDate");
                            String type = jsonObject.getString("type");
                            String subject = jsonObject.getString("subject");
                            String ndId = jsonObject.getString("ndId");
                            float value = Float.parseFloat(jsonObject.getString("value"));

                            deleteGrades( () -> {
                                saveGrades(grade, description, selectedDate, type, subject, ndId, value);
                            });
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    socket.close();
                } catch (Exception e) {
                    Log.v("HttpRequestTask", "Error: " + String.valueOf(e));
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void savePlans(String title, String description, String selectedDate, String selectedDays, String ndID, String planType) {
        Plan plan = new Plan();
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setSelectedDate(selectedDate);
        plan.setSelectedDays(selectedDays);
        plan.setChecked(false);
        plan.setNdId(ndID);
        plan.setPlanType(planType);

        FirebaseHelper.saveDataToDb("plans", plan, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Vulcan_synchronize_activity.this, "Dane zapisane poprawnie", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(Vulcan_synchronize_activity.this, "Nie udało się zapisać danych: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveGrades(int grade, String description, String selectedDate, String type, String subject, String ndId, float value) {
        Grade grades = new Grade();
        grades.setGrade(grade);
        grades.setDescription(description);
        grades.setSelectedDate(selectedDate);
        grades.setType(type);
        grades.setSubject(subject);
        grades.setNdId(ndId);
        grades.setValue(value);
        FirebaseHelper.saveDataToDb("grades", grades, new FirebaseHelper.SavePlanCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Vulcan_synchronize_activity.this, "Dane zapisane poprawnie", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(Vulcan_synchronize_activity.this, "Nie udało się zapisać danych: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
