package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers.Analyzer;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers.ThresholdAnalyzer;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.DefaultEvent;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.DoNothing;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.DropEvent;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.OnDetect;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.ZeroEvent;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResultAnalyzersManager {
    private final static String TAG = "ResultAnalyzersManager";
    private final static Map<String, Class<? extends Analyzer>> names_to_analyzer = new HashMap<String, Class<? extends Analyzer>>() {{
        put("threshold", ThresholdAnalyzer.class);
    }};
    private final static Map<String, Class<? extends OnDetect>> names_to_on_detect = new HashMap<String, Class<? extends OnDetect>>() {{
        put("block", DropEvent.class);
        put("default", DropEvent.class);
        put("default_event", DefaultEvent.class);
        put("nothing", DoNothing.class);
        put("zero_event", ZeroEvent.class);
    }};
    private final ClassifiersPackage package_config;
    private final Map<String, Analyzer> sensor_analyzers = new HashMap<>();
    private final Map<String, OnDetect> sensor_on_detect = new HashMap<>();

    public ResultAnalyzersManager(ClassifiersPackage package_config) {
        this.package_config = package_config;
        try {
            parse_analyzers();
        } catch (JSONException e) {
            Log.e(TAG, "JSON error " + e);
        }
    }

    private void parse_analyzers() throws JSONException {
/*"anomaly_detectors": {
  "gyroscope": "analyzer": {
                    "method": "threshold",
                    "parameters": {
                        "method": "sum_loss_on_all_classifiers",
                        "pipelines": ["x_pipeline","y_pipeline","z_pipeline","l2norm_pipeline"],
                        "loss": "MeanSquaredError"},
                    "user_config": ...
              }
              "on_detect_action": {
                  "method": "block",
                  "parameters": {"trust_level": 0.9},
                  "user_config": ...
              }
}*/
        JSONObject analyzers = package_config.origin_json.getJSONObject("anomaly_detectors");
        for (Iterator<String> it = analyzers.keys(); it.hasNext(); ) {
            String sensor = it.next();
            if (sensor == null || sensor.equals("")) {
                continue;
            }

            JSONObject sensorDetector = analyzers.getJSONObject(sensor);
            JSONObject sensorDetectorAnalyzer = sensorDetector.getJSONObject("analyzer");
            JSONObject sensorDetectorOnDetect = sensorDetector.getJSONObject("on_detect_action");
            UserConfigManager userConfigManagerAnalyzer = new UserConfigManager(sensorDetectorAnalyzer, TAG, "analyzer", sensor);
            UserConfigManager userConfigManagerOnDetect = new UserConfigManager(sensorDetectorOnDetect, TAG, "on_detect_action", sensor);
            assert names_to_analyzer.containsKey(userConfigManagerAnalyzer.method);
            assert names_to_on_detect.containsKey(userConfigManagerOnDetect.method);
            try {
                sensor_analyzers.put(sensor, names_to_analyzer.get(userConfigManagerAnalyzer.method).getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManagerAnalyzer));
                sensor_on_detect.put(sensor, names_to_on_detect.get(userConfigManagerOnDetect.method).getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManagerOnDetect));
            } catch (InvocationTargetException e) {
                Log.e(TAG, "InvocationTargetException " + e + ", " + e.getTargetException());
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                Log.e(TAG, "Reflection error " + sensor + ": " + e);
            }
        }
    }

    public Analyzer<?> get_analyzer(String sensor) {
        return sensor_analyzers.getOrDefault(sensor, null);
    }

    public OnDetect get_on_detect(String sensor) {
        return sensor_on_detect.getOrDefault(sensor, null);
    }

    public Collection<String> get_supported_sensors() {
        return sensor_analyzers.keySet();
    }
}
