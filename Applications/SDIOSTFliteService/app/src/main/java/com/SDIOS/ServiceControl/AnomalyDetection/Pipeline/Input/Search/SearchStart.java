package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigUpdateCallback;

import java.util.List;

public abstract class SearchStart extends UserConfigUpdateCallback {
    public SearchStart(UserConfigManager userConfigManager) {
        super(userConfigManager);
    }

    public abstract int get_start_index(List<SensorEventSdios> list);
}
