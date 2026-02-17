package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector;

import java.util.List;

public interface DataCollector {
    void add(SensorEventSdios sensorEvent);

    List<SensorEventSdios> get();

    /* delete events from the 0 to i on the given get */
    void delete(int i);

    void reset();
}
