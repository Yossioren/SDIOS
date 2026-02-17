package android.hardware.SDIOS.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.HardwareBuffer;
import android.hardware.Sensor;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.MemoryFile;
import android.util.Log;
import android.util.Pair;

import android.hardware.SDIOS.NonNull;
import android.hardware.SDIOS.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.hardware.SensorEventSdios;
import android.hardware.SensorEventListener;
import android.hardware.SDIOS.EventsCreatorSdios;
import android.hardware.SDIOS.SensorFetcher;
import android.hardware.SDIOS.SystemSensorManagerSdios;
import android.hardware.SDIOS.listener_utils.OnSensorChangedSdiosService;
import android.hardware.SDIOS.listener_utils.RegisterSensorEventListenerCallback;

public class SensorManagerSdiosHelper implements OnSensorChangedSdiosService {
    public static final String SENSOR_EVENT_LISTENER_INDEX_KEY = "SEL";
    private static final String TAG = "Framework-SM-SdiosHelper";
    private static final AtomicInteger connected_devices_count = new AtomicInteger(0);
    private final ServiceCommunicator mServiceCommunicator;
    private final EventsCreatorSdios eventsCreator;
    private final SensorFetcher sensorFetcher;
    // This map is used for efficient removal
    private final Map<SensorEventListener, Queue<Pair<String, Integer>>> listenerToSensorAndSEL = new HashMap<>();
    private final Map<String, Queue<SensorEventListener>> sensorToListener = new HashMap<>();
    private final Context context;
    private final FallbackSensorManager mFallbackSensorManager;
    private boolean service_failure = false;

    public SensorManagerSdiosHelper(@NonNull Context context, @NonNull SensorManager sensorManager,
                                  @NonNull FallbackSensorManager fallbackSensorManager) {
        mServiceCommunicator = new ServiceCommunicator(context, this);
        mFallbackSensorManager = fallbackSensorManager;
        sensorFetcher = new SensorFetcher(sensorManager);
        eventsCreator = new EventsCreatorSdios();
        this.context = context;
    }

    private static <E, V> Queue<V> getQueue(@NonNull Map<E, Queue<V>> m, E key) {
        Queue<V> list = m.get(key);
        if (list == null) {
            list = new ConcurrentLinkedQueue<>();
            m.put(key, list);
        }
        return list;
    }

    @SuppressLint("ExecutorRegistration")
    public boolean register_fallback(@Nullable Sensor sensor, int samplingPeriodUs,
                                     int maxReportLatencyUs, @Nullable Handler handler, @Nullable SensorEventListener listener) {
        if (sensor == null || listener == null)
            return false;
        Log.w(TAG, "Registering fallback listener for " +
                sensor.getName() + "_" + sensor.getType());
        putConnection(sensor, -1, listener);
        return this.mFallbackSensorManager.registerListenerFallback(
                listener, sensor, samplingPeriodUs, null, maxReportLatencyUs, 0);
    }

    @Override
    public void onSensorChanged(@NonNull Bundle bundle) {
        Sensor sensor = sensorFetcher.getSensor(bundle);
        assert sensor != null;
        Queue<SensorEventListener> listeners = sensorToListener.get(sensor.toString());
        if (listeners == null)
            return;
        for (SensorEventListener sensorEventListener : listeners) {
            SensorEventSdios sensorEventSdios = eventsCreator.createSensorEventSdios(sensor, bundle);
            sensorEventListener.onSensorChanged(sensorEventSdios);
        }
    }

    @Override
    public void onAccuracyChanged(@NonNull Bundle bundle) {
        Sensor sensor = sensorFetcher.getSensor(bundle);
        assert sensor != null;
        Queue<SensorEventListener> listeners = sensorToListener.get(sensor.toString());
        if (listeners == null)
            return;
        for (SensorEventListener sensorEventListener : listeners)
            sensorEventListener.onAccuracyChanged(sensor, bundle.getInt("accuracy"));
    }

    private Queue<Pair<String, Integer>> delConnection(@NonNull SensorEventListener listener) {
        Queue<Pair<String, Integer>> indexes = listenerToSensorAndSEL.remove(listener);
        if (indexes != null) {
            for (Pair<String, Integer> pair : indexes) {
                Queue<SensorEventListener> listeners = sensorToListener.get(pair.first);
                assert listeners != null;
                listeners.remove(listener);
            }
        } else
            Log.d(TAG, "not registered in inv");
        return indexes;
    }

    private int delConnection(@NonNull SensorEventListener listener, @NonNull Sensor sensor) {
        String sensor_key = sensor.toString();
        Queue<SensorEventListener> listeners = sensorToListener.get(sensor_key);
        if (listeners != null && !listeners.isEmpty()) {
            listeners.remove(listener);
            Queue<Pair<String, Integer>> sensors = listenerToSensorAndSEL.get(listener);
            if (sensors == null) {
                Log.e(TAG, "application sensor manage leak");
                return -1;
            }
            Iterator<Pair<String, Integer>> iterator = sensors.iterator();
            while (iterator.hasNext()) {
                Pair<String, Integer> pair = iterator.next();
                if (sensor_key.equals(pair.first)) {
                    iterator.remove();
                    if (sensors.isEmpty())
                        listenerToSensorAndSEL.remove(listener);
                    return pair.second;
                }
            }
        }
        return -1;
    }

    @SuppressLint("ExecutorRegistration")
    public void putConnection(@NonNull Sensor sensor, int sensor_listener_key, @NonNull SensorEventListener listener) {
        String sensor_key = sensor.toString();
        Queue<SensorEventListener> listeners = getQueue(sensorToListener, sensor_key);
        Queue<Pair<String, Integer>> sensorsForListener = getQueue(listenerToSensorAndSEL, listener);
        listeners.add(listener);
        connected_devices_count.incrementAndGet();
        sensorsForListener.add(new Pair<>(sensor_key, sensor_listener_key));
    }

