package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager;

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
import android.os.MemoryFile;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.ListenerWrapper;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.service.SensorManagerSdiosHelper;


public class SensorManagerSdios {
    /* without defend */
    public static final String SENSOR_SERVICE_RAW = "sensor_raw";
    public static final int AXIS_MINUS_X = 129;
    public static final int AXIS_MINUS_Y = 130;
    public static final int AXIS_MINUS_Z = 131;
    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 3;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int DATA_X = 0;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int DATA_Y = 1;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int DATA_Z = 2;
    public static final float GRAVITY_DEATH_STAR_I = 3.5303614E-7F;
    public static final float GRAVITY_EARTH = 9.80665F;
    public static final float GRAVITY_JUPITER = 23.12F;
    public static final float GRAVITY_MARS = 3.71F;
    public static final float GRAVITY_MERCURY = 3.7F;
    public static final float GRAVITY_MOON = 1.6F;
    public static final float GRAVITY_NEPTUNE = 11.0F;
    public static final float GRAVITY_PLUTO = 0.6F;
    public static final float GRAVITY_SATURN = 8.96F;
    public static final float GRAVITY_SUN = 275.0F;
    public static final float GRAVITY_THE_ISLAND = 4.815162F;
    public static final float GRAVITY_URANUS = 8.69F;
    public static final float GRAVITY_VENUS = 8.87F;
    public static final float LIGHT_CLOUDY = 100.0F;
    public static final float LIGHT_FULLMOON = 0.25F;
    public static final float LIGHT_NO_MOON = 0.001F;
    public static final float LIGHT_OVERCAST = 10000.0F;
    public static final float LIGHT_SHADE = 20000.0F;
    public static final float LIGHT_SUNLIGHT = 110000.0F;
    public static final float LIGHT_SUNLIGHT_MAX = 120000.0F;
    public static final float LIGHT_SUNRISE = 400.0F;
    public static final float MAGNETIC_FIELD_EARTH_MAX = 60.0F;
    public static final float MAGNETIC_FIELD_EARTH_MIN = 30.0F;
    public static final float PRESSURE_STANDARD_ATMOSPHERE = 1013.25F;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int RAW_DATA_INDEX = 3;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int RAW_DATA_X = 3;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int RAW_DATA_Y = 4;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int RAW_DATA_Z = 5;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_ACCELEROMETER = 2;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_ALL = 127;
    public static final int SENSOR_DELAY_FASTEST = 0;
    public static final int SENSOR_DELAY_GAME = 1;
    public static final int SENSOR_DELAY_NORMAL = 3;
    public static final int SENSOR_DELAY_UI = 2;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_LIGHT = 16;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_MAGNETIC_FIELD = 8;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_MAX = 64;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_MIN = 1;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_ORIENTATION = 1;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_ORIENTATION_RAW = 128;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_PROXIMITY = 32;
    public static final int SENSOR_STATUS_ACCURACY_HIGH = 3;
    public static final int SENSOR_STATUS_ACCURACY_LOW = 1;
    public static final int SENSOR_STATUS_ACCURACY_MEDIUM = 2;
    public static final int SENSOR_STATUS_NO_CONTACT = -1;
    public static final int SENSOR_STATUS_UNRELIABLE = 0;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_TEMPERATURE = 4;
    /**
     * @deprecated
     */
    @Deprecated
    public static final int SENSOR_TRICORDER = 64;
    public static final float STANDARD_GRAVITY = 9.80665F;
    private static final String TAG = "SDIOSLib-SM-SDIOS";
    public final @NonNull Context context;
    private final @NonNull SensorManager mFallbackSensorManager; //used on fallback and by sensorFetcher
    private final @Nullable SensorManagerSdiosHelper mSensorManagerSdiosHelper;
    private final @NonNull Map<SensorEventListener, ListenerWrapper> sensorEventListenerSensorToWarpedListener = new HashMap<>();
    private final boolean isAppInstalled, isSdiOS;

