package com.example.neurobuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.neurobuddy.Plan.CurrentDate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class LearningPlanJobIntentService extends JobIntentService {

    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, LearningPlanJobIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        checkAndSendLearningPlanNotifications();
    }

    private void checkAndSendLearningPlanNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        DatabaseReference plansRef = userRef.child("plans");

        plansRef.orderByChild("planType").equalTo("Learning").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot planSnapshot : dataSnapshot.getChildren()) {
                    if (planSnapshot.child("planType").exists()) {
                        String planType = planSnapshot.child("planType").getValue(String.class);
                        if ("Specific".equals(planType)) {
                            String selectedDate = planSnapshot.child("selectedDate").getValue(String.class);
                            String selectedTime = planSnapshot.child("selectedTime").getValue(String.class);

                            long notificationTime = convertToTimestamp(selectedDate, selectedTime);

                            scheduleNotification(planSnapshot.child("title").getValue(String.class), notificationTime);
                        } else if ("Routine".equals(planType)) {
                            handleRoutinePlans(planSnapshot);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void scheduleNotification(String title, long notificationTime) {
        Intent intent = new Intent(this, YourNotificationReceiver.class);
        intent.putExtra("title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
    }

    private long convertToTimestamp(String selectedDate, String selectedTime) {
        try {
            String dateTimeString = selectedDate + " " + selectedTime;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = format.parse(dateTimeString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean isTodaySelectedDay(String selectedDate) {
        LocalDate today = LocalDate.now();
        LocalDate selectedDay = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-d"));
        return today.equals(selectedDay);
    }

    private void handleRoutinePlans(DataSnapshot planSnapshot) {
        String selectedTime = planSnapshot.child("selectedTime").getValue(String.class);
        String selectedDays = planSnapshot.child("selectedDays").getValue(String.class);

        CurrentDate c = new CurrentDate();
        c.initializeWeek();

        String dayOfTheWeek = String.valueOf(CurrentDate.dayOfWeek);

        if (selectedDays != null && selectedDays.contains(dayOfTheWeek)) {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-d"));
            long notificationTime = convertToTimestamp(today, selectedTime);
            scheduleNotification(planSnapshot.child("title").getValue(String.class), notificationTime);
        }
    }

}

