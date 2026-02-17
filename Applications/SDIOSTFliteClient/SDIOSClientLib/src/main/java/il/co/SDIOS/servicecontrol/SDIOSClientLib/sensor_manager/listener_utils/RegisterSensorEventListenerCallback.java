package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils;

import android.annotation.SuppressLint;
import android.hardware.Sensor;

import androidx.annotation.NonNull;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.service.SensorManagerSdiosHelper;


@SuppressLint("CallbackMethodName")
public class RegisterSensorEventListenerCallback {
    private final @NonNull SensorManagerSdiosHelper sdiosHelper;
    private final @NonNull SensorEventListener listener;
    private final @NonNull Sensor sensor;
    private final int samplingPeriodUs;
    private final int maxReportLatencyUs;

    @SuppressLint("ExecutorRegistration")
    public RegisterSensorEventListenerCallback(@NonNull SensorManagerSdiosHelper sdiosHelper,
                                               @NonNull SensorEventListener listener,
                                               @NonNull Sensor sensor,
                                               int samplingPeriodUs, int maxReportLatencyUs) {
        this.sdiosHelper = sdiosHelper;
        this.listener = listener;
        this.sensor = sensor;
        this.samplingPeriodUs = samplingPeriodUs;
        this.maxReportLatencyUs = maxReportLatencyUs;
    }

    public void register(int identifier) {
        sdiosHelper.putConnection(sensor, identifier, listener);
    }

    public void register_fallback() {
        sdiosHelper.register_fallback(sensor, samplingPeriodUs, maxReportLatencyUs, null, listener);
    }

    public String toString() {
        return "RegisterSensorEventListenerCallback<" + sensor.toString() + "," + samplingPeriodUs + "," + maxReportLatencyUs + ">";
    }
}
