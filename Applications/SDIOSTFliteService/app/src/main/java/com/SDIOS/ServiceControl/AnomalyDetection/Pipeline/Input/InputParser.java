package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.DataCollector;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SimpleArrayList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SimpleConcurentList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search.RemoveOldest;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search.SearchStart;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.Search.SearchTimestamps;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan.ScanAll;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan.SkipByTimestamp;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.SkipScan.SkipScan;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputParser {
    private final static String TAG = "InputParser";
    private final static String DEFAULT = "default";
    private final static Map<String, Class<? extends SearchStart>> names_to_input_searchers = new HashMap<String, Class<? extends SearchStart>>() {{
        put(DEFAULT, RemoveOldest.class);
        put("remove_oldest", RemoveOldest.class);
        put("time", SearchTimestamps.class);
    }};
    private final static Map<String, Class<? extends SkipScan>> names_to_input_skip = new HashMap<String, Class<? extends SkipScan>>() {{
        put("scan_all", ScanAll.class);
        put(DEFAULT, ScanAll.class);
        put("time", SkipByTimestamp.class);
    }};

    private final JSONObject input_config;
    private final DataCollector dataCollector;
    private String key;
    private SearchStart searchStart;
    private SkipScan skipScan;

    public InputParser(String pipeline_name, String sensor, JSONObject input_config) {
        this.key = pipeline_name + "_" + sensor;
        this.input_config = input_config;
        this.dataCollector = new SimpleConcurentList();
        try {
            parse_config_data_collection();
            parse_config_skip_samples();
        } catch (JSONException e) {
            Log.e(TAG, "JSON error " + e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException " + e + ", " + e.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            Log.e(TAG, "Reflection error " + e);
        }
    }

    private void parse_config_skip_samples() throws JSONException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JSONObject data_collection = input_config.getJSONObject("skip_samples");
        UserConfigManager userConfigManager = new UserConfigManager(data_collection, TAG, this.key, "classifiers_map_skip_samples");
        String method = userConfigManager.method;
        if (!names_to_input_skip.containsKey(method)) {
            method = DEFAULT;
        }
        key += method + '_' + userConfigManager.parameters.toString();
        this.skipScan = names_to_input_skip.get(method).getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManager);
    }

    private void parse_config_data_collection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, JSONException {
        JSONObject data_collection = input_config.optJSONObject("data_collection");
        assert data_collection != null;
        UserConfigManager userConfigManager = new UserConfigManager(data_collection, TAG, this.key, "classifiers_map_data_collection");
        String method = userConfigManager.method;
        if (!names_to_input_skip.containsKey(method)) {
            method = DEFAULT;
        }
        key += method + '_' + userConfigManager.parameters.toString();
        this.searchStart = names_to_input_searchers.get(method).getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManager);
    }

    public List<SensorEventSdios> getCollected() {
        if (skipScan.shouldScan()) {
            List<SensorEventSdios> list = dataCollector.get();
            if (list.size() < 2) // skip_scan initialization response
                return null;
            int start = searchStart.get_start_index(list);
            if (start == -1)
                return null;
            List<SensorEventSdios> output = new ArrayList<>(list.subList(start, list.size()));
            dataCollector.delete(start);
            return output;
        }
        return null;
    }

    public void add(SensorEventSdios sensorEvent) {
        dataCollector.add(sensorEvent);
        skipScan.add(sensorEvent);
    }

    public String get_key() {
        return key;
    }

    public void reset() {
        dataCollector.reset();
        skipScan.reset();
    }

    public boolean should_scan_events() {
        return skipScan.shouldScan();
    }
}
