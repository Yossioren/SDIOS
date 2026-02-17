package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEvent;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.EventsCreatorSdios;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.SensorFetcher;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.ListenerWrapper;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.OnSensorChangedSdiosService;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.RegisterSensorEventListenerCallback;

//The class is public to use in test
public class SensorManagerSdiosHelper implements OnSensorChangedSdiosService {
    protected static final String SENSOR_EVENT_LISTENER_INDEX_KEY = "SEL";
    private static final String TAG = "SDIOSLib-SM-SdiosHelper";
    private static final boolean use_fallback_on_unsupported = true;
    private final AtomicInteger connected_devices_count = new AtomicInteger(0);
    private final EventsCreatorSdios eventsCreator;
    private final SensorFetcher sensorFetcher;
    private final Map<SensorEventListener, Queue<Pair<String, Integer>>> listenerToSensorAndSEL = new HashMap<>();// for efficient removal
    private final Map<String, Queue<SensorEventListener>> sensorToListener = new HashMap<>();
    private final Map<SensorEventListener, ListenerWrapper> libraryListenerToAndroidListener = new HashMap<>();
    private final Context context;
    private final ServiceCommunicator mServiceCommunicator;
    private final SensorManager mFallbackSensorManager;
    private boolean service_failure = false;

    public SensorManagerSdiosHelper(@NonNull Context context, @NonNull SensorManager fallbackSensorManager) {
        mServiceCommunicator = new ServiceCommunicator(context, this);
        mFallbackSensorManager = fallbackSensorManager;
        sensorFetcher = new SensorFetcher(mFallbackSensorManager);
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
        if (sensor == null || listener == null) {
            Log.e(TAG, "register_fallback - Sensor or Listener are null!");
            return false;
        }
        Log.w(TAG, "Registering fallback listener for " +
                sensor.getName() + "_" + sensor.getType());
        putConnection(sensor, -1, listener);
        ListenerWrapper wrapped_listener;
        if (!libraryListenerToAndroidListener.containsKey(listener)) {
            wrapped_listener = new ListenerWrapper(listener);
            libraryListenerToAndroidListener.put(listener, wrapped_listener);
        } else wrapped_listener = libraryListenerToAndroidListener.get(listener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return this.mFallbackSensorManager.registerListener(wrapped_listener, sensor,
                    samplingPeriodUs, maxReportLatencyUs, handler);
        }
        return this.mFallbackSensorManager.registerListener(wrapped_listener, sensor, samplingPeriodUs);
    }

