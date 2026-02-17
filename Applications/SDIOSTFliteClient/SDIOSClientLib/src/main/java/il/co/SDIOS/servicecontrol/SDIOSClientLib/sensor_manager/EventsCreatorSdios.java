package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager;

import android.hardware.Sensor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEvent;

public class EventsCreatorSdios {

    @NonNull
    public SensorEvent createSensorEvent(@NonNull Sensor sensor, long timestamp, int accuracy, @NonNull float[] values, float trust) {
        return new SensorEvent(accuracy, sensor, timestamp, values, trust);
    }

    @NonNull
    public SensorEvent createSensorEvent(@NonNull Sensor sensor, @NonNull Bundle bundle) {
        return createSensorEvent(sensor, bundle.getLong("timestamp"), bundle.getInt("accuracy"), bundle.getFloatArray("values"), bundle.getFloat("trust", -1));
    }
}
