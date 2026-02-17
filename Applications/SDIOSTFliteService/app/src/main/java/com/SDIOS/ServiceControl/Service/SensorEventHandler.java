package com.SDIOS.ServiceControl.Service;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.ClassifiersManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Evaluator;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UpdateObserver;
import com.SDIOS.ServiceControl.ConfigurationManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;
import com.SDIOS.ServiceControl.Service.Trackers.SensorFetcher;
import com.SDIOS.ServiceControl.Service.Trackers.SensorReporter;
import com.SDIOS.ServiceControl.Statistics.StatisticsMemoryStore;
import com.SDIOS.ServiceControl.Statistics.StatisticsTracker;
import com.SDIOS.ServiceControl.utils.FileUtils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler of incoming messages from clients.
 */
public class SensorEventHandler extends Handler {
    private final static StatisticsTracker statisticsTracker = new StatisticsMemoryStore();
    private static final String TAG = "SensorEventDistributor";
    private static final String SENSOR_EVENT_LISTENER_KEY = "SEL";
    //uid->sensors->listener
    private static final AtomicInteger connectedCount = new AtomicInteger(0);
    private static final AtomicInteger rollingListenerIndex = new AtomicInteger(0);
    private static final Map<Messenger, List<Integer>> messengerUidToIndexes = new HashMap<>();
    private static final Map<Integer, SensorReporter> connections = new HashMap<>();
    private static final Map<Sensor, SensorReporter> sensorsListeners = new HashMap<>();
    //we estimate max approx. 128 connections in parallel due to limitation from the OS
    private static SensorManager sensorManager;
    private static SensorFetcher sensorFetcher;
    private final ConfigurationManager configurationManager;
    private ClassifiersManager classifiersManager;

