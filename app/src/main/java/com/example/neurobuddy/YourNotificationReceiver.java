package com.example.neurobuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class YourNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");

        // Check for the required permission before showing the notification
        if (hasPermission(context, "android.permission.USE_FULL_SCREEN_INTENT")) {
            // You can add code here to show a notification using NotificationCompat.Builder
            // Example:
            int notificationId = 1; // Assign a unique identifier for the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText("Your notification text")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());

            // For simplicity, let's log the notification for now
            Log.d("YourNotificationReceiver", "Received notification: " + title);
        } else {
            Log.w("YourNotificationReceiver", "Missing required permission: USE_FULL_SCREEN_INTENT");
        }
    }

    private boolean hasPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
