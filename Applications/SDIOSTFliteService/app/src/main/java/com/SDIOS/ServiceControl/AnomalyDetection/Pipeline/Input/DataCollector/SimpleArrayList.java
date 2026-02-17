package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector;

import java.util.ArrayList;
import java.util.List;

public class SimpleArrayList implements DataCollector {
    private final List<SensorEventSdios> values = new ArrayList<>();

    public SimpleArrayList() {
    }

    @Override
    public void add(SensorEventSdios sensorEvent) {
        values.add(sensorEvent);
    }

    @Override
    public List<SensorEventSdios> get() {
        return values;
    }

    @Override
    public void delete(int i) {
        values.subList(0, i).clear();
    }

    @Override
    public void reset() {
        values.clear();
    }
}
