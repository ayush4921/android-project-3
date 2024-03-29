package com.example.project3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TimePicker tp = findViewById(R.id.timePicker);
        Button setAlarm = findViewById(R.id.buttonSetAlarm);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Calendar c=Calendar.getInstance();

            int hr = tp.getHour();
            int m = tp.getMinute();


            c.set(Calendar.HOUR_OF_DAY,hr);
            c.set(Calendar.MINUTE,m);
        // Add logic to set date of the calendar object and get time in milliseconds
        // like we did earlier for setting the alarm
        }

    }
}