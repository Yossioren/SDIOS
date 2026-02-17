package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigUpdateCallback;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class OnDetect extends UserConfigUpdateCallback {
    protected double allowed_trust_level;

    public OnDetect(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
        load_parameters(userConfigManager.parameters);
    }

    public boolean isTrustAmountMalicious(float trust) {
        if (trust < 0)
            return false;
        return trust < allowed_trust_level;
    }

    public abstract SensorEventSdios actionOnDetection(SensorEventSdios mySensorEvent);

    @Override
    public void load_parameters(JSONObject parameters) throws JSONException {
        allowed_trust_level = parameters.getDouble("trust_level");
    }
}
