package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector;

import android.hardware.SensorEvent;

public class SensorEventSdios {
    public final int accuracy;
    public final long timestamp;
    public final float[] values;
    public float trust;

    /*
        Clone SensorEvent object since it somehow reused
     */
    public SensorEventSdios(SensorEvent sensorEvent) {
        this(sensorEvent, 0);
    }

    public SensorEventSdios(SensorEvent sensorEvent, float trust) {
        accuracy = sensorEvent.accuracy;
        timestamp = sensorEvent.timestamp;
        values = sensorEvent.values.clone();
        this.trust = trust;
    }

    public SensorEventSdios(long timestamp, float[] values) {
        this(0, timestamp, values, 0);
    }

    public SensorEventSdios(int accuracy, long timestamp, float[] values, float trust) {
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.values = values;
        this.trust = trust;
    }
}
