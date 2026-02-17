package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;

import java.util.Arrays;

public class ZeroEvent extends OnDetect {

    public ZeroEvent(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
    }

    @Override
    public SensorEventSdios actionOnDetection(SensorEventSdios mySensorEvent) {
        Arrays.fill(mySensorEvent.values, 0);
        return mySensorEvent;
    }
}
