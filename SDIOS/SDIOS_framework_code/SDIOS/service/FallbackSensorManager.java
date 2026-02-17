package android.hardware.SDIOS.service;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.os.Handler;

import android.hardware.SDIOS.Nullable;
import android.hardware.SensorEventListener;

public interface FallbackSensorManager {
    @SuppressLint({"ExecutorRegistration", "RegistrationName"})
    public boolean registerListenerFallback(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                            int delayUs, @Nullable Handler handler, int maxBatchReportLatencyUs, int reservedFlags);

    @SuppressLint("RegistrationName")
    public void unregisterListenerFallback(@Nullable SensorEventListener listener, @Nullable Sensor sensor);
}
