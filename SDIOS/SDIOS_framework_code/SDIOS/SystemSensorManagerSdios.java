package android.hardware.SDIOS;

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
import android.os.Handler;
import android.os.Looper;
import android.os.MemoryFile;
import android.util.Log;

import android.hardware.SDIOS.NonNull;
import android.hardware.SDIOS.Nullable;

import android.hardware.SensorEventListener;
import android.hardware.SystemSensorManager;
import android.hardware.SDIOS.service.FallbackSensorManager;
import android.hardware.SDIOS.service.SensorManagerSdiosHelper;

/**
 * Sensor manager implementation that communicates with
 * a built-in analyzing app - SDIOS service
 *
 * @hide
 */
public class SystemSensorManagerSdios extends SystemSensorManager implements FallbackSensorManager {
    private static final String TAG = "Framework-SM-SDIOS";
    private final @Nullable SensorManagerSdiosHelper mSensorManagerSdiosHelper;
    private final boolean isAppInstalled, isSDIOS = true;
    private final @NonNull Context context;

    @SuppressLint("WrongConstant")
    public SystemSensorManagerSdios(Context context, Looper mainLooper) {
        super(context, mainLooper);
        isAppInstalled = isPackageInstalled(context);
        Log.d(TAG, "SystemSensorManagerSdios version 1.0 initialized");
        this.context = context;
        if (isAppInstalled) {
            Log.d(TAG, "binding service");
            mSensorManagerSdiosHelper = new SensorManagerSdiosHelper(context, this, this);
        } else
            mSensorManagerSdiosHelper = null;
    }

    @SuppressLint("RegistrationName")
    private boolean isPackageInstalled(@NonNull Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.SDIOS.ServiceControl", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "APP is not installed " + e);
            return false;
        }
    }

    @SuppressLint("RegistrationName")
    @Override
    public boolean registerListenerFallback(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                            int delayUs, @Nullable Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        return super.registerListenerImpl(listener, sensor, delayUs, handler, maxBatchReportLatencyUs, reservedFlags);
    }

    @SuppressLint("RegistrationName")
    @Override
    public void unregisterListenerFallback(@Nullable SensorEventListener listener, @Nullable Sensor sensor) {
        super.unregisterListenerImpl(listener, sensor);
    }

    @SuppressLint("RegistrationName")
    @Override
    protected boolean registerListenerImpl(@Nullable SensorEventListener listener, @Nullable Sensor sensor,
                                           int delayUs, @Nullable Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        if (listener == null || sensor == null) {
            Log.e(TAG, "registerListenerImpl - sensor or listener is null");
            return false;
        }
        // Trigger Sensors should use the requestTriggerSensor call.
        if (sensor.getReportingMode() == Sensor.REPORTING_MODE_ONE_SHOT) {
            Log.e(TAG, "Trigger Sensors should use the requestTriggerSensor.");
            return false;
        }
        if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            Log.e(TAG, "maxBatchReportLatencyUs and delayUs should be non-negative");
            return false;
        }
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.registerListener(listener, sensor, delayUs, maxBatchReportLatencyUs) :
                super.registerListenerImpl(listener, sensor, delayUs, handler, maxBatchReportLatencyUs, reservedFlags);
    }

    @SuppressLint("RegistrationName")
    @Override
    protected void unregisterListenerImpl(@Nullable SensorEventListener listener, @Nullable Sensor sensor) {
        // Trigger Sensors should use the cancelTriggerSensor call.
        if (sensor != null && sensor.getReportingMode() == Sensor.REPORTING_MODE_ONE_SHOT) {
            return;
        }

        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterListener(listener, sensor);
        else
            super.unregisterListenerImpl(listener, sensor);
    }

    @NonNull
    @Override
    public String toString() {
        return "SensorManagerSdios os_flashed:" + isSDIOS + ", app installed:" + isAppInstalled;
    }
}
