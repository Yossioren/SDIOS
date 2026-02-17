package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;

public class DropEvent extends OnDetect {
    public DropEvent(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
    }

    @Override
    public SensorEventSdios actionOnDetection(SensorEventSdios mySensorEvent) {
        return null;
    }
}
