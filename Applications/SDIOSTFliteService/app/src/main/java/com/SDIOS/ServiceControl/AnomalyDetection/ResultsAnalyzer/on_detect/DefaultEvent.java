package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect;

import static com.SDIOS.ServiceControl.AnomalyDetection.uilts.MyArrays.get_shape_array;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;

import java.util.Arrays;

public class DefaultEvent extends OnDetect {

    private final int[] default_event;

    public DefaultEvent(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
        this.default_event = get_shape_array(userConfigManager.parameters, "default_event");
    }

    @Override
    public SensorEventSdios actionOnDetection(SensorEventSdios mySensorEvent) {
        for (int i = 0; i < mySensorEvent.values.length && i < default_event.length; i++) {
            mySensorEvent.values[i] = default_event[i];
        }
        return mySensorEvent;
    }
}
