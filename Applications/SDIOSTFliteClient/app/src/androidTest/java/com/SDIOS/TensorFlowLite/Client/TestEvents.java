package com.SDIOS.Client;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.SensorManagerSdios;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEvent;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEvents {
    private static final int[] Type = new int[]{Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD};
    private final Context test_context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final SensorManagerSdios mSensorManager = new SensorManagerSdios(test_context);

    private void registerSensor(SensorManagerSdios mSensorManager, listenerHelper lh, int time, int type) throws InterruptedException {
        registerSensor(mSensorManager, lh, time, type, 3);
    }

    private void registerSensor(SensorManagerSdios mSensorManager, listenerHelper lh, int time, int type, int amount) throws InterruptedException {
        Sensor s = mSensorManager.getDefaultSensor(type);
        assertTrue(mSensorManager.registerListener(lh,
                mSensorManager.getDefaultSensor(type), SensorManager.SENSOR_DELAY_NORMAL));
        Thread.sleep(time);
        Log.d("Test", "testREG " + lh.countAccuracy + " " + lh.countOnChange);
        assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(s, lh));
        assertTrue(lh.countOnChange >= amount);
        assertTrue(lh.countAccuracy >= 0);
    }

    private void DoesNotGotMoreEvents(listenerHelper lh, int time) throws InterruptedException {
        Thread.sleep(time);
        Log.d("Test", "DoesNotGotMoreEvents");
        assertEquals(lh.countOnChange, 0);
        assertEquals(lh.countAccuracy, 0);
    }

    private void unregisterSensor(SensorManagerSdios mSensorManager, listenerHelper lh, int time) throws InterruptedException {
        mSensorManager.unregisterListener(lh);
        int before1 = lh.countOnChange;
        int before2 = lh.countAccuracy;
        Thread.sleep(time);
        Log.d("Test", "testUNREG " + lh.countAccuracy + " " + lh.countOnChange);
        assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
        assertEquals(lh.countOnChange, before1);
        assertEquals(lh.countAccuracy, before2);
    }

    private void unregisterSensor(SensorManagerSdios mSensorManager, listenerHelper lh, int type, int time) throws InterruptedException {
        unregisterSensor(mSensorManager, lh, type, time, 3);
    }

    private void unregisterSensor(SensorManagerSdios mSensorManager, listenerHelper lh, int type, int time, int amount) throws InterruptedException {
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(type));
        int before1 = lh.countOnChange;
        int before2 = lh.countAccuracy;
        Thread.sleep(time);
        Log.d("Test", "testUNREG " + lh.countAccuracy + " " + lh.countOnChange);
        assertTrue(lh.countOnChange - before1 < amount);
        assertTrue(lh.countAccuracy - before2 < amount);
    }

    private void registerListenerSdios(SensorManagerSdios mSensorManager, listenerHelperSdios lh, int time, int type) throws InterruptedException {
        Sensor s = mSensorManager.getDefaultSensor(type);
        assertTrue(mSensorManager.registerListenerSdios(lh,
                s, SensorManager.SENSOR_DELAY_NORMAL));
        Thread.sleep(time);
        Log.d("Test", "testREGSDIOS " + lh.countAccuracy + " " + lh.countOnChange + " " + lh.countOnChangeSDIOS);
        assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(s, lh));
        assertTrue(lh.countOnChange >= 3);
        assertEquals(lh.countOnChange, lh.countOnChangeSDIOS);
        assertTrue(lh.countAccuracy >= 0);
    }

    private void unregisterListenerSdios(SensorManagerSdios mSensorManager, listenerHelperSdios lh, int time) throws InterruptedException {
        mSensorManager.unregisterListener(lh);
        int before1 = lh.countOnChange;
        int before2 = lh.countAccuracy;
        Thread.sleep(time);
        Log.d("Test", "testUNREGSDIOS " + lh.countAccuracy + " " + lh.countOnChange + " " + lh.countOnChangeSDIOS);
        assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
        assertEquals(lh.countOnChange, before1);
        assertEquals(lh.countAccuracy, before2);
        assertEquals(lh.countOnChange, lh.countOnChangeSDIOS);
    }

    @Before
    public void checkConnectedB() {
        assertEquals(0, mSensorManager.test_getSensorManagerSdiosHelper().countConnected2());
        assertEquals(0, mSensorManager.test_getSensorManagerSdiosHelper().countConnected());
    }

    @After
    public void checkConnectedA() throws InterruptedException {
        Thread.sleep(30);
        assertEquals(0, mSensorManager.test_getSensorManagerSdiosHelper().countConnected2());
        assertEquals(0, mSensorManager.test_getSensorManagerSdiosHelper().countConnected());
    }

    @Test
    public void testInstalledResponses() throws InterruptedException {
        assertTrue(mSensorManager.isAnalyzingAppInstalled());
        assertFalse(mSensorManager.isOsServiceInstalled());
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
    public void testGettingEventsSdios() throws InterruptedException {
        listenerHelperSdios lh = new listenerHelperSdios();
        registerListenerSdios(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        unregisterListenerSdios(mSensorManager, lh, 1000);

        lh.reset();
        mSensorManager.registerListenerSdios(lh,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        Thread.sleep(1000);
        assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
    }

    @Test
    public void oneListenerTwoRegisters() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        lh.reset();
        registerSensor(mSensorManager, lh, 1000, Sensor.TYPE_ACCELEROMETER);
        unregisterSensor(mSensorManager, lh, 1000);

        listenerHelperSdios lhSDIOS = new listenerHelperSdios();
        registerListenerSdios(mSensorManager, lhSDIOS, 1000, Sensor.TYPE_GYROSCOPE);
        lhSDIOS.reset();
        registerListenerSdios(mSensorManager, lhSDIOS, 1000, Sensor.TYPE_ACCELEROMETER);
        unregisterListenerSdios(mSensorManager, lhSDIOS, 1000);
    }

    @Test
    public void reuse() throws InterruptedException {
        listenerHelper lh1 = new listenerHelper();
        registerSensor(mSensorManager, lh1, 2000, Sensor.TYPE_ACCELEROMETER);
        unregisterSensor(mSensorManager, lh1, 1000);
        lh1.reset();
        DoesNotGotMoreEvents(lh1, 500);
        registerSensor(mSensorManager, lh1, 1000, Sensor.TYPE_MAGNETIC_FIELD);
        unregisterSensor(mSensorManager, lh1, 1000);
        lh1.reset();
        DoesNotGotMoreEvents(lh1, 500);
        registerSensor(mSensorManager, lh1, 1000, Sensor.TYPE_GYROSCOPE);
        unregisterSensor(mSensorManager, lh1, 1000);
        lh1.reset();
        DoesNotGotMoreEvents(lh1, 500);
    }

    @Test
    public void unregistersListenerThatNotRegisters() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        unregisterSensor(mSensorManager, lh, 500);

        listenerHelperSdios lhSDIOS = new listenerHelperSdios();
        unregisterListenerSdios(mSensorManager, lhSDIOS, 500);
    }

    @Test
    public void testGettingEventsForMultipleSensors() throws InterruptedException {
        listenerHelper lh1 = new listenerHelper();
        registerSensor(mSensorManager, lh1, 2000, Sensor.TYPE_ACCELEROMETER);
        listenerHelper lh2 = new listenerHelper();
        registerSensor(mSensorManager, lh2, 1000, Sensor.TYPE_MAGNETIC_FIELD);
        listenerHelper lh3 = new listenerHelper();
        registerSensor(mSensorManager, lh3, 1000, Sensor.TYPE_GYROSCOPE);

        listenerHelperSdios lhsdios1 = new listenerHelperSdios();
        registerListenerSdios(mSensorManager, lhsdios1, 1000, Sensor.TYPE_ACCELEROMETER);
        listenerHelperSdios lhsdios2 = new listenerHelperSdios();
        registerListenerSdios(mSensorManager, lhsdios2, 1000, Sensor.TYPE_MAGNETIC_FIELD);
        listenerHelperSdios lhsdios3 = new listenerHelperSdios();
        registerListenerSdios(mSensorManager, lhsdios2, 1000, Sensor.TYPE_GYROSCOPE);
        unregisterSensor(mSensorManager, lh1, 500);
        unregisterSensor(mSensorManager, lh2, 500);
        unregisterSensor(mSensorManager, lh3, 500);
        unregisterListenerSdios(mSensorManager, lhsdios1, 500);
        unregisterListenerSdios(mSensorManager, lhsdios2, 500);
        unregisterListenerSdios(mSensorManager, lhsdios3, 500);
    }

    @Test
    public void testSameTwice() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        lh.reset();
        registerSensor(mSensorManager, lh, 1000, Sensor.TYPE_GYROSCOPE, 5);
        lh.reset();
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
    }

    @Test
    public void testSameThreeTimes() throws InterruptedException {
        listenerHelper lh = new listenerHelper();
        registerSensor(mSensorManager, lh, 2000, Sensor.TYPE_GYROSCOPE);
        lh.reset();
        registerSensor(mSensorManager, lh, 1000, Sensor.TYPE_GYROSCOPE, 5);
        lh.reset();
        registerSensor(mSensorManager, lh, 1000, Sensor.TYPE_GYROSCOPE, 10);
        lh.reset();
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
        mSensorManager.unregisterListener(lh, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
    }

    //some crazy stress test
    @Test
    public void testGettingEventsForALotOfSensors() throws InterruptedException {
        List<listenerHelper> lh_list = new LinkedList<>();
        for (int i = 0; i < 256; i++)
            lh_list.add(new listenerHelper());

        for (listenerHelper lh : lh_list) {//reg
            int type = Type[(int) (Math.random() * Type.length)];
            Sensor s = mSensorManager.getDefaultSensor(type);
            assertTrue(mSensorManager.registerListener(lh,
                    mSensorManager.getDefaultSensor(type), SensorManager.SENSOR_DELAY_NORMAL));
        }

        Thread.sleep(1500);
        for (listenerHelper lh : lh_list) {//is alive
            assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
            assertTrue(lh.countOnChange >= 10);
        }

        for (listenerHelper lh : lh_list) lh.reset();//reset

        Thread.sleep(500);
        for (listenerHelper lh : lh_list) {//is alive
            assertTrue(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
            assertTrue(lh.countOnChange >= 10);
        }

        for (listenerHelper lh : lh_list) {//unreg
            mSensorManager.unregisterListener(lh);
            assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
            lh.reset();
        }

        Thread.sleep(500);
        for (listenerHelper lh : lh_list) {//is dead
            assertFalse(mSensorManager.test_getSensorManagerSdiosHelper().isRegistered(lh));
            assertTrue(lh.countOnChange < 2);
        }
    }

    class listenerHelper implements SensorEventListener {
        int countOnChange = 0;
        int countAccuracy = 0;

        @Override
        public void onSensorChanged(@NonNull SensorEvent sensorEvent) {
            assertNotNull(sensorEvent);
            assertNotNull(sensorEvent.values);
            assertTrue(sensorEvent.accuracy > -1);
            assertNotNull(sensorEvent.sensor);
            assertTrue(sensorEvent.timestamp > -1);
            countOnChange += 1;
        }

        @Override
        public void onAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
            countAccuracy += 1;
        }

        public void reset() {
            countOnChange = 0;
            countAccuracy = 0;
        }
    }

    class listenerHelperSdios extends listenerHelper {
        int countOnChangeSDIOS = 0;

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            assertTrue(sensorEvent.trust > -1);
            countOnChangeSDIOS += 1;
            super.onSensorChanged(sensorEvent);
        }

        @Override
        public void reset() {
            countOnChangeSDIOS = 0;
            super.reset();
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