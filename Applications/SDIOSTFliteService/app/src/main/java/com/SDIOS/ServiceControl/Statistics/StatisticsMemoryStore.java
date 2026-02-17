package com.SDIOS.ServiceControl.Statistics;

import android.hardware.Sensor;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsMemoryStore implements StatisticsTracker {
    private static final Map<String, Double[]> statistics = new ConcurrentHashMap<>();
    private static long start_time = System.currentTimeMillis();

    public StatisticsMemoryStore() {
        statistics.put("version - SDIOS-Service: 1.1.0", new Double[]{1.1});
    }

    private static void increase_key(String key) {
        increase_key(key, 1);
    }

    private static void increase_key(String key, int amount) {
        if (!statistics.containsKey(key)) statistics.put(key, new Double[]{0.0});
        statistics.get(key)[0] += amount;
    }

    private static void decrease_key(String key) {
        if (!statistics.containsKey(key))
            statistics.put(key, new Double[]{0.0});
        else if (statistics.get(key)[0] > 0)
            statistics.get(key)[0]--;
    }

    @Override
    public long get_up_time() {
        return start_time;
    }

    @Override
    public void registerListener(String requesting_package_name, Sensor sensor) {
        increase_key(requesting_package_name + "-currentRegisteredSensors_" + sensor.getName());
        increase_key(requesting_package_name + "-totalRegisteredSensors_" + sensor.getName());
        increase_key("currentRegisteredSensors_" + sensor.getName());
        increase_key("totalRegisteredSensors_" + sensor.getName());
    }

    @Override
    public void unregisterListener(String requesting_package_name, Sensor sensor) {
        decrease_key("currentRegisteredSensors_" + sensor.getName());
        decrease_key(requesting_package_name + "-currentRegisteredSensors_" + sensor.getName());
    }

    @Override
    public void registerDetection(String sensor) {
        increase_key("detection_" + sensor);
    }

    @Override
    public void registerTimeDifference(String sensor, SensorEventSdios event) {
        long timestamp_ns = System.nanoTime() - event.timestamp;
        String key = "millisecondsDelay_" + sensor;
        if (!statistics.containsKey(key)) statistics.put(key, new Double[]{0.0});
        Double[] delay_holder = statistics.get(key);
        delay_holder[0] = delay_holder[0] * 0.8 + timestamp_ns * 0.2 * 1E-6;
    }

    @Override
    public void set_application_connection(String packageName) {
        increase_key(packageName);
    }

    @Override
    public void refuse_sensor(String requesting_package_name, boolean permitted_high_sampling, Sensor sensor) {
        if (permitted_high_sampling)
            increase_key(requesting_package_name + "-Refuse-sensor-" + sensor.getName());
        else
            increase_key(requesting_package_name + "-Refuse-high-sampling-sensors-" + sensor.getName());
    }

    @Override
    public void reset() {
        statistics.clear();
        start_time = System.currentTimeMillis();
    }

    private String time_duration_representation(long duration) {
        long seconds = duration / 1000;
        long minutes = 0, hours = 0;
        if (seconds > 60) {
            minutes = seconds / 60;
            seconds %= 60;
            if (minutes > 60) {
                hours = minutes / 60;
                minutes %= 60;
                if (hours > 24) {
                    long days = hours / 24;
                    hours %= 24;
                    return String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds);
                }
            }
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public String print_statistics() {
        String timeText = time_duration_representation(System.currentTimeMillis() - start_time);
        StringBuilder exported_content = new StringBuilder();
        exported_content.append("Up time: ");
        exported_content.append(timeText);
        List<Map.Entry<String, Double[]>> output = new ArrayList<>(statistics.entrySet());
        output.sort(Map.Entry.comparingByKey());
        for (Map.Entry<String, Double[]> entry : output) {
            exported_content.append("\n");
            exported_content.append(entry.getKey());
            exported_content.append(": ");
            exported_content.append(entry.getValue()[0]);
        }
        return exported_content.toString();
    }

    @Override
    public void dropped_events(String sensor, int size) {
        increase_key("droppedEvents_" + sensor, size);
    }
}
