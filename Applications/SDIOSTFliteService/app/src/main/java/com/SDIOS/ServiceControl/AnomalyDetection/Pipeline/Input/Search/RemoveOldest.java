package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONObject;

import java.util.List;

public class RemoveOldest extends SearchStart {

    public RemoveOldest(UserConfigManager userConfigManager) {
        super(userConfigManager);
    }

    @Override
    public int get_start_index(List<SensorEventSdios> list) {
        return 1;
    }

    @Override
    public void load_parameters(JSONObject parameters) {
    }
}
