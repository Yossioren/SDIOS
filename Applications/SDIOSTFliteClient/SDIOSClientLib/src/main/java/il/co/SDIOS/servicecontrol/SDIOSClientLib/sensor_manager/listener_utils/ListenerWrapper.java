package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils;

import android.annotation.SuppressLint;
import android.hardware.Sensor;

import androidx.annotation.NonNull;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEvent;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;

public class ListenerWrapper implements android.hardware.SensorEventListener {
    SensorEventListener listener;

    @SuppressLint("ExecutorRegistration")
    public ListenerWrapper(@NonNull SensorEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(@NonNull android.hardware.SensorEvent sensorEvent) {
        listener.onSensorChanged(new SensorEvent(sensorEvent));
    }

    @Override
    public void onAccuracyChanged(@NonNull Sensor sensor, int i) {
        listener.onAccuracyChanged(sensor, i);
    }
}
