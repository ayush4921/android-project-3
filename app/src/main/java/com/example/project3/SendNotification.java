package com.example.project3;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SendNotification extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "12001";
    private final static String default_notification_channel_id = "default1";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SendNotification", "Broadcast received");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int frequency = intent.getIntExtra("frequency", 0);
        long alarmTime = intent.getLongExtra("alarmTime", 0);
        String alarmTimeString = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(alarmTime));
        String contentText = "Alarm triggered at " + alarmTimeString + " with frequency " + frequency + " minutes";
        NotificationCompat.Builder builder = getNotificationBuilder(context, contentText);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            Log.d("SendNotification", "Notification channel created");
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        Log.d("SendNotification", "Notification displayed");

        // Reschedule the alarm based on the frequency
        long frequencyMillis = frequency * 60 * 1000;
        long nextAlarmTime = alarmTime + frequencyMillis;
        Intent nextIntent = new Intent(context, SendNotification.class);
        nextIntent.putExtra("frequency", frequency);
        nextIntent.putExtra("alarmTime", nextAlarmTime);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, flags);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, nextPendingIntent);
            } else {

            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, nextPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, nextPendingIntent);
            }
        }
        Log.d("SendNotification", "Next alarm scheduled for: " + new Date(nextAlarmTime));
    }


    private NotificationCompat.Builder getNotificationBuilder(Context context, String content) {
        return new NotificationCompat.Builder(context, default_notification_channel_id)
                .setContentTitle("Alarm Notification")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }
}