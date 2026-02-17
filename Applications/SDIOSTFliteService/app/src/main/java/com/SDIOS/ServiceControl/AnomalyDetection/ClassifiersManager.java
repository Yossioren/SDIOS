package com.SDIOS.ServiceControl.AnomalyDetection;

import android.hardware.Sensor;
import android.util.Log;

import androidx.annotation.Nullable;

import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class ClassifiersManager {
    public final static Map<String, Integer> sensorNameToType = new HashMap<String, Integer>() {{
        put("gyroscope", Sensor.TYPE_GYROSCOPE);
        put("accelerometer", Sensor.TYPE_ACCELEROMETER);
        put("magnetometer", Sensor.TYPE_MAGNETIC_FIELD);
    }};
    private final static String TAG = "ClassifiersManager";
    private final SensorClassifier[] supportedSdios = new SensorClassifier[50];
    private final PackageParser packageParser;

    public ClassifiersManager(ClassifiersPackage ModelConfiguration) throws JSONException {
        this.packageParser = new PackageParser(ModelConfiguration);
        load_classifiers();
    }

    private void load_classifiers() {
        for (Map.Entry<String, Integer> sensor_name_type_entry : sensorNameToType.entrySet()) {
            String sensor_name = sensor_name_type_entry.getKey();
            if (packageParser.resultAnalyzersManager.get_supported_sensors().contains(sensor_name)) {
                supportedSdios[sensor_name_type_entry.getValue()] = new SensorClassifier(sensor_name, packageParser);
            } else {
                Log.e(TAG, sensor_name + " Has no supported defending model");
            }
        }
    }

    public Evaluator get_by_name(String name) {
        if (sensorNameToType.containsKey(name)) {
            return extract_by_type(sensorNameToType.get(name.toLowerCase()));
        }
        return null;
    }

    @Nullable
    private SensorClassifier extract_by_type(Integer type) {
        if (type != null && type >= 0 && type < supportedSdios.length)
            return supportedSdios[type];
        Log.e(TAG, "No such evaluator type!! " + type);
        return null;
    }

    public Evaluator get(Sensor mSensor) {
        int type = mSensor.getType();
        return extract_by_type(type);
    }
}
