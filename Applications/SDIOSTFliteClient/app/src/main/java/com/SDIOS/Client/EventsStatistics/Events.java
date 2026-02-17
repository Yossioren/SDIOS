package com.SDIOS.Client.EventsStatistics;

import android.hardware.Sensor;
import android.widget.TextView;

import androidx.annotation.NonNull;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEvent;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;

public class Events extends Timer implements SensorEventListener {
    private final TextView textViewResponse;

    public Events(TextView textViewResponse) {
        super(5);
        this.textViewResponse = textViewResponse;
    }

    @Override
    public void onSensorChanged(@NonNull SensorEvent sensorEvent) {
        if (this.should_skip())
            return;
        String event_time = String.format("SensorEventListener <%f, %f, %f>\nTime: %s",
                sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                super.timeStr(sensorEvent.timestamp));
        this.textViewResponse.setText(event_time);
    }

    @Override
    public void onAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
    }
}
