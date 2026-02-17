package com.SDIOS.ServiceControl.AnomalyDetection;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;

public interface Evaluator {
    SensorEventSdios ComputeTrust(SensorEventSdios data);

    void reset();

    @NonNull
    String toString();

    String getSensorName();
}
