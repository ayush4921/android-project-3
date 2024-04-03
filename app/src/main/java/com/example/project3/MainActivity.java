package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
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
        options.add("1 hour");

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
                // Ensure the calendar is set to the selected date and t

                start_Sensing_new(selectedFrequency, cal);

                // Show a toast message to indicate the alarm is set
                Toast.makeText(MainActivity.this, "Alarm has been updated", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove alarm settings here
                Toast.makeText(MainActivity.this, "Alarm has been removed", Toast.LENGTH_SHORT).show();
            }
        });
        }

    public void start_Sensing_new(int frequency, Calendar cal){
        Log.d("MyActivity", "Alarm On. Frequency = " + frequency);
        long currentTime = cal.getTimeInMillis();

        Intent intent = new Intent(this, SendNotification.class);
        intent.putExtra("frequency", frequency);
        intent.putExtra("Calendar", cal);
        PendingIntent pendingIntent = null;

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        long frequencyMillis;
        frequencyMillis = frequency*60*1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, currentTime, frequencyMillis, pendingIntent);
    }

    }
