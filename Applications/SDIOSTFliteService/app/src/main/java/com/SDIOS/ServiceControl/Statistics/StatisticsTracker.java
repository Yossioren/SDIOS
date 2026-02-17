package com.SDIOS.ServiceControl.Statistics;

import android.hardware.Sensor;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;

public interface StatisticsTracker {
    long get_up_time();

    void registerListener(String requesting_package_name, Sensor sensor);

    void unregisterListener(String requesting_package_name, Sensor sensor);

    void registerDetection(String sensor);

    void registerTimeDifference(String sensor, SensorEventSdios event);

    void reset();

    String print_statistics();

    void set_application_connection(String packageName);

    void refuse_sensor(String requesting_package_name, boolean permitted_high_sampling, Sensor sensor);

    void dropped_events(String sensor, int size);
}
