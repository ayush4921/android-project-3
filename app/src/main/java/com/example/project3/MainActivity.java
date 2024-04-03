package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private int selectedFrequency;
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARM_TIME_KEY = "alarmTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePicker tp = findViewById(R.id.timePicker);
        Button setAlarm = findViewById(R.id.buttonSetAlarm);
        Button deleteAlarm = findViewById(R.id.buttonDeleteAlarm);
        Spinner frequencySpinner = findViewById(R.id.frequencySpinner);

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
                cal.set(year, month, dayOfMonth);     // updating date in calendar
            }
        });

        setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = tp.getHour();
                int minute = tp.getMinute();
                // Ensure the calendar is set to the selected date and time
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);

                startSensing(selectedFrequency, cal);

                // Store alarm time in SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(ALARM_TIME_KEY, cal.getTimeInMillis());
                editor.apply();

                // Show a toast message to indicate the alarm is set
                Toast.makeText(MainActivity.this, "Alarm has been updated", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove alarm settings
                cancelAlarm();

                // Remove alarm time from SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(ALARM_TIME_KEY);
                editor.apply();

                Toast.makeText(MainActivity.this, "Alarm has been removed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startSensing(int frequency, Calendar cal) {
        Log.d("MyActivity", "Alarm On. Frequency = " + frequency);
        long currentTime = cal.getTimeInMillis();

        Intent intent = new Intent(this, SendNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long frequencyMillis = frequency * 60 * 1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, currentTime, frequencyMillis, pendingIntent);
    }

    private void cancelAlarm() {
        Intent intent = new Intent(this, SendNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}