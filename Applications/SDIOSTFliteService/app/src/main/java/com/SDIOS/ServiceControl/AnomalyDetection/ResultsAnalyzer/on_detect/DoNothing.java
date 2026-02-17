package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

public class DoNothing extends OnDetect {
    public DoNothing(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
    }

    @Override
    public SensorEventSdios actionOnDetection(SensorEventSdios mySensorEvent) {
        return null;
    }

    @Override
    public void load_parameters(JSONObject parameters) {
        allowed_trust_level = 0;
    }
}
