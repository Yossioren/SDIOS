package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONObject;

import java.util.List;

public class SearchTimestamps extends SearchStart {
    private final static long ms_to_timestamp_ns = 1000000;
    // based on the fact the data is already ordered by timestamp
    private long collectMS_ns;

    public SearchTimestamps(UserConfigManager userConfigManager) {
        super(userConfigManager);
        load_parameters(userConfigManager.parameters);
    }

    @Override
    public void load_parameters(JSONObject parameters) {
        int collectMS = parameters.optInt("collect_ms", 1000);
        this.collectMS_ns = (long) (1.1 * collectMS * ms_to_timestamp_ns);
    }

    @Override
    public int get_start_index(List<SensorEventSdios> list) {
        int size = list.size();
        assert size > 5;
        long start_time = list.get(0).timestamp;
        long end_time = list.get(size - 1).timestamp;
        long wanted_time = end_time - collectMS_ns;
        if (start_time > wanted_time) {
            return -1; // Not enough data has been collected
        }
        return binary_search_index(list, size, wanted_time);
    }

    private int binary_search_index(List<SensorEventSdios> list, int size, long wanted_time) {
        int current_index = size >> 1;
        int jump = size >> 1;
        int last_index = size - 1;
        while (jump > 0) {
            if (list.get(current_index).timestamp > wanted_time)
                current_index = Math.max(current_index - jump, 0);
            else if (list.get(current_index).timestamp < wanted_time)
                current_index = Math.min(current_index + jump, last_index);
            else
                break;
            jump >>= 1;
        }
        return current_index;
    }
}
