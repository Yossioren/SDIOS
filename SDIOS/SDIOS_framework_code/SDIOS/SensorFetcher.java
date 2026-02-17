package android.hardware.SDIOS;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.hardware.SDIOS.NonNull;
import android.hardware.SDIOS.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SensorFetcher {
    public static final int MAXIMAL_SENSOR_TYPES = 50;
    private static final Map<Integer, List<Sensor>> sensorNumberToSensorList = new HashMap<>();

    public SensorFetcher(@NonNull SensorManager mSensorManager) {
        if (sensorNumberToSensorList.isEmpty())
            init(mSensorManager);
    }

    @NonNull
    static <E, V> List<V> getList(@NonNull Map<E, List<V>> m, E key) {
        List<V> list = m.get(key);
        if (list == null) {
            list = new LinkedList<>();
            m.put(key, list);
        }
        return list;
    }

    @NonNull
    public static Bundle getSensorBundle(@NonNull Sensor sensor) {
        Bundle bundle = new Bundle();
        bundle.putInt("getType", sensor.getType());
        bundle.putString("getName", sensor.getName());
        bundle.putString("getVendor", sensor.getVendor());
        return bundle;
    }

    private void init(@NonNull SensorManager mSensorManager) {
        for (int i = 0; i < MAXIMAL_SENSOR_TYPES; i++) {
            List<Sensor> sensorList = mSensorManager.getSensorList(i);
            if (sensorList != null)
                getList(sensorNumberToSensorList, i).addAll(sensorList);
        }
    }

    @Nullable
    public Sensor getSensor(@NonNull Bundle bundle) {
        List<Sensor> sensorsLst = getList(sensorNumberToSensorList, bundle.getInt("getType"));
        for (Sensor s : sensorsLst)
            if (isEqualSensors(bundle, s))
                return s;
        return null;
    }

    private boolean isEqualSensors(@NonNull Bundle bundle, @NonNull Sensor sensor) {
        return bundle.getInt("getType") == sensor.getType() &&
                bundle.getString("getName").equals(sensor.getName()) &&
                bundle.getString("getVendor").equals(sensor.getVendor());
    }
}
