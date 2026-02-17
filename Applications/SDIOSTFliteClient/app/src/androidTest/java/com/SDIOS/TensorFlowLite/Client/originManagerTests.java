package com.SDIOS.Client;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

@RunWith(AndroidJUnit4.class)
public class originManagerTests {
    private Context tmp = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private SensorManager mSensorManager = (SensorManager) tmp.getSystemService(tmp.SENSOR_SERVICE);

    private void registerSensor(SensorManager mSensorManager, listenerHelper lh, int time, int type) throws InterruptedException {
        Sensor s = mSensorManager.getDefaultSensor(type);
        assertTrue(mSensorManager.registerListener(lh,
                mSensorManager.getDefaultSensor(type), SensorManager.SENSOR_DELAY_NORMAL));
        Thread.sleep(time);
        Log.d("Test", "testREG " + lh.countAccuracy + " " + lh.countOnChange);
        assertTrue(lh.countOnChange > 5);
        assertTrue(lh.countAccuracy >= 0);
    }

    private void unregisterSensor(SensorManager mSensorManager, listenerHelper lh, int time) throws InterruptedException {
        mSensorManager.unregisterListener(lh);
        int before1 = lh.countOnChange;
        int before2 = lh.countAccuracy;
        Thread.sleep(time);
        Log.d("Test", "testUNREG " + lh.countAccuracy + " " + lh.countOnChange);
        assertEquals(lh.countOnChange, before1);
        assertEquals(lh.countAccuracy, before2);
    }

    @Test
    public void testLight() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_LIGHT);
        unregisterSensor(mSensorManager, lh, 1000);
    }

    @Test
    public void testGettingEvents() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        unregisterSensor(mSensorManager, lh, 1000);

        lh.reset();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_LIGHT);
        unregisterSensor(mSensorManager, lh, 1000);
    }

    @Test
    public void oneListenerTwoRegisters() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        lh.reset();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_ACCELEROMETER);
        unregisterSensor(mSensorManager, lh, 1000);
    }

    @Test
    public void testGettingEventsForMultipleSensors() {
        //TODO Generalize and stress
    }

    @Test
    public void testGettingEventsForALotOfSensors() {
    }

    class listenerHelper implements SensorEventListener {
        int countOnChange = 0;
        int countAccuracy = 0;

        @Override
        public void onSensorChanged(SensorEvent var1) {
            assertNotNull(var1);
            assertNotNull(var1.values);
            assertTrue(var1.accuracy > -1);
            assertNotNull(var1.sensor);
            assertTrue(var1.timestamp > -1);
            countOnChange += 1;
        }

        @Override
        public void onAccuracyChanged(Sensor var1, int var2) {
            countAccuracy += 1;
        }

        public void reset() {
            countOnChange = 0;
            countAccuracy = 0;
        }
    }
}

    /*
    Supported in emulator
    D/sensors_app: init SensorFetcher
    D/SensorFetcher: 1=[{Sensor name="Goldfish 3-axis Accelerometer", vendor="The Android Open Source Project", version=1, type=1, maxRange=39.3, resolution=2.480159E-4, power=3.0, minDelay=10000}]
    D/SensorFetcher: 2=[{Sensor name="Goldfish 3-axis Magnetic field sensor", vendor="The Android Open Source Project", version=1, type=2, maxRange=2000.0, resolution=0.5, power=6.7, minDelay=10000}]
    D/SensorFetcher: 3=[{Sensor name="Goldfish Orientation sensor", vendor="The Android Open Source Project", version=1, type=3, maxRange=360.0, resolution=1.0, power=9.7, minDelay=10000}, {Sensor name="Orientation Sensor", vendor="AOSP", version=1, type=3, maxRange=360.0, resolution=0.00390625, power=12.7, minDelay=10000}]
    D/SensorFetcher: 4=[{Sensor name="Goldfish 3-axis Gyroscope", vendor="The Android Open Source Project", version=1, type=4, maxRange=16.46, resolution=0.001, power=3.0, minDelay=10000}]
    D/SensorFetcher: 5=[{Sensor name="Goldfish Light sensor", vendor="The Android Open Source Project", version=1, type=5, maxRange=40000.0, resolution=1.0, power=20.0, minDelay=10000}]
    D/SensorFetcher: 6=[{Sensor name="Goldfish Pressure sensor", vendor="The Android Open Source Project", version=1, type=6, maxRange=800.0, resolution=1.0, power=20.0, minDelay=10000}]
    D/SensorFetcher: 8=[{Sensor name="Goldfish Proximity sensor", vendor="The Android Open Source Project", version=1, type=8, maxRange=1.0, resolution=1.0, power=20.0, minDelay=10000}]
    D/SensorFetcher: 9=[{Sensor name="Gravity Sensor", vendor="AOSP", version=3, type=9, maxRange=19.6133, resolution=2.480159E-4, power=12.7, minDelay=10000}]
    D/SensorFetcher: 10=[{Sensor name="Linear Acceleration Sensor", vendor="AOSP", version=3, type=10, maxRange=19.6133, resolution=2.480159E-4, power=12.7, minDelay=10000}]
    D/SensorFetcher: 11=[{Sensor name="Rotation Vector Sensor", vendor="AOSP", version=3, type=11, maxRange=1.0, resolution=5.9604645E-8, power=12.7, minDelay=10000}]
    D/SensorFetcher: 12=[{Sensor name="Goldfish Humidity sensor", vendor="The Android Open Source Project", version=1, type=12, maxRange=100.0, resolution=1.0, power=20.0, minDelay=10000}]
    D/SensorFetcher: 13=[{Sensor name="Goldfish Ambient Temperature sensor", vendor="The Android Open Source Project", version=1, type=13, maxRange=80.0, resolution=1.0, power=0.001, minDelay=10000}]
    D/SensorFetcher: 14=[{Sensor name="Goldfish 3-axis Magnetic field sensor (uncalibrated)", vendor="The Android Open Source Project", version=1, type=14, maxRange=2000.0, resolution=0.5, power=6.7, minDelay=10000}]
    D/SensorFetcher: 15=[{Sensor name="Game Rotation Vector Sensor", vendor="AOSP", version=3, type=15, maxRange=1.0, resolution=5.9604645E-8, power=12.7, minDelay=10000}]
    D/SensorFetcher: 20=[{Sensor name="GeoMag Rotation Vector Sensor", vendor="AOSP", version=3, type=20, maxRange=1.0, resolution=5.9604645E-8, power=12.7, minDelay=10000}]
     */