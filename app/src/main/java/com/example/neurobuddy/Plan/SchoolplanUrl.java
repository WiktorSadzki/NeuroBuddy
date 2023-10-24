package com.example.neurobuddy.Plan;

import static com.example.neurobuddy.FirebaseHelper.deleteExistingPlans;
import static com.example.neurobuddy.SpinnerUtil.isCurrentActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neurobuddy.FirebaseHelper;
import com.example.neurobuddy.R;
import com.example.neurobuddy.SpinnerUtil;
import com.example.neurobuddy.TabsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SchoolplanUrl extends AppCompatActivity {

    private EditText planUrlEditText;
    private EditText planClassEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schoolplan_url);

        planUrlEditText = findViewById(R.id.planUrl);
        planClassEditText = findViewById(R.id.planClass);

        Button confirmButton = findViewById(R.id.planConfirm);
        Button cancelButton = findViewById(R.id.planCancel);

        Spinner activityTypeSpinner = findViewById(R.id.activityTypeSpinner);
        activityTypeSpinner.post(() -> activityTypeSpinner.setSelection(2));
        SpinnerUtil.setupSpinner(
                this,
                activityTypeSpinner,
                R.array.activity_types,
                position -> updateRecyclerView(position)
        );

        confirmButton.setOnClickListener(view -> {
            onConfirmButtonClick();
        });

        cancelButton.setOnClickListener(view -> finish());
    }

    private void updateRecyclerView(int selectedPosition) {
        switch (selectedPosition) {
            case 0:
                if (!isCurrentActivity(this, SpecificPlanActivity.class)) {
                    Intent intent = new Intent(this, SpecificPlanActivity.class);
                    startActivity(intent);
                }
                break;
            case 1:
                if (!isCurrentActivity(this, RoutineActivity.class)) {
                    Intent intent = new Intent(this, RoutineActivity.class);
                    startActivity(intent);
                }
                break;

        }
    }

    private class DownloadPlanTask extends AsyncTask<String, Void, Document> {
        private String planSchoolClassValue;
        private String link;

        public DownloadPlanTask(String planSchoolClassValue, String link) {
            this.planSchoolClassValue = planSchoolClassValue;
            this.link = link;
        }

        @Override
        protected Document doInBackground(String... params) {
            try {
                String link = params[0];
                return Jsoup.connect(link).get();
            } catch (IOException e) {
                Log.e("AsyncTask", "Error fetching document", e);
                return null;
            }
        }

        protected void onPostExecute(Document result) {
            if (result != null) {
                Log.v("week", String.valueOf(result));
                Elements links = result.select("li:contains(" + planSchoolClassValue + ")>a");
                Log.v("week", planSchoolClassValue);
                link = link.replace("lista.html", "");
                String linkPlan = link + links.attr("href");
                Log.v("week", linkPlan);

                new DownloadPlanDetailTask(planSchoolClassValue, linkPlan, "UrlSchoolPlan").execute(linkPlan);
            } else {
                Toast.makeText(SchoolplanUrl.this, "Error fetching document", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadPlanDetailTask extends AsyncTask<String, Void, Document> {
        private String planSchoolClassValue;
        private Map<String, String> selectedDay;
        private String planType;

        public DownloadPlanDetailTask(String planSchoolClassValue, String linkPlan, String planType) {
            this.planSchoolClassValue = planSchoolClassValue;
            this.planType = planType;
        }

        @Override
        protected Document doInBackground(String... params) {
            try {
                String linkPlan = params[0];
                return Jsoup.connect(linkPlan).get();
            } catch (IOException e) {
                Log.e("AsyncTask", "Error fetching document", e);
                return null;
            }
        }

        protected void onPostExecute(Document result) {
            if (result != null) {
                Log.v("week", String.valueOf(result));
                Elements time_v = result.getElementsByClass("g");
                String[] time = new String[time_v.size()];
                for (int i = 0; i < time_v.size(); i++) {
                    time[i] = time_v.get(i).text();
                }

                String[] extractedTimes = new String[time.length];
                for (int i = 0; i < time.length; i++) {
                    String[] parts = time[i].split("-");
                    extractedTimes[i] = parts[0].trim();
                }

                Log.v("week", Arrays.toString(extractedTimes));

                Element table = result.select("table").get(2);
                Elements rows = table.select("tr");

                Map<String, String> mon = new TreeMap<>(new TimeComparator());
                Map<String, String> tue = new TreeMap<>(new TimeComparator());
                Map<String, String> wen = new TreeMap<>(new TimeComparator());
                Map<String, String> thu = new TreeMap<>(new TimeComparator());
                Map<String, String> fri = new TreeMap<>(new TimeComparator());
                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");
                    if (!cols.get(2).text().isEmpty()) {
                        mon.put(extractedTimes[i - 1], cols.get(2).text());
                    }

                    if (!cols.get(3).text().isEmpty()) {
                        tue.put(extractedTimes[i - 1], cols.get(3).text());
                    }

                    if (!cols.get(4).text().isEmpty()) {
                        wen.put(extractedTimes[i - 1], cols.get(4).text());
                    }

                    if (!cols.get(5).text().isEmpty()) {
                        thu.put(extractedTimes[i - 1], cols.get(5).text());
                    }

                    if (!cols.get(6).text().isEmpty()) {
                        fri.put(extractedTimes[i - 1], cols.get(6).text());
                    }
                }
                Log.v("week", mon.toString());

                //Usuń stare plany o typie "UrlSchoolPlan"
                deleteExistingPlans(planType, () -> {
                    // Zapisz plany
                    savePlans(mon, "1", planType);
                    savePlans(tue, "2", planType);
                    savePlans(wen, "3", planType);
                    savePlans(thu, "4", planType);
                    savePlans(fri, "5", planType);
                });

            } else {
                Toast.makeText(SchoolplanUrl.this, "Error fetching document", Toast.LENGTH_SHORT).show();
            }
        }

        private void savePlans(Map<String, String> day, String dayName, String planType) {
            for (Map.Entry<String, String> entry : day.entrySet()) {
                String title = entry.getValue();
                String time = entry.getKey();

                Plan plan = new Plan();
                plan.setTitle(title);
                plan.setSelectedDays(dayName);
                plan.setSelectedTime(time);
                plan.setChecked(false);
                plan.setNdId(UUID.randomUUID().toString());
                plan.setPlanType(planType);
                plan.setDuration(String.valueOf(45));

                FirebaseHelper.saveDataToDb("plans", plan, new FirebaseHelper.SavePlanCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(SchoolplanUrl.this, "Plan dodany poprawnie", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(SchoolplanUrl.this, "Nie udało się dodać planu: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void onConfirmButtonClick() {
        String baseLink = planUrlEditText.getText().toString();
        String classValue = planClassEditText.getText().toString();

        baseLink = baseLink.contains("index.html/") ? baseLink.replace("index.html/", "") : baseLink;
        baseLink = baseLink.contains("index.html") ? baseLink.replace("index.html", "") : baseLink;
        String link = baseLink.endsWith("lista.html") ? baseLink : baseLink + "lista.html";

        new DownloadPlanTask(classValue, link).execute(link);

        toTabsActivity();
    }

    private class TimeComparator implements Comparator<String> {
        @Override
        public int compare(String time1, String time2) {
            try {
                return getTime(time1).compareTo(getTime(time2));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }

        private Date getTime(String time) throws ParseException {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.parse(time);
        }
    }

    private void toTabsActivity() {
        Intent intent = new Intent(getApplicationContext(), TabsActivity.class);
        startActivity(intent);
    }
}
