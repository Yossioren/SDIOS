package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONObject;

public class SkipByTimestamp extends SkipScan {
    private final static long ms_to_timestamp_ns = 1000000;
    private long skip_ns;
    private long last_timestamp = 0;
    private long current_timestamp = 0;
    private boolean should_scan;

    public SkipByTimestamp(UserConfigManager userConfigManager) {
        super(userConfigManager);
        load_parameters(userConfigManager.parameters);
    }

    @Override
    public void load_parameters(JSONObject parameters) {
        int skipMS = parameters.optInt("skip_ms", 1000);
        this.skip_ns = skipMS * ms_to_timestamp_ns;
    }

    public void add(SensorEventSdios sensorEvent) {
        current_timestamp = sensorEvent.timestamp;
        if (current_timestamp - last_timestamp >= skip_ns) {
            last_timestamp = current_timestamp;
            should_scan = true;
        } else
            should_scan = false;
    }

    public boolean shouldScan() {
        return should_scan;
    }

    @Override
    public void reset() {
        last_timestamp = 0;
        current_timestamp = 0;
    }
}
