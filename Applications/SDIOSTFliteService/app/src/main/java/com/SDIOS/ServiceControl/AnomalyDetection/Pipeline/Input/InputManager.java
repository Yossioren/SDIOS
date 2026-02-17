package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input;

import android.util.Log;

import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InputManager {
    private final static String TAG = "InputManager";
    private static final Map<String, String> pipeline_to_key = new HashMap<>();
    private static final Map<String, InputParser> cache_parsers = new HashMap<>();
    private final ClassifiersPackage package_config;

    public InputManager(ClassifiersPackage package_config) {
        this.package_config = package_config;
        try {
            parse_input();
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
    }

    private void parse_input() throws JSONException {
        /* Map objects built like this:
        {
        "name": "x_pipeline",
        "input": {
          "sensors": ["gyroscope"],
          "data_collection": {"method": "time","parameters": {"collect_ms": 2000}},
          "skip_samples": {"method": "time","parameters": {"skip_ms": 400}}
        },
        "preprocess": ...,
        "classifiers": ...}
 */
        JSONArray classifiers = this.package_config.origin_json.optJSONArray("classifiers_map");
        assert classifiers != null;
        for (int i = 0; i < classifiers.length(); i++) {
            JSONObject nn_config = classifiers.getJSONObject(i);
            String pipeline_name = nn_config.getString("name");
            JSONObject input_config = nn_config.getJSONObject("input");
            JSONArray sensors = input_config.getJSONArray("sensors");
            for (int j = 0; j < sensors.length(); j++) {
                String sensor = sensors.getString(j);
                InputParser inputParser = new InputParser(pipeline_name, sensor, input_config);
                String key = inputParser.get_key();
                pipeline_to_key.put(pipeline_name, key);
                if (!cache_parsers.containsKey(key)) {
                    cache_parsers.put(key, inputParser);
                }
            }
        }
    }

    public InputParser get_input_parser(String pipeline_name) {
        if (pipeline_to_key.containsKey(pipeline_name)) {
            String key = pipeline_to_key.get(pipeline_name);
            return cache_parsers.get(key);
        }
        return null;
    }
}
