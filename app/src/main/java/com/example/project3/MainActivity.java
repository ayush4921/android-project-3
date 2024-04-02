package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String selectedFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePicker tp = findViewById(R.id.timePicker);
        Button setAlarm = findViewById(R.id.buttonSetAlarm);
        Spinner frequencySpinner = findViewById(R.id.frequencySpinner);

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
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setTextColor(Color.WHITE);
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
                selectedFrequency = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFrequency = null;
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Calendar c=Calendar.getInstance();

            int hr = tp.getHour();
            int m = tp.getMinute();


            c.set(Calendar.HOUR_OF_DAY,hr);
            c.set(Calendar.MINUTE,m);
        // Add logic to set date of the calendar object and get time in milliseconds
        // like we did earlier for setting the alarm
        }

        CalendarView calView = findViewById(R.id.calendarView);
        calView.setBackgroundColor(Color.WHITE);
        Calendar cal = Calendar.getInstance();


        int curDay = cal.get(Calendar.DAY_OF_MONTH);
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH);

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                cal.set(year, month, dayOfMonth);     // updating date in calendar
            }
        });

        }

    }
