package il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses;

import android.hardware.Sensor;

import androidx.annotation.NonNull;

public class SensorEvent {
    public final int accuracy;
    public final @NonNull Sensor sensor;
    public final long timestamp;
    public final @NonNull float[] values;
    public final float trust;

    public SensorEvent(int accuracy, @NonNull Sensor sensor, long timestamp, @NonNull float[] values, float trust) {
        this.accuracy = accuracy;
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.values = values;
        this.trust = trust;
    }

    public SensorEvent(@NonNull android.hardware.SensorEvent sensorEvent) {
        this.accuracy = sensorEvent.accuracy;
        this.sensor = sensorEvent.sensor;
        this.timestamp = sensorEvent.timestamp;
        this.values = sensorEvent.values;
        this.trust = -1;
    }
}
