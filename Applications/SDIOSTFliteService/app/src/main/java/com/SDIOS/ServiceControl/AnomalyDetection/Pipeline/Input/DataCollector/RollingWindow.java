package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector;

import java.util.ArrayList;
import java.util.List;

public class RollingWindow implements DataCollector {
    private final int windowSize;
    private final List<SensorEventSdios> values = new ArrayList<>();
    private int index;

    public RollingWindow(int windowSize) {
        this.windowSize = windowSize;
        reset();
    }

    @Override
    public void add(SensorEventSdios sensorEvent) {
        if (index == windowSize) {
            index = 0;
        }
        values.set(index, sensorEvent);
        index++;
    }

    @Override
    public List<SensorEventSdios> get() {
        List<SensorEventSdios> output = new ArrayList<>(values.subList(index, windowSize - 1));
        output.addAll(values.subList(0, index));
        return output;
    }

    @Override
    public void delete(int i) {
        // we will not need to delete with this implementation
    }

    @Override
    public void reset() {
        index = 0;
        values.clear();
        for (int i = 0; i < windowSize; i++) {
            values.add(null);
        }
    }
}
