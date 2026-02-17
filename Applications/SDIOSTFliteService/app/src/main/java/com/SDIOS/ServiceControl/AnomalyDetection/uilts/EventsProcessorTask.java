package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.Evaluator;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.Statistics.StatisticsMemoryStore;
import com.SDIOS.ServiceControl.Statistics.StatisticsTracker;

import java.util.LinkedList;
import java.util.List;

public class EventsProcessorTask implements Runnable {
    private static final int REAL_MAX_QUEUE_SIZE = 1200;
    private final static StatisticsTracker statisticsTracker = new StatisticsMemoryStore();
    private static int MAX_QUEUE_SIZE = 300;
    private final String TAG = "EventsProcessorTask";
    private final List<SensorEventSdios> incoming_events = new ConcurrentLinkedList<>();
    private final List<SensorEventSdios> processed_events = new ConcurrentLinkedList<>();
    private final Evaluator evaluator;
    private boolean running = false;

    public EventsProcessorTask(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void registerEvent(SensorEventSdios mySensorEvent) {
        if (!running) return;
        incoming_events.add(mySensorEvent);
//        Log.e(TAG, incoming_events.size() + "A");
        if (incoming_events.size() >= MAX_QUEUE_SIZE) {
            Log.e(TAG, "Too many incoming events! Processing is too slow " + this.evaluator.toString());
            if (MAX_QUEUE_SIZE < REAL_MAX_QUEUE_SIZE) { // to handle better fast sensors
                MAX_QUEUE_SIZE *= 2;
                Log.w(TAG, "Increase queue max capacity to " + MAX_QUEUE_SIZE);
                return;
            }
            Log.e(TAG, "Dropped <" + incoming_events.size() + "> events! Reset evaluator!");
            statisticsTracker.dropped_events(evaluator.getSensorName(), incoming_events.size());
            incoming_events.clear();
            evaluator.reset();
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            while (!incoming_events.isEmpty() && running) {
//                Log.e(TAG, incoming_events.size() + "P");
                SensorEventSdios event = incoming_events.remove(0);
                if (event == null)
                    continue;
                if ((event = evaluator.ComputeTrust(event)) != null) {
//                    Log.e(TAG, processed_events.size() + "QP");
                    processed_events.add(event);
                    if (processed_events.size() >= MAX_QUEUE_SIZE) {
                        Log.e(TAG, "Too many processed events! distribute process is too slow");
                        Log.e(TAG, "Dropped processed events!");
                        processed_events.clear();
                    }
                }
            }
            Thread.yield();
        }
        reset();
    }

    private void reset() {
        Log.d(TAG, "reset queue");
        incoming_events.clear();
        processed_events.clear();
        evaluator.reset();
    }

    public void stop() {
        running = false;
    }

    public List<SensorEventSdios> export_processed_events() {
        if (processed_events.isEmpty())
            return null;
        List<SensorEventSdios> list = new LinkedList<>();
        while (!processed_events.isEmpty())
            list.add(processed_events.remove(0));
        return list;
    }
}