    @SuppressLint("WrongConstant")
    public SensorManagerSdios(@NonNull Context context) {
        this.context = context;
        isAppInstalled = isAnalyzingServiceInstalled(context.getPackageManager());
        isSdiOS = isOsServiceInstalled();
        // in case of problem we want fallback to native version and not to our service + recursive calling can happen (this code deployed at service)
        if (isSdiOS)
            mFallbackSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE_RAW);
        else
            mFallbackSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (isAppInstalled)
            mSensorManagerSdiosHelper = new SensorManagerSdiosHelper(context, mFallbackSensorManager);
        else
            mSensorManagerSdiosHelper = null;
    }

    public static boolean getRotationMatrix(@Nullable float[] R, @Nullable float[] I, @Nullable float[] gravity, @Nullable float[] geomagnetic) {
        return SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
    }

    public static float getInclination(@Nullable float[] I) {
        return SensorManager.getInclination(I);
    }

    public static boolean remapCoordinateSystem(@Nullable float[] inR, int X, int Y, @Nullable float[] outR) {
        return SensorManager.remapCoordinateSystem(inR, X, Y, outR);
    }

    @Nullable
    public static float[] getOrientation(@Nullable float[] R, @Nullable float[] values) {
        return SensorManager.getOrientation(R, values);
    }

    public static float getAltitude(float p0, float p) {
        return SensorManager.getAltitude(p0, p);
    }

    public static void getAngleChange(@Nullable float[] angleChange, @Nullable float[] R, @Nullable float[] prevR) {
        SensorManager.getAngleChange(angleChange, R, prevR);
    }

    public static void getRotationMatrixFromVector(@Nullable float[] R, @Nullable float[] rotationVector) {
        SensorManager.getRotationMatrixFromVector(R, rotationVector);
    }

    public static void getQuaternionFromVector(@Nullable float[] Q, @Nullable float[] rv) {
        SensorManager.getRotationMatrixFromVector(Q, rv);
    }

    @SuppressLint("WrongConstant")
    public boolean isOsServiceInstalled() {
        SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE_RAW);
        return sm != null;
    }

    public boolean isAnalyzingAppInstalled() {
        return isAppInstalled;
    }

    private boolean isAnalyzingServiceInstalled(@NonNull PackageManager packageManager) {
        try {
            packageManager.getPackageInfo("com.SDIOS.ServiceControl", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "APP is not installed " + e);
            return false;
        }
    }

    /**
     * @deprecated
     */
    public boolean registerListener(@Nullable SensorListener listener, int sensors) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ? mSensorManagerSdiosHelper.registerListener(listener, sensors) :
                mFallbackSensorManager.registerListener(listener, sensors);
    }

    /**
     * @deprecated
     */
    public boolean registerListener(@Nullable SensorListener listener, int sensors, int rate) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ? mSensorManagerSdiosHelper.registerListener(listener, sensors, rate) :
                mFallbackSensorManager.registerListener(listener, sensors, rate);
    }

    /**
     * @deprecated
     */
    public void unregisterListener(@Nullable SensorListener listener) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterListener(listener);
        else
            mFallbackSensorManager.unregisterListener(listener);
    }

    /**
     * @deprecated
     */
    public void unregisterListener(@Nullable SensorListener listener, int sensors) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterListener(listener, sensors);
        else
            mFallbackSensorManager.unregisterListener(listener, sensors);
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterListener(listener, sensor);
        else
            mFallbackSensorManager.unregisterListener(find_warped(listener, true), sensor);
    }

    @SuppressLint("RegistrationName")
    public void unregisterListener(@Nullable SensorEventListener listener) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterListener(listener);
        else
            mFallbackSensorManager.unregisterListener(find_warped(listener, true));
    }

    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs) {
        return isAppInstalled && mSensorManagerSdiosHelper != null && mSensorManagerSdiosHelper.registerListenerSdios(listener, sensor, samplingPeriodUs);
    }

    //the delta that need to be done from here to release code
    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.registerListener(listener, sensor, samplingPeriodUs) :
                mFallbackSensorManager.registerListener(listener_adapeter(listener), sensor, samplingPeriodUs);
    }

    @SuppressLint("ExecutorRegistration")
    public boolean registerListenerSdios(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) {
        return isAppInstalled && mSensorManagerSdiosHelper != null && mSensorManagerSdiosHelper.registerListenerSdios(listener, sensor, samplingPeriodUs, maxReportLatencyUs);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.registerListener(listener, sensor, samplingPeriodUs, maxReportLatencyUs) :
                mFallbackSensorManager.registerListener(listener_adapeter(listener), sensor, samplingPeriodUs, maxReportLatencyUs);
    }

    @SuppressLint("RegistrationName")
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, @Nullable Handler handler) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.registerListener(listener, sensor, samplingPeriodUs, handler) :
                mFallbackSensorManager.registerListener(listener_adapeter(listener), sensor, samplingPeriodUs, handler);
    }

    @SuppressLint({"ExecutorRegistration", "RegistrationName"})
    public boolean registerListener(@Nullable SensorEventListener listener, @Nullable Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, @Nullable Handler handler) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.registerListener(listener, sensor, samplingPeriodUs, maxReportLatencyUs, handler) :
                mFallbackSensorManager.registerListener(listener_adapeter(listener), sensor, samplingPeriodUs, maxReportLatencyUs, handler);
    }

    @SuppressLint("ExecutorRegistration")
    public boolean flush(@Nullable SensorEventListener listener) {
        if (listener == null) return false;
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.flush(listener) :
                mFallbackSensorManager.flush(find_warped(listener, false));
    }

    @Nullable
    private ListenerWrapper find_warped(@Nullable SensorEventListener listener, boolean remove) {
        if (listener == null || !sensorEventListenerSensorToWarpedListener.containsKey(listener)) {
            return null;
        }
        if (remove) {
            return sensorEventListenerSensorToWarpedListener.remove(listener);
        }
        return sensorEventListenerSensorToWarpedListener.get(listener);
    }

    @NonNull
    private ListenerWrapper listener_adapeter(@Nullable SensorEventListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        ListenerWrapper warpped_listener = new ListenerWrapper(listener);
        sensorEventListenerSensorToWarpedListener.put(listener, warpped_listener);
        return warpped_listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable MemoryFile mem) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.createDirectChannel(mem) :
                mFallbackSensorManager.createDirectChannel(mem);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    public SensorDirectChannel createDirectChannel(@Nullable HardwareBuffer mem) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.createDirectChannel(mem) :
                mFallbackSensorManager.createDirectChannel(mem);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void registerDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.registerDynamicSensorCallback(callback);
        else
            mFallbackSensorManager.registerDynamicSensorCallback(callback);
    }

    @SuppressLint("ExecutorRegistration")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void registerDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback, @Nullable Handler handler) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.registerDynamicSensorCallback(callback, handler);
        else
            mFallbackSensorManager.registerDynamicSensorCallback(callback, handler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void unregisterDynamicSensorCallback(@Nullable android.hardware.SensorManager.DynamicSensorCallback callback) {
        if (isAppInstalled && mSensorManagerSdiosHelper != null)
            mSensorManagerSdiosHelper.unregisterDynamicSensorCallback(callback);
        else
            mFallbackSensorManager.unregisterDynamicSensorCallback(callback);
    }

    @SuppressLint("ExecutorRegistration")
    public boolean requestTriggerSensor(@Nullable @SuppressLint("ListenerLast") TriggerEventListener listener, @Nullable Sensor sensor) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.requestTriggerSensor(listener, sensor) :
                mFallbackSensorManager.requestTriggerSensor(listener, sensor);
    }

    @SuppressLint("ExecutorRegistration")
    public boolean cancelTriggerSensor(@Nullable @SuppressLint("ListenerLast") TriggerEventListener listener, @Nullable Sensor sensor) {
        return isAppInstalled && mSensorManagerSdiosHelper != null ?
                mSensorManagerSdiosHelper.cancelTriggerSensor(listener, sensor) :
                mFallbackSensorManager.cancelTriggerSensor(listener, sensor);
    }

    //Functions that do not need protection OR we cannot provide it
    @NonNull
    public List<Sensor> getSensorList(int type) {
        return mFallbackSensorManager.getSensorList(type);
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Sensor> getDynamicSensorList(int type) {
        return mFallbackSensorManager.getDynamicSensorList(type);
    }

    @Nullable
    public Sensor getDefaultSensor(int type) {
        return mFallbackSensorManager.getDefaultSensor(type);
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Sensor getDefaultSensor(int type, boolean wakeUp) {
        return mFallbackSensorManager.getDefaultSensor(type, wakeUp);
    }

    public boolean isDynamicSensorDiscoverySupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return mFallbackSensorManager.isDynamicSensorDiscoverySupported();
        }
        return false;
    }

    //for testing or stats
    @NonNull
    public SensorManagerSdiosHelper test_getSensorManagerSdiosHelper() {
        assert mSensorManagerSdiosHelper != null;
        return mSensorManagerSdiosHelper;
    }

    @NonNull
    @Override
    public String toString() {
        return "SensorManagerSdios os_flashed:" + isSdiOS + ", app installed:" + isAppInstalled;
    }
}