    @Override
    public void onSensorChanged(@NonNull Bundle bundle) {
        Sensor sensor = sensorFetcher.getSensor(bundle);
        assert sensor != null;
        Queue<SensorEventListener> listeners = sensorToListener.get(sensor.toString());
        if (listeners == null) return;
        for (SensorEventListener sensorEventListener : listeners) {
            SensorEvent sensorEvent = eventsCreator.createSensorEvent(sensor, bundle);
            sensorEventListener.onSensorChanged(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(@NonNull Bundle bundle) {
        Sensor sensor = sensorFetcher.getSensor(bundle);
        assert sensor != null;
        Queue<SensorEventListener> listeners = sensorToListener.get(sensor.toString());
        if (listeners == null) return;
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
                Log.e(TAG, "application sensor manage leak");// + invConnections.toString() + "_!_" + sensorsConnections.toString());
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
        Log.w(TAG, "Use unsupported registerListener(@Nullable SensorListener listener, int sensors)");
        if (use_fallback_on_unsupported)
            return mFallbackSensorManager.registerListener(listener, sensors);
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorListener listener, int sensors, int rate) {
        Log.w(TAG, "Use unsupported registerListener(@Nullable SensorListener listener, int sensors, int rate)");
        if (use_fallback_on_unsupported)
            return mFallbackSensorManager.registerListener(listener, sensors, rate);
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorListener listener) {
        Log.w(TAG, "Use unsupported unregisterListener(@Nullable SensorListener listener)");
        if (use_fallback_on_unsupported)
            mFallbackSensorManager.unregisterListener(listener);
        throw new UnsupportedOperationException("Deprecated method!");
    }

    /**
     * @deprecated
     */
    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorListener listener, int sensors) {
        Log.w(TAG, "Use unsupported unregisterListener(@Nullable SensorListener listener, int sensors)");
        if (use_fallback_on_unsupported)
            mFallbackSensorManager.unregisterListener(listener, sensors);
        throw new UnsupportedOperationException("Deprecated method!");
    }

    @SuppressLint("RegistrationName")
    private void unregisterListenerHelper(@NonNull Bundle bundle) {
        mServiceCommunicator.sendToService(
                ServiceMessagingParser.MSG_UNREGISTER_SENSOR, bundle);
        if (connected_devices_count.get() > 0 && connected_devices_count.decrementAndGet() == 0) {
            mServiceCommunicator.unbind();
        }
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor) {
        if (sensor == null) {
            Log.w(TAG, "Sensor is null - unregister all Listener sensors!");
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
        } else if (libraryListenerToAndroidListener.containsKey(listener)) {
            Log.d(TAG, "Removing fallback listener");
            ListenerWrapper wrapperListener = libraryListenerToAndroidListener.remove(listener);
            mFallbackSensorManager.unregisterListener(wrapperListener);
        } else {
            Log.w(TAG, "Did not found listener");
        }
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener) {
        if (listener == null) {
            Log.e(TAG, "unregisterListener - Listener is null!");
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
    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR_SDIOS, sensor, samplingPeriodUs, 50, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR, sensor, samplingPeriodUs, 50, listener);
    }

    @SuppressLint("ExecutorRegistration")
    private boolean registerHelper(int action_type, @Nullable Sensor sensor, int samplingPeriodUs,
                                   int maxReportLatencyUs, @Nullable SensorEventListener listener) {
        if (sensor == null || listener == null) {
            Log.e(TAG, "registerHelper - Sensor or Listener are null!");
            return false;
        }
        Log.d(TAG, "Register listener for " + sensor.getName() + "_" + sensor.getType());
        if (samplingPeriodUs == 0 && safe_test_has_permission()) {
            Log.e(TAG, "No permission for high sampling");
            return false;
        }
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
        mServiceCommunicator.sendPendingRequest(
                action_type, data, new RegisterSensorEventListenerCallback(
                        this, listener, sensor, samplingPeriodUs, maxReportLatencyUs));
        return true;
    }

    private boolean safe_test_has_permission() {
        try {
            return ContextCompat.checkSelfPermission(this.context, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_DENIED;
        } catch (Throwable e) {
            Log.e(TAG, "Fail to fetch high-sampling permission " + e);
            return false;
        }
    }

    @SuppressLint("ExecutorRegistration")
    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR_SDIOS, sensor, samplingPeriodUs, maxReportLatencyUs, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) {
        return registerHelper(ServiceMessagingParser.MSG_REGISTER_SENSOR, sensor, samplingPeriodUs, maxReportLatencyUs, listener);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, @Nullable Handler handler) {
        Log.w(TAG, "Use unsupported registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, @Nullable Handler handler)");
        if (use_fallback_on_unsupported)
            return this.register_fallback(sensor, samplingPeriodUs, 0, handler, listener);
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint({"ExecutorRegistration", "RegistrationName"})
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, @Nullable Handler handler) {
        Log.w(TAG, "Use unsupported registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, @Nullable Handler handler)");
        if (use_fallback_on_unsupported)
            return this.register_fallback(sensor, samplingPeriodUs, maxReportLatencyUs, handler, listener);
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean flush(@Nullable SensorEventListener listener) {
        Log.w(TAG, "Use unsupported flush(@Nullable SensorEventListener listener)");
        if (use_fallback_on_unsupported) {
            if (libraryListenerToAndroidListener.containsKey(listener))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return this.mFallbackSensorManager.flush(libraryListenerToAndroidListener.get(listener));
                }
            return false;
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable MemoryFile mem) {
        Log.w(TAG, "Use unsupported createDirectChannel(@Nullable MemoryFile mem)");
        if (use_fallback_on_unsupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                return this.mFallbackSensorManager.createDirectChannel(mem);
            else return null;
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable HardwareBuffer mem) {
        Log.w(TAG, "Use unsupported createDirectChannel(@Nullable HardwareBuffer mem)");
        if (use_fallback_on_unsupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                return this.mFallbackSensorManager.createDirectChannel(mem);
            else return null;
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    public void registerDynamicSensorCallback(@Nullable SensorManager.DynamicSensorCallback callback) {
        Log.w(TAG, "Use unsupported registerDynamicSensorCallback(@Nullable DynamicSensorCallback callback)");
        if (use_fallback_on_unsupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                this.mFallbackSensorManager.registerDynamicSensorCallback(callback);
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public void registerDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback, @Nullable Handler handler) {
        Log.w(TAG, "Use unsupported registerDynamicSensorCallback(@Nullable DynamicSensorCallback callback, @Nullable Handler handler)");
        if (use_fallback_on_unsupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                this.mFallbackSensorManager.registerDynamicSensorCallback(callback, handler);
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    public void unregisterDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback) {
        Log.w(TAG, "Use unsupported unregisterDynamicSensorCallback(@Nullable DynamicSensorCallback callback)");
        if (use_fallback_on_unsupported) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                this.mFallbackSensorManager.unregisterDynamicSensorCallback(callback);
        }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean requestTriggerSensor(@Nullable TriggerEventListener listener, @SuppressLint("ListenerLast") @Nullable Sensor sensor) {
        Log.w(TAG, "Use unsupported requestTriggerSensor(@Nullable TriggerEventListener listener, @Nullable Sensor sensor");
        if (use_fallback_on_unsupported)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                this.mFallbackSensorManager.requestTriggerSensor(listener, sensor);
            }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }

    @SuppressLint("ExecutorRegistration")
    public boolean cancelTriggerSensor(@Nullable TriggerEventListener listener, @SuppressLint("ListenerLast") @Nullable Sensor sensor) {
        Log.w(TAG, "Use unsupported cancelTriggerSensor(@Nullable TriggerEventListener listener, @Nullable Sensor sensor");
        if (use_fallback_on_unsupported)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                this.mFallbackSensorManager.cancelTriggerSensor(listener, sensor);
            }
        throw new UnsupportedOperationException("UnSupported in SDIOS");
    }
}