    public boolean isRegistered(@Nullable SensorEventListener listener) {
        return listenerToSensorAndSEL.containsKey(listener);
    }

    public int countConnected() {
        return listenerToSensorAndSEL.size();
    }

    public int countConnected2() {
        return connected_devices_count.get();
    }

    @SuppressLint("ExecutorRegistration")
    public boolean isRegistered(@NonNull Sensor sensor, @NonNull SensorEventListener listener) {
        String sensor_key = sensor.toString();
        if (!sensorToListener.containsKey(sensor_key))
            return false;
        Queue<SensorEventListener> listeners = getQueue(sensorToListener, sensor_key);
        return listeners.contains(listener);
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorListener listener, int sensors) {
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorListener listener, int sensors, int rate) {
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorListener listener) {
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorListener listener, int sensors) {
        throw new UnsupportedOperationException("Deprecated method!");
    }

    @SuppressLint("RegistrationName")
    private void unregisterListenerHelper(@NonNull Bundle bundle) {
        mServiceCommunicator.sendToService(ServiceMessagingParser.MSG_UNREGISTER_SENSOR, bundle);
        if (connected_devices_count.get() > 0 && connected_devices_count.decrementAndGet() == 0) {
            mServiceCommunicator.unbind();
        }
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor) {
        if (sensor == null) {
            unregisterListener(listener);
            return;
        }
        if (listener == null) {
            Log.e(TAG, "unregisterListener2 - Listener is null!");
            return;
        }
        Log.d(TAG, "Unregister listener for " + sensor.getName() + "_" + sensor.getType());
        int sensor_listener_key = delConnection(listener, sensor);
        if (sensor_listener_key != -1) {
            Bundle bundle = new Bundle();
            bundle.putInt(SENSOR_EVENT_LISTENER_INDEX_KEY, sensor_listener_key);
            unregisterListenerHelper(bundle);
            Log.d(TAG, "delete sensor_listener_key " + sensor_listener_key);
        } else {
            Log.d(TAG, "Removing fallback listener");
            mFallbackSensorManager.unregisterListenerFallback(listener, sensor);
        }
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener) {
        if (listener == null) {
            Log.e(TAG, "unregisterListener - listener is null!");
            return;
        }
        Log.d(TAG, "Unregister all listener registrations");
        Bundle bundle = new Bundle();
        Queue<Pair<String, Integer>> listener_registrations = delConnection(listener);
        if (listener_registrations != null) {// check if it is registered
            for (Pair<String, Integer> sensor_and_listener_key_pair : listener_registrations) {
                bundle.putInt(SENSOR_EVENT_LISTENER_INDEX_KEY, sensor_and_listener_key_pair.second);
                unregisterListenerHelper(bundle);
            }
        }
    }

    @SuppressLint("RegistrationName")
    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                       int samplingPeriodUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR_SDIOS, sensor, samplingPeriodUs, 50, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                    int samplingPeriodUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR, sensor, samplingPeriodUs, 50, listener);
    }

    @SuppressLint("ExecutorRegistration")
    private boolean registerHelper(int action_type, @Nullable Sensor sensor, int samplingPeriodUs,
                                   int maxReportLatencyUs, @Nullable SensorEventListener listener) {
        if (sensor == null || listener == null) {
            Log.e(TAG, "registerHelper - Sensor or listener are null!");
            return false;
        }
        Log.d(TAG, "Register listener for " + sensor.getName() + "_" + sensor.getType());
        if (service_failure) {
            return register_fallback(sensor, samplingPeriodUs, maxReportLatencyUs, null, listener);
        }
        Bundle data = SensorFetcher.getSensorBundle(sensor);
        data.putInt("samplingPeriodUs", samplingPeriodUs);
        data.putInt("maxReportLatencyUs", maxReportLatencyUs);
        if (connected_devices_count.get() == 0) {
            try {
                mServiceCommunicator.bind();
            } catch (RuntimeException exception) {
                Log.e(TAG, "Failed to bind service, using fallback: " + exception);
                service_failure = true;
                return register_fallback(sensor, samplingPeriodUs, maxReportLatencyUs, null, listener);
            }
        }
        mServiceCommunicator.sendPendingRequest(action_type, data,
                new RegisterSensorEventListenerCallback(this, listener, sensor, samplingPeriodUs, maxReportLatencyUs));
        return true;
    }

    @SuppressLint("ExecutorRegistration")
    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                       int samplingPeriodUs, int maxReportLatencyUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR_SDIOS, sensor, samplingPeriodUs,
                maxReportLatencyUs, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                    int samplingPeriodUs, int maxReportLatencyUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR, sensor, samplingPeriodUs,
                maxReportLatencyUs, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                    int samplingPeriodUs, @Nullable Handler handler) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint({"ExecutorRegistration", "RegistrationName"})
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                    int samplingPeriodUs, int maxReportLatencyUs, @Nullable Handler handler) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean flush(@Nullable SensorEventListener listener) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable MemoryFile mem) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable HardwareBuffer mem) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    public void registerDynamicSensorCallback(@Nullable SensorManager.DynamicSensorCallback callback) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public void registerDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback,
                                              @Nullable Handler handler) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    public void unregisterDynamicSensorCallback(
            @Nullable android.hardware.SensorManager.DynamicSensorCallback callback) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean requestTriggerSensor(@Nullable TriggerEventListener listener,
                                        @SuppressLint("ListenerLast") @Nullable Sensor sensor) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean cancelTriggerSensor(@Nullable TriggerEventListener listener,
                                       @SuppressLint("ListenerLast") @Nullable Sensor sensor) {
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }
}