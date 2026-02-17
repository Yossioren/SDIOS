package il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses;

import android.hardware.Sensor;

import androidx.annotation.NonNull;

public interface SensorEventListener {
    void onSensorChanged(@NonNull SensorEvent sensorEvent);

    void onAccuracyChanged(@NonNull Sensor sensor, int accuracy);
}
