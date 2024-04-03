package com.example.project3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class SendNotification extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "12001";
    private final static String default_notification_channel_id = "default1";

    @Override
    public void onReceive(Context context, Intent intent) {
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
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
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