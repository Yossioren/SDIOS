package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONObject;

public class ScanAll extends SkipScan {
    private static final int default_initial_size = 300;
    private int initial_size;
    private int current_size = 0;

    public ScanAll(UserConfigManager userConfigManager) {
        super(userConfigManager);
        load_parameters(userConfigManager.parameters);
    }

    @Override
    public void load_parameters(JSONObject parameters) {
        initial_size = parameters.optInt("initial_size", default_initial_size);
    }

    public void add(SensorEventSdios sensorEvent) {
        if (current_size <= initial_size) {
            current_size += 1;
        }
    }

    public boolean shouldScan() {
        return current_size > initial_size;
    }

    @Override
    public void reset() {
        current_size = 0;
    }
}
