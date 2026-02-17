package com.SDIOS.ServiceControl.Service.Trackers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.Evaluator;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.EventsProcessorTask;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.ThreadController;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigUpdateCallback;
import com.SDIOS.ServiceControl.Service.SensorEventHandler;
import com.SDIOS.ServiceControl.Service.ServiceConstants;
import com.SDIOS.ServiceControl.Service.UidDictionary;
import com.SDIOS.ServiceControl.Statistics.StatisticsMemoryStore;
import com.SDIOS.ServiceControl.Statistics.StatisticsTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

//Will update all the sensors for upcoming events
public class SensorReporter extends UserConfigUpdateCallback implements SensorEventListener {
    private final static String TAG = "SensorReporter";
    private final List<Messenger> replyToList = new CopyOnWriteArrayList<>();
    private final List<Boolean> isSDIOSList = new CopyOnWriteArrayList<>();
    private final SensorEventHandler handler;
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private final StatisticsTracker statisticsTracker = new StatisticsMemoryStore();
    private final ThreadController event_looper = new ThreadController();
    private final AtomicInteger SDIOSCount = new AtomicInteger(0);
    private final EventsProcessorTask eventsProcessorTask;
    private Evaluator evaluator;
    private boolean registered = false;
    private boolean enabled = true;

    public SensorReporter(SensorEventHandler handler, SensorManager sensorManager, Sensor sensor, Evaluator evaluator) throws JSONException {
        super(new UserConfigManager("core_"));
        this.handler = handler;
        this.sensorManager = sensorManager;
        this.evaluator = evaluator;
        this.sensor = sensor;
        eventsProcessorTask = new EventsProcessorTask(evaluator);
    }

    @Override
    public void load_parameters(JSONObject parameters) {
        enabled = "enabled".equals(parameters.optString("enabled", "enabled"));
    }

    private void report(int action, Bundle bundle) {
        Message result = Message.obtain(null, action);
        result.obj = bundle;

        for (Messenger replyTo : replyToList)
            this.handler.reply(replyTo, result);
    }

    private void reportSdios(Bundle bundle, Bundle bSdios) {
        Message result = Message.obtain(null, ServiceConstants.MSG_ON_SENSOR_CHANGED);
        result.obj = bundle;

        Message resultSdios = Message.obtain(null, ServiceConstants.MSG_ON_SENSOR_CHANGED);
        resultSdios.obj = bSdios;

        Iterator<Messenger> iterM = replyToList.iterator();
        Iterator<Boolean> iterB = isSDIOSList.iterator();
        while (iterM.hasNext()) {
            if (iterB.next())
                this.handler.reply(iterM.next(), resultSdios);
            else
                this.handler.reply(iterM.next(), result);
        }
    }

    public boolean addListener(Messenger replyTo, Bundle bundle, boolean isSdios) {
        if (!registered) {
            // We should be able to update it just as android is doing bundle.getInt("maxReportLatencyUs"), bundle.getInt("samplingPeriodUs")
            if (!sensorManager.registerListener(this, sensor,
                    bundle.getInt("samplingPeriodUs"), bundle.getInt("maxReportLatencyUs"))) {
                Log.e(TAG, "Unable to register " + sensor.getName());
                return false;
            }
            event_looper.wait_for_previous_close_blocking();
            event_looper.run(eventsProcessorTask);
            event_looper.wait_for_thread_blocking();
            assert event_looper.is_status_running();
            registered = true;
        }
        if (isSdios && (evaluator == null))
            return false;
        statisticsTracker.registerListener(UidDictionary.getPackage(replyTo), sensor);
        if (isSdios)
            SDIOSCount.incrementAndGet();
        isSDIOSList.add(isSdios);
        replyToList.add(replyTo);
        return true;
    }

    public void remListener(Messenger replyTo) {
        int index = replyToList.indexOf(replyTo);
        if (index == -1)
            return;
        statisticsTracker.unregisterListener(UidDictionary.getPackage(replyTo), sensor);
        if (isSDIOSList.get(index))
            SDIOSCount.decrementAndGet();
        isSDIOSList.remove(index);
        Log.d(TAG, "Rem from listener " + replyToList.remove(replyTo));
        if (registered && replyToList.size() == 0) {
            sensorManager.unregisterListener(this, sensor);
            registered = false;
            eventsProcessorTask.stop();
            event_looper.close();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Bundle bundle = SensorFetcher.getSensorBundle(sensorEvent.sensor);
        if (!enabled) {
            report_event_bundle(bundle, new SensorEventSdios(sensorEvent));
            return;
        }
        if (!event_looper.is_status_running()) {
            if (event_looper.is_status_failed()) {
                Log.e(TAG, "event_looper thread crashed!");
                throw new AssertionError("event_looper thread crashed!");
            }
            Log.w(TAG, "event_looper thread not running!");
            return;
        }
        eventsProcessorTask.registerEvent(new SensorEventSdios(sensorEvent));
        List<SensorEventSdios> events = eventsProcessorTask.export_processed_events();
        if (events == null)
            return;
        for (SensorEventSdios event : events) {
            // Merge events reports to one bundle may improve performance
            report_event_bundle(bundle, event);
        }
    }

    private void report_event_bundle(Bundle bundle, SensorEventSdios event) {
        bundle.putInt("accuracy", event.accuracy);
        bundle.putLong("timestamp", event.timestamp);
        bundle.putFloatArray("values", event.values);
        if (SDIOSCount.get() > 0) {
            Bundle bSdios = (Bundle) bundle.clone();
            bSdios.putFloat("trust", event.trust);
            //Log.i(TAG, bSdios.toString());            //Log.i(TAG, ""+bSdios.getFloat("trust"));
            reportSdios(bundle, bSdios);
        } else {
            report(ServiceConstants.MSG_ON_SENSOR_CHANGED, bundle);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Bundle bundle = SensorFetcher.getSensorBundle(sensor);
        bundle.putInt("i", i);
        report(ServiceConstants.MSG_ON_ACCURACY_CHANGED, bundle);
    }

    public void set_evaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }
}