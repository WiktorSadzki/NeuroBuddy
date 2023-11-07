package com.example.neurobuddy;

import static android.app.PendingIntent.getActivity;
import static com.google.common.collect.Iterables.size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.Manifest;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neurobuddy.Login.MainActivity;
import com.example.neurobuddy.Notes.Notes;
import com.example.neurobuddy.Plan.CurrentDate;
import com.example.neurobuddy.Plan.Plan;
import com.example.neurobuddy.Plan.RankingActivity;
import com.example.neurobuddy.Plan.SpecificPlanActivity;
import com.example.neurobuddy.Plan.Vulcan_Activity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.*;

public class TabsActivity extends AppCompatActivity implements ValueEventListener {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;

    TextView account, week_text;
    int dayOfTheWeek, weekOfYear, selectedDay;
    String firstDayOfWeekStr, lastDayOfWeekStr;
    String week_string;
    LocalDate firstDayOfWeek;
    LocalDate lastDayOfWeek;
    String selectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-d"));

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        account = findViewById(R.id.account);

        week_text = findViewById(R.id.week);

        Button add_plan = findViewById(R.id.addPlan);
        add_plan.setOnClickListener(this::onClick);

        scheduleLearningPlanJobService();
        checkAndUpdatePlansOnceADay();

        checkAndRequestNotificationPermission();

