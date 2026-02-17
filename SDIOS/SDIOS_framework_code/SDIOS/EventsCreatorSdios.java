package android.hardware.SDIOS;

import android.hardware.Sensor;
import android.hardware.SensorEventSdios;
import android.os.Bundle;

import android.hardware.SDIOS.NonNull;

public class EventsCreatorSdios {

    @NonNull
    public SensorEventSdios createSensorEventSdios(@NonNull Sensor sensor, long timestamp, int accuracy,
                                               @NonNull float[] values, float trust) {
        return new SensorEventSdios(accuracy, sensor, timestamp, values, trust);
    }

    @NonNull
    public SensorEventSdios createSensorEventSdios(@NonNull Sensor sensor, @NonNull Bundle bundle) {
        return createSensorEventSdios(sensor, bundle.getLong("timestamp"), bundle.getInt("accuracy"),
                bundle.getFloatArray("values"), bundle.getFloat("trust", -1.1f));
    }
}
