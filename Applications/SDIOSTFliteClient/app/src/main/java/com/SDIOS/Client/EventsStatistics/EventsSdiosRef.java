package com.SDIOS.Client.EventsStatistics;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class EventsSdiosRef extends Timer implements SensorEventListener {//SDIOS {
    private static final String TAG = "EventsSdiosRef";
    private final TextView textViewResponse;
    Class<?> clazz = null;
    Field field = null;
    private boolean crashing = false;

    public EventsSdiosRef(TextView textViewResponse) {
        super(5);
        this.textViewResponse = textViewResponse;
        try {
            clazz = Class.forName("android.hardware.SensorEventSDIOS");
            field = clazz.getDeclaredField("trust");
            field.setAccessible(true);
        } catch (Throwable e) {
            Log.e(TAG, "Init: " + e);
            this.crashing = true;
        }
    }

    public float getTrust(SensorEvent sensorEvent) {
        float value = -1;
        if (this.crashing)
            return -1;
        try {
            if (field != null) {
                value = (float) field.get(sensorEvent);
            }
        } catch (Throwable e) {
            Log.e(TAG, "GetTrust: " + e);
            this.crashing = true;
        }
        return value;
    }

    @Override
    public void onSensorChanged(@NonNull SensorEvent sensorEvent) {
        if (this.should_skip())
            return;
        String event_text = String.format("EventsSdiosRef <%f, %f, %f>\nTrust: %f\nTime: %s",
                sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                getTrust(sensorEvent), super.timeStr(sensorEvent.timestamp));
        this.textViewResponse.setText(event_text);
    }

    @Override
    public void onAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
    }
}