        findViewById(R.id.dropdown_menu).setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.actions, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.vulcan_button) {
                    toVulcan();
                    return true;
                }else if (itemId == R.id.notes_button) {
                    toNotes();
                    return true;
                } else if (itemId == R.id.log_out_button) {
                    logOutUser();
                    return true;
                }else if (itemId == R.id.ranking) {
                    toRanking();
                    return true;
                } else if (itemId == R.id.emotions) {
                    toSignActivity();
                    return true;
                } else {
                    return false;
                }
            });
            popup.show();
        });

        Button calendarButton = findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(v -> openCalendar());

        CurrentDate c = new CurrentDate();
        c.initializeDay();
        c.initializeWeek();

        dayOfTheWeek = CurrentDate.dayOfWeek;
        weekOfYear = CurrentDate.weekOfYear;

        selectedDay = dayOfTheWeek;

        Log.v("dayOfTheWeek", String.valueOf(dayOfTheWeek));

        firstDayOfWeek = CurrentDate.firstDayOfWeek;
        lastDayOfWeek = CurrentDate.lastDayOfWeek;

        week_string = firstDayOfWeek + " - " + lastDayOfWeek;

        week_text.setText(week_string);

        Log.v("week", String.valueOf(dayOfTheWeek));
        Log.v("week", String.valueOf(firstDayOfWeek));
        Log.v("week", String.valueOf(lastDayOfWeek));
        Log.v("week", String.valueOf(week_string));

        RadioGroup radioGroup = findViewById(R.id.DaysOfTheWeek);
        updateRadioButtonSelection(radioGroup, dayOfTheWeek);

        fetchAndDisplayPlans(dayOfTheWeek);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedDay = mapSelectedDayFromRadioButton(checkedId);
            fetchAndDisplayPlans(selectedDay);
        });

        List<Plan> plansForToday = new ArrayList<>();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        // Jeżeli użytkownik nie wypełnił formularza dotyczącego godzin nauki, przekieruj go do niego
        userRef.child("forms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean formExists = dataSnapshot.exists();

                if (!formExists) {
                    Intent intent = new Intent(getApplicationContext(), FormActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TabsActivity.this,
                        "Wystąpił błąd! Spróbuj ponownie później.",
                        Toast.LENGTH_LONG).show();
            }
        });


        userRef.child("plans").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot planSnapshot : snapshot.getChildren()) {
                    Plan plan = planSnapshot.getValue(Plan.class);
                    if (plan != null && plan.getSelectedDays() != null && plan.getSelectedDays().contains(String.valueOf(dayOfTheWeek))) {
                        if (isPlanWithinSelectedWeek(plan)) {
                            plansForToday.add(plan);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TabsActivity.this,
                        "Wystąpił błąd! Spróbuj ponownie później.",
                        Toast.LENGTH_LONG).show();
            }
        });

        scheduleNotifications(plansForToday);

        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() == null) {
                account.setText("Użytkownik nie jest zalogowany!");
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("login").getValue(String.class);
                            account.setText("Witaj " + username + "!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TabsActivity.this,
                                "Wystąpił błąd! Spróbuj ponownie później.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 1000);
    }

    private void scheduleNotifications(List<Plan> plans) {
        for (Plan plan : plans) {
            String time = plan.getSelectedTime();
            if (time != null) {
                LocalTime notificationTime = LocalTime.parse(time);

                scheduleNotification(plan.getTitle(), notificationTime);
            }
        }
    }

    private void checkAndRequestNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with sending notifications
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Permission denied. Notifications may not work properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndScheduleLearningPlanJobService() {
        Intent serviceIntent = new Intent(this, LearningPlanJobIntentService.class);
        LearningPlanJobIntentService.enqueueWork(this, serviceIntent);
    }

    private void scheduleLearningPlanJobService() {
        checkAndScheduleLearningPlanJobService();
    }

    private void scheduleNotification(String title, LocalTime notificationTime) {
        createNotification(title, notificationTime);

        Log.d("ScheduledNotification", "Scheduled notification for " + title + " at " + notificationTime);
    }

    private void createNotification(String title, LocalTime notificationTime) {
        Intent intent = new Intent(this, TabsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Czas na")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Schedule the notification at the specified time
        long futureInMillis = calculateNotificationTime(notificationTime);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE));
    }

    private long calculateNotificationTime(LocalTime notificationTime) {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(today, notificationTime);
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());

        return zonedDateTime.toInstant().toEpochMilli();
    }

    private void checkAndUpdatePlansOnceADay() {
        String lastCheckDate = getLastCheckDate();

        String currentDate = getCurrentDate();

        if (!currentDate.equals(lastCheckDate)) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            DatabaseReference plansRef = userRef.child("plans");

            plansRef.orderByChild("type").startAt("Routine").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot planSnapshot : dataSnapshot.getChildren()) {
                        Plan plan = planSnapshot.getValue(Plan.class);
                        if (plan != null) {
                            String selectedDate = plan.getSelectedDate();

                            if (selectedDate == null) {
                                planSnapshot.getRef().child("checked").setValue(false);
                            }
                        }
                    }

                    saveLastCheckDate(currentDate);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("UpdatePlans", "Error: " + databaseError.getMessage());
                }
            });
        }
    }

    private String getLastCheckDate() {
        return getSharedPreferences("PlansCheckPrefs", MODE_PRIVATE)
                .getString("lastCheckDate", "");
    }

    private void saveLastCheckDate(String date) {
        getSharedPreferences("PlansCheckPrefs", MODE_PRIVATE)
                .edit()
                .putString("lastCheckDate", date)
                .apply();
    }

    private void fetchAndDisplayPlans(int selectedDay) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.child("plans").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Plan> plansForSelectedDay = new ArrayList<>();
                for (DataSnapshot planSnapshot : snapshot.getChildren()) {
                    Plan plan = planSnapshot.getValue(Plan.class);
                    if (plan != null && plan.getSelectedDays() != null && plan.getSelectedDays().contains(String.valueOf(selectedDay))) {
                        if (isPlanWithinSelectedWeek(plan)) {
                            plansForSelectedDay.add(plan);
                        }
                    }
                }
                Collections.sort(plansForSelectedDay, new Comparator<Plan>() {
                    @Override
                    public int compare(Plan plan1, Plan plan2) {
                        String time1 = plan1.getSelectedTime();
                        String time2 = plan2.getSelectedTime();

                        int minutes1 = convertTimeToMinutes(time1);
                        int minutes2 = convertTimeToMinutes(time2);

                        return Integer.compare(minutes1, minutes2);
                    }
                });

                displayPlansInRecyclerView(plansForSelectedDay);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TabsActivity.this,
                        "Wystąpił błąd! Spróbuj ponownie później.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int convertTimeToMinutes(String time) {
        if (time == null) return 0;

        String[] parts = time.split(":");
        if (parts.length != 2) return 0;

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        return hours * 60 + minutes;
    }

    private boolean isPlanWithinSelectedWeek(Plan plan) {
        if(plan.getSelectedDate()==null)
            return true;
        String startDateStr = plan.getSelectedDate();
        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-d"));

        return !startDate.isBefore(firstDayOfWeek) && !startDate.isAfter(lastDayOfWeek);
    }


    private int mapSelectedDayFromRadioButton(int checkedId) {
        if (checkedId == R.id.Monday) {
            return 1;
        } else if (checkedId == R.id.Tuesday) {
            return 2;
        } else if (checkedId == R.id.Wednesday) {
            return 3;
        } else if (checkedId == R.id.Thursday) {
            return 4;
        } else if (checkedId == R.id.Friday) {
            return 5;
        } else if (checkedId == R.id.Saturday) {
            return 6;
        } else if (checkedId == R.id.Sunday) {
            return 7;
        } else {
            return -1;
        }
    }

    private void displayPlansInRecyclerView(List<Plan> plans) {
        RecyclerView regularRecyclerView = findViewById(R.id.plans);
        RecyclerView checkedRecyclerView = findViewById(R.id.checkedPlans);
        RecyclerView overdueRecyclerView = findViewById(R.id.overduePlans);

        TextView plansText = findViewById(R.id.plansText);
        TextView overduePlansText = findViewById(R.id.overduePlansText);
        TextView checkedPlansText = findViewById(R.id.checkedPlansText);

        plansText.setVisibility(View.GONE);
        overduePlansText.setVisibility(View.GONE);
        checkedPlansText.setVisibility(View.GONE);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-d"));

//        if (Objects.equals(selectedDate, today)) {
            Log.v("ConfirmButton", selectedDate+ " : " + today);

            List<Plan> regularPlans = new ArrayList<>();
            List<Plan> checkedPlans = new ArrayList<>();
            List<Plan> overDuePlans = new ArrayList<>();

            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            int minutesTime = convertTimeToMinutes(time.format(formatter));

            for (Plan plan : plans) {
                if (plan.isChecked()) {
                    checkedPlans.add(plan);
                    if(size(checkedPlans) > 0){
                        checkedPlansText.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (minutesTime > convertTimeToMinutes(plan.getSelectedTime())) {
                        overduePlansText.setVisibility(View.VISIBLE);
                        overDuePlans.add(plan);
                    } else {
                        plansText.setVisibility(View.VISIBLE);
                        regularPlans.add(plan);
                    }
                }
            }

            PlanAdapter regularAdapter = new PlanAdapter(regularPlans, selectedDay, selectedDate);
            PlanAdapter checkedAdapter = new PlanAdapter(checkedPlans, selectedDay, selectedDate);
            PlanAdapter overdueAdapter = new PlanAdapter(overDuePlans, selectedDay, selectedDate);


            checkedRecyclerView.setAdapter(checkedAdapter);
            checkedRecyclerView.setLayoutManager(new LinearLayoutManager(this));


            regularRecyclerView.setAdapter(regularAdapter);
            regularRecyclerView.setLayoutManager(new LinearLayoutManager(this));


            overdueRecyclerView.setAdapter(overdueAdapter);
            overdueRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        } else {
//            Log.v("ConfirmButton", selectedDate+ " : " + today);
//            Log.v("ConfirmButton", selectedDate+ " : " + today);
//
//            List<Plan> regularPlans = new ArrayList<>();
//            PlanAdapter regularAdapter = new PlanAdapter(regularPlans, selectedDay, selectedDate);
//
//            for (Plan plan : plans) {
//                plansText.setVisibility(View.VISIBLE);
//                regularPlans.add(plan);
//            }
//
//            plansText.setVisibility(View.VISIBLE);
//            regularRecyclerView.setAdapter(regularAdapter);
//            regularRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.addPlan) {
            Intent intent = new Intent(getApplicationContext(), SpecificPlanActivity.class);
            startActivity(intent);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    private void logOutUser() {
        if (mAuth.getCurrentUser() == null) {
            toMainActivity();
        } else {
            FirebaseAuth.getInstance().signOut();
            toMainActivity();
        }
    }



    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(TabsActivity.this,
                "Wystąpił błąd! Spróbuj ponownie później.",
                Toast.LENGTH_LONG).show();
    }

    private void openCalendar() {
        View rootView = findViewById(android.R.id.content);

        disableClicksOutsideCalendar(rootView, true);

        View overlayView = new View(this);
        overlayView.setBackgroundColor(Color.parseColor("#80000000"));
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ((ViewGroup) rootView).addView(overlayView, 0);

        ConstraintLayout calendarLayout = findViewById(R.id.calendar_layout);
        calendarLayout.setVisibility(View.VISIBLE);

        Button cancelButton = findViewById(R.id.calendar_cancel_button);
        cancelButton.setOnClickListener(v -> {
            disableClicksOutsideCalendar(rootView, false);
            calendarLayout.setVisibility(View.GONE);
            ((ViewGroup) rootView).removeView(overlayView);
        });

        CalendarView calendarView = findViewById(R.id.calendar_choose_date);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                Log.v("ConfirmButton", selectedDate);
            }
        });

        Button confirmButton = findViewById(R.id.calendar_button_confirm);
        confirmButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.getDefault());
                LocalDate parsedDate = LocalDate.parse(selectedDate, formatter);

                firstDayOfWeek = parsedDate.with(DayOfWeek.MONDAY);
                lastDayOfWeek = firstDayOfWeek.plusDays(6);

                Log.d("ConfirmButton", "Selected Date: " + selectedDate);
                Log.d("ConfirmButton", "First Day of the Week: " + firstDayOfWeek);
                Log.d("ConfirmButton", "Last Day of the Week: " + lastDayOfWeek);

                week_string = firstDayOfWeek + " - " + lastDayOfWeek;
                week_text.setText(week_string);

                RadioGroup radioGroup = findViewById(R.id.DaysOfTheWeek);
                updateRadioButtonSelection(radioGroup, parsedDate.getDayOfWeek().getValue());

                disableClicksOutsideCalendar(rootView, false);
                calendarLayout.setVisibility(View.GONE);
                ((ViewGroup) rootView).removeView(overlayView);

                selectedDay = parsedDate.getDayOfWeek().getValue();
                fetchAndDisplayPlans(selectedDay);

            } else {
                Toast.makeText(TabsActivity.this, "Proszę wybierz datę", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableClicksOutsideCalendar(View view, boolean disable) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child.getId() != R.id.calendar_layout) {
                    child.setClickable(!disable);
                    if (child instanceof ViewGroup) {
                        disableClicksOutsideCalendar(child, disable);
                    }
                }
            }
        }
    }
    private void updateRadioButtonSelection(RadioGroup radioGroup, int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                radioGroup.check(R.id.Monday);
                break;
            case 2:
                radioGroup.check(R.id.Tuesday);
                break;
            case 3:
                radioGroup.check(R.id.Wednesday);
                break;
            case 4:
                radioGroup.check(R.id.Thursday);
                break;
            case 5:
                radioGroup.check(R.id.Friday);
                break;
            case 6:
                radioGroup.check(R.id.Saturday);
                break;
            case 7:
                radioGroup.check(R.id.Sunday);
                break;
        }
    }

    private void toVulcan(){
        Intent intent = new Intent(getApplicationContext(), Vulcan_Activity.class);
        startActivity(intent);
    }

    private void toNotes(){
        Intent intent = new Intent(getApplicationContext(), Notes.class);
        startActivity(intent);
    }

    private void toMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void toSignActivity(){
        Intent intent = new Intent(getApplicationContext(), SignLanguage.class);
        startActivity(intent);
    }

    private void toRanking(){
        Intent intent = new Intent(getApplicationContext(), RankingActivity.class);
        startActivity(intent);
    }

}