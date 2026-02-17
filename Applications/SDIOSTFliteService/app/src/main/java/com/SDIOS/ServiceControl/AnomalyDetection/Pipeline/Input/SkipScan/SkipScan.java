package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigUpdateCallback;

public abstract class SkipScan extends UserConfigUpdateCallback {
    public SkipScan(UserConfigManager userConfigManager) {
        super(userConfigManager);
    }

    public abstract void add(SensorEventSdios sensorEvent);

    public abstract boolean shouldScan();

    public abstract void reset();
}
