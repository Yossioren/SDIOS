package com.SDIOS.ServiceControl.Service.Trackers;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SensorFetcher {
    public static final int MAXIMAL_SENSOR_TYPES = 50;
    private static final Map<Integer, List<Sensor>> sensorNumberToSensorList = new HashMap<>();

    public SensorFetcher(SensorManager mSensorManager) {
        if (sensorNumberToSensorList.isEmpty())
            init(mSensorManager);
    }

    public static <E, V> List<V> getList(Map<E, List<V>> map, E key) {
        return map.computeIfAbsent(key, k -> new LinkedList<>());
    }

    public static Bundle getSensorBundle(Sensor sensor) {
        Bundle bundle = new Bundle();
        bundle.putInt("getType", sensor.getType());
        bundle.putString("getName", sensor.getName());
        bundle.putString("getVendor", sensor.getVendor());
        return bundle;
    }

    private void init(SensorManager mSensorManager) {
        for (int sensor_identifier = 0; sensor_identifier < MAXIMAL_SENSOR_TYPES; sensor_identifier++) {
            List<Sensor> sensorList = mSensorManager.getSensorList(sensor_identifier);
            if (sensorList != null)
                getList(sensorNumberToSensorList, sensor_identifier).addAll(sensorList);
        }
    }

    public Sensor getSensor(Bundle bundle) {
        List<Sensor> sensorsList = getList(sensorNumberToSensorList, bundle.getInt("getType"));
        for (Sensor sensor : sensorsList)
            if (isEqualSensors(bundle, sensor))
                return sensor;
        return null;
    }

    private boolean isEqualSensors(Bundle bundle, Sensor sensor) {
        return bundle.getInt("getType") == sensor.getType() &&
                bundle.getString("getName").equals(sensor.getName()) &&
                bundle.getString("getVendor").equals(sensor.getVendor());
    }

    public void printSupported() {
        for (Map.Entry<Integer, List<Sensor>> e : sensorNumberToSensorList.entrySet()) {
            if (!e.getValue().isEmpty()) {
                Log.d("SensorFetcher", e.toString());
            }
        }
    }
}
