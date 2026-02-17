package android.hardware.SDIOS.listener_utils;

import android.annotation.SuppressLint;
import android.hardware.Sensor;

import android.hardware.SDIOS.NonNull;

import android.hardware.SensorEventListener;
import android.hardware.SDIOS.service.SensorManagerSdiosHelper;

@SuppressLint("CallbackMethodName")
public class RegisterSensorEventListenerCallback {
    private final SensorManagerSdiosHelper sdiosHelper;
    private final SensorEventListener listener;
    private final Sensor sensor;
    private final int samplingPeriodUs;
    private final int maxReportLatencyUs;

    @SuppressLint("ExecutorRegistration")
    public RegisterSensorEventListenerCallback(@NonNull SensorManagerSdiosHelper sdiosHelper,
                                               @NonNull SensorEventListener listener, @NonNull Sensor sensor,
                                               int samplingPeriodUs, int maxReportLatencyUs) {
        this.sdiosHelper = sdiosHelper;
        this.listener = listener;
        this.sensor = sensor;
        this.samplingPeriodUs = samplingPeriodUs;
        this.maxReportLatencyUs = maxReportLatencyUs;
    }

    @SuppressLint("CallbackMethodName")
    public void register(int identifier) {
        sdiosHelper.putConnection(sensor, identifier, listener);
    }

    @SuppressLint("CallbackMethodName")
    public void register_fallback() {
        sdiosHelper.register_fallback(sensor, samplingPeriodUs, maxReportLatencyUs, null, listener);
    }

    @Override
    @SuppressLint("CallbackMethodName")
    public String toString() {
        return "RegisterSensorEventListenerCallback<" + sensor.getName() + "_" + sensor.getType() + "," + samplingPeriodUs + "," + maxReportLatencyUs + ">";
    }
}
