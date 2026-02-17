package android.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import android.hardware.SDIOS.NonNull;

public class SensorEventSdios extends SensorEvent {
    public final float trust;

    public SensorEventSdios(int accuracy, @NonNull Sensor sensor, long timestamp, @NonNull float[] values_arr, float trust) {
        super(values_arr.length);
        this.trust = trust;
        this.init(accuracy, sensor, timestamp, values_arr);
    }

    public SensorEventSdios(@NonNull SensorEvent sensorEvent) {
        super(sensorEvent.values.length);
        this.trust = -1;
        this.init(sensorEvent.accuracy, sensorEvent.sensor, sensorEvent.timestamp, sensorEvent.values);
    }

    private void init(int accuracy, @NonNull Sensor sensor, long timestamp, @NonNull float[] values_arr) {
        this.accuracy = accuracy;
        this.sensor = sensor;
        this.timestamp = timestamp;
        System.arraycopy(values_arr, 0, this.values, 0, this.values.length);
    }
}
