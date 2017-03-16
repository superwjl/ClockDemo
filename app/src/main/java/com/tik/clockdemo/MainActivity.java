package com.tik.clockdemo;

import android.app.TimePickerDialog;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ClockView mClockView;
    private ClockView mClockView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mClockView = (ClockView) findViewById(R.id.clockView);
        mClockView2 = (ClockView) findViewById(R.id.clockView2);
    }

    public void start1(View view){
        mClockView.start();
    }

    public void stop1(View view){
        mClockView.stop();
    }
    public void start2(View view){
        mClockView2.start();
    }

    public void stop2(View view){
        mClockView2.stop();
    }

    public void set(View view){
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mClockView2.setTime(hourOfDay, minute, -1);
            }
        }, hour, minute, true).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
