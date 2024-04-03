package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private int selectedFrequency;
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARM_TIME_KEY = "alarmTime";
    private static final String ALARM_FREQUENCY_KEY = "alarmFrequency";
    private TextView textViewDuration;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewDuration = findViewById(R.id.textViewDuration);
        TimePicker tp = findViewById(R.id.timePicker);
        Button setAlarm = findViewById(R.id.buttonSetAlarm);
        Button deleteAlarm = findViewById(R.id.buttonDeleteAlarm);
        Spinner frequencySpinner = findViewById(R.id.frequencySpinner);
        Button testNotification = findViewById(R.id.buttonTestNotification);
        testNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTestNotification();
            }
        });
        frequencySpinner.setBackgroundColor(Color.WHITE);

        List<String> options = new ArrayList<>();
        options.add("Select Frequency");
        options.add("5 minutes");
        options.add("10 minutes");
        options.add("15 minutes");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) { // Hint's position
                    textView.setText(getItem(position));
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);
        frequencySpinner.setSelection(0);
        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFrequencyString = parent.getItemAtPosition(position).toString();
                if (position > 0) {
                    String[] parts = selectedFrequencyString.split(" ");
                    selectedFrequency = Integer.parseInt(parts[0]);
                } else {
                    selectedFrequency = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFrequency = 0;
            }
        });

        CalendarView calView = findViewById(R.id.calendarView);
        calView.setBackgroundColor(Color.WHITE);
        Calendar cal = Calendar.getInstance();

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                cal.set(year, month, dayOfMonth);
            }
        });

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFrequency == 0) {
                    Toast.makeText(MainActivity.this, "Please select a frequency", Toast.LENGTH_SHORT).show();
                    return;
                }

                int hour = tp.getHour();
                int minute = tp.getMinute();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);

                if (cal.before(Calendar.getInstance())) {
                    Toast.makeText(MainActivity.this, "Please select a future time", Toast.LENGTH_SHORT).show();
                    return;
                }

                startSensing(selectedFrequency, cal);

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(ALARM_TIME_KEY, cal.getTimeInMillis());
                editor.putInt(ALARM_FREQUENCY_KEY, selectedFrequency);
                editor.apply();
                updateDurationMessage();
                Toast.makeText(MainActivity.this, "Alarm has been set", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(ALARM_TIME_KEY);
                editor.remove(ALARM_FREQUENCY_KEY);
                editor.apply();
                updateDurationMessage();
                Toast.makeText(MainActivity.this, "Alarm has been canceled", Toast.LENGTH_SHORT).show();
            }
        });

        // Restore previous alarm settings on app restart
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long alarmTime = prefs.getLong(ALARM_TIME_KEY, 0);
        int alarmFrequency = prefs.getInt(ALARM_FREQUENCY_KEY, 0);

        if (alarmTime > 0 && alarmFrequency > 0) {
            Calendar alarmCal = Calendar.getInstance();
            alarmCal.setTimeInMillis(alarmTime);
            startSensing(alarmFrequency, alarmCal);
        }

        updateDurationMessage();
    }
    private void updateDurationMessage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long alarmTime = prefs.getLong(ALARM_TIME_KEY, 0);
        int alarmFrequency = prefs.getInt(ALARM_FREQUENCY_KEY, 0);

        if (alarmTime > 0 && alarmFrequency > 0) {
            long currentTime = System.currentTimeMillis();
            long nextAlarmTime = alarmTime;

            while (nextAlarmTime < currentTime) {
                nextAlarmTime += TimeUnit.MINUTES.toMillis(alarmFrequency);
            }

            long durationMillis = nextAlarmTime - currentTime;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);

            String message = "The next alarm will go live in " + minutes + " minutes.";
            textViewDuration.setText(message);
        } else {
            textViewDuration.setText("No upcoming alarm");
        }
    }
    private void sendTestNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String contentText = "This is a test notification";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SendNotification.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Test Notification")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(SendNotification.NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(SendNotification.NOTIFICATION_CHANNEL_ID);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
    public void startSensing(int frequency, Calendar cal) {
        Log.d("MyActivity", "Alarm Set. Frequency: " + frequency + ", Time: " + cal.getTime());
        long alarmTime = cal.getTimeInMillis();

        Intent intent = new Intent(this, SendNotification.class);
        intent.putExtra("frequency", frequency);
        intent.putExtra("alarmTime", alarmTime);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            } else {
                // Request permission to schedule exact alarms
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(permissionIntent);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        }

        // Schedule the next alarm based on the frequency
        long frequencyMillis = frequency * 60 * 1000;
        long nextAlarmTime = alarmTime + frequencyMillis;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
    }

    private void cancelAlarm() {
        Intent intent = new Intent(this, SendNotification.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        Log.d("MyActivity", "Alarm Canceled");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
                Log.d("MainActivity", "Notification permission granted");
            } else {
                // Notification permission denied
                Log.d("MainActivity", "Notification permission denied");
            }
        }
    }
}