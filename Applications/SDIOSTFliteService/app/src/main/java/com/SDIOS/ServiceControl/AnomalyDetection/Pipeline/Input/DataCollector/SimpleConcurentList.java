package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.ConcurrentLinkedList;

import java.util.List;

public class SimpleConcurentList implements DataCollector {
    private final ConcurrentLinkedList<SensorEventSdios> values = new ConcurrentLinkedList<>();

    public SimpleConcurentList() {
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
        values.remove_subset(0, i);
    }

    @Override
    public void reset() {
        values.clear();
    }
}