    @SuppressLint("WrongConstant")
    public SensorEventHandler(Context context) {
        Log.d(TAG, "connecting service");
        configurationManager = new ConfigurationManager(new FileUtils(context));
        try {
            load_model();
        } catch (Throwable e) {
            Log.e(TAG, "Fail booting up... closing");
            throw new AssertionError("Fail booting up... closing!");
        }
        sensorManager = (SensorManager) context.getSystemService(ServiceConstants.SENSOR_SERVICE_RAW);//deploy on my OS
        if (sensorManager == null) {
            Log.w(TAG, "Can not get service_raw -> we are not on SDIOS-OS");
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);//to test on regular OS
        }
        Log.d(TAG, "init SensorFetcher");
        sensorFetcher = new SensorFetcher(sensorManager);
        sensorFetcher.printSupported();
    }

    //Kill all the uid messengers
    private static void removeMessenger(Messenger replyTo) {
        if (replyTo == null || !messengerUidToIndexes.containsKey(replyTo))
            return;
        List<Integer> SensorEventListenerIndexes = messengerUidToIndexes.remove(replyTo);
        if (SensorEventListenerIndexes == null) return;
        for (int sel_to_remove : SensorEventListenerIndexes) {
            SensorReporter sensorReporter = connections.remove(sel_to_remove);
            connectedCount.getAndDecrement();
            if (sensorReporter != null)
                sensorReporter.remListener(replyTo);
            else
                Log.e(TAG, "SensorEventListener NOT FOUND " + connections + " " + sel_to_remove);
        }
    }

    private void load_model() {
        try {
            ClassifiersPackage Configurations = configurationManager.getCurrentPackage();
            classifiersManager = new ClassifiersManager(Configurations);
            for (Map.Entry<Sensor, SensorReporter> sensorReporterEntry : sensorsListeners.entrySet()) {
                Evaluator evaluator = classifiersManager.get(sensorReporterEntry.getKey());
                assert evaluator != null;
                sensorReporterEntry.getValue().set_evaluator(evaluator);
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error cannot boot SDIOS " + e);
        }
    }

    @Override
    public void handleMessage(Message message) {
        UidDictionary.set(message);
        switch (message.what) {
            case ServiceConstants.MSG_REGISTER_SENSOR:
                handleRegister(message, false);
            case ServiceConstants.MSG_KEEP_ALIVE:
                break;
            case ServiceConstants.MSG_UNREGISTER_SENSOR_Sdios:
            case ServiceConstants.MSG_UNREGISTER_SENSOR:
                handleUnregister(message);
                break;
            case ServiceConstants.MSG_REGISTER_SENSOR_Sdios:
                handleRegister(message, true);
                break;
            case ServiceConstants.MSG_ENABLE:
            case ServiceConstants.MSG_DISABLE:
            case ServiceConstants.MSG_UPDATE_USER_CONFIGURATIONS:
                if (UidDictionary.isPrivileged(message.sendingUid)) {
                    Log.i(TAG, "Update user conf:");
                    UpdateObserver.updateUserConfigurations();
                }
                break;
            case ServiceConstants.MSG_CHANGE_PACKAGE:
                if (UidDictionary.isPrivileged(message.sendingUid)) {
                    Log.i(TAG, "Update conf:");
                    load_model();
                }
                break;
            default:
                Log.e(TAG, "Unknown message what: " + message.what);
                super.handleMessage(message);
                break;
        }
    }

    private SensorReporter getSensorReporter(Sensor mSensor) {
        if (mSensor == null || classifiersManager == null)
            return null;
        Evaluator evaluator = classifiersManager.get(mSensor);
        if (evaluator == null)
            return null;
        SensorReporter sensorReporter = sensorsListeners.get(mSensor);
        if (sensorReporter == null) {
            try {
                sensorReporter = new SensorReporter(this, sensorManager, mSensor, evaluator);
                sensorsListeners.put(mSensor, sensorReporter);
            } catch (JSONException e) {
                Log.e(TAG, "JSONObject: " + e);
            }
        }
        return sensorReporter;
    }

    private void handleRegister(Message message, boolean isSdios) {
        if (message == null || message.obj == null) {
            return;
        }
        Bundle bundle = (Bundle) message.obj;
        Sensor mSensor = sensorFetcher.getSensor(bundle);
        if (mSensor == null) {
            Log.w(TAG, "Deny cannot decode bundle required sensor " + bundle.keySet());
            replyRefuse(message.replyTo, bundle);
            return;
        }
        Log.d(TAG, "Register request for sensor " + mSensor);
        registerSensor(message, isSdios, bundle, mSensor);
    }

    private void registerSensor(Message message, boolean isSdios, Bundle bundle, Sensor sensor) {
        boolean permitted_high_sampling = bundle.getInt("samplingPeriodUs") != 0 ||
                UidDictionary.allowedHighSamplingRate(message.sendingUid);
        SensorReporter sensorReporter = getSensorReporter(sensor);
        Bundle out_bundle = new Bundle();
        out_bundle.putInt("requestID", bundle.getInt("requestID"));
        if (sensorReporter != null && permitted_high_sampling &&
                sensorReporter.addListener(message.replyTo, bundle, isSdios)) {
            int unique_sensor_index = rollingListenerIndex.getAndIncrement();
            connections.put(unique_sensor_index, sensorReporter);
            SensorFetcher.getList(messengerUidToIndexes, message.replyTo).add(unique_sensor_index);
            replySuccess(message.replyTo, out_bundle, unique_sensor_index);
            connectedCount.getAndIncrement();
        } else {
            Log.w(TAG, "Deny " + sensor.getName() + " " + isSdios + " sensorReporter==null?" + (sensorReporter == null));
            statisticsTracker.refuse_sensor(UidDictionary.getPackage(message.replyTo), permitted_high_sampling, sensor);
            replyRefuse(message.replyTo, out_bundle);
        }
    }

    private void replySuccess(Messenger replyTo, Bundle bundle, int unique_sensor_index) {
        Log.d(TAG, "success registering!");
        Message result = Message.obtain(null, ServiceConstants.MSG_ACCEPT);
        bundle.putInt(SENSOR_EVENT_LISTENER_KEY, unique_sensor_index);
        result.obj = bundle;
        reply(replyTo, result);
    }

    private void replyRefuse(Messenger replyTo, Bundle bundle) {
        Log.d(TAG, "Refuse sensor registering!");
        Message result = Message.obtain(null, ServiceConstants.MSG_REFUSE);
        result.obj = bundle;
        reply(replyTo, result);
    }

    private void handleUnregister(Message message) {
        Bundle bundle = (Bundle) message.obj;
        if (!SensorFetcher.getList(messengerUidToIndexes, message.replyTo).
                remove((Integer) bundle.getInt(SENSOR_EVENT_LISTENER_KEY))) // security app can reg/unreg just the listeners to itself
            return;
        SensorReporter sensorReporter = connections.remove(bundle.getInt(SENSOR_EVENT_LISTENER_KEY));
        connectedCount.decrementAndGet();
        if (sensorReporter != null)
            sensorReporter.remListener(message.replyTo);
        else
            Log.e(TAG, "SensorEventListener NOT FOUND " + connections + " " + bundle.getInt(SENSOR_EVENT_LISTENER_KEY));
    }

    public void reply(Messenger replyTo, Message result) {
        try {
            replyTo.send(result);
        } catch (RemoteException e) {
            Log.e(TAG, "reply exception: " + connectedCount.get() + " " + e);
            removeMessenger(replyTo);
        }
    }
}