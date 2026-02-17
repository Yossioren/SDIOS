package com.SDIOS.Client;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.SDIOS.Client.EventsStatistics.Events;
import com.SDIOS.Client.EventsStatistics.EventsSdios;
import com.SDIOS.Client.EventsStatistics.EventsSdiosRef;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.hardwareClasses.SensorEventListener;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.SensorManagerSdios;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SDIOS_example_main";
    private final int typeAccelerometer = Sensor.TYPE_ACCELEROMETER;
    private final int typeGyroscope = Sensor.TYPE_GYROSCOPE;
    private final int typeMagnetometer = Sensor.TYPE_MAGNETIC_FIELD;
    boolean registered = false;
    private TextView textViewAccelerometer;
    private TextView textViewGyroscope;
    private TextView textViewMagnetometer;
    private SensorEventListener events;
    private SensorEventListener eSDIOS;
    private android.hardware.SensorEventListener eventsRef;
    //private SensorManager mSensorManager;
    private SensorManagerSdios mSensorManager;

    public void pressRegisterSensor(int sensor1, int sensor2, int sensor3) {
        if (registered)
            return;
        textViewAccelerometer.setText("Waiting for accelerometer through library regular");
        textViewGyroscope.setText("Waiting for gyroscope through library SDIOS ");
        textViewMagnetometer.setText("Waiting for magnetometer reference through Sm");

        boolean result;
        registered = true;
        events = new Events(textViewAccelerometer);
        result = mSensorManager.registerListener(events,
                mSensorManager.getDefaultSensor(sensor1), SensorManager.SENSOR_DELAY_GAME);
        Log.i(TAG, "Register listener: " + result);
        if (!result)
            textViewAccelerometer.setText("Could not register first listener " + sensor1);
        eSDIOS = new EventsSdios(textViewGyroscope);
        result = mSensorManager.registerListenerSdios(eSDIOS, mSensorManager.getDefaultSensor(sensor2), SensorManager.SENSOR_DELAY_GAME);//SENSOR_DELAY_FASTEST);
        if (!result)
            textViewGyroscope.setText("Could not register second listener " + sensor2);
        Log.i(TAG, "Register listener: " + result);
        eventsRef = new EventsSdiosRef(textViewMagnetometer);
        result = ((SensorManager) this.getSystemService(Context.SENSOR_SERVICE)).registerListener(eventsRef, mSensorManager.getDefaultSensor(sensor3), SensorManager.SENSOR_DELAY_GAME);
        if (!result)
            textViewMagnetometer.setText("Could not register second listener " + sensor3);
        Log.i(TAG, "Register listener: " + result);
    }

    public void pressUnRegisterSensor(int sensor1, int sensor2, int sensor3) {
        if (!registered)
            return;
        registered = false;
        mSensorManager.unregisterListener(events, mSensorManager.getDefaultSensor(sensor1));
        mSensorManager.unregisterListener(eSDIOS, mSensorManager.getDefaultSensor(sensor2));
        ((SensorManager) this.getSystemService(Context.SENSOR_SERVICE)).unregisterListener(eventsRef, mSensorManager.getDefaultSensor(sensor3));
        /*mSensorManager.cancelTriggerSensor(t, mSigMotion);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//sensor service raw/insecure
        mSensorManager = new SensorManagerSdios(this.getBaseContext());
        Log.i(TAG, "MainActivity thread id: " + Thread.currentThread().getId());

        findViewById(R.id.buttonRegSensor).setOnClickListener(
                (_s) -> pressRegisterSensor(typeAccelerometer, typeGyroscope, typeMagnetometer));
        findViewById(R.id.buttonUnRegSensor).setOnClickListener(
                (_s) -> pressUnRegisterSensor(typeAccelerometer, typeGyroscope, typeMagnetometer));
        set_phone_status();
        textViewAccelerometer = findViewById(R.id.tellMeMore);
        textViewGyroscope = findViewById(R.id.tellMeMore2);
        textViewMagnetometer = findViewById(R.id.tellMeMore3);
    }

    private void set_phone_status() {
        TextView phone_info = findViewById(R.id.phone_info);
        String gyroscope = "", accelerometer = "", magnetometer = "";
        Sensor sensor;
        if ((sensor = mSensorManager.getDefaultSensor(typeGyroscope)) != null)
            gyroscope = sensor.toString();
        if ((sensor = mSensorManager.getDefaultSensor(typeAccelerometer)) != null)
            accelerometer = sensor.toString();
        if ((sensor = mSensorManager.getDefaultSensor(typeMagnetometer)) != null)
            magnetometer = sensor.toString();
        phone_info.setText(String.format("Is SDIOS OS installed: %b\nIs SDIOS analyzing app installed: %b\nGyroscope: %s\nAccelerometer: %s\nMagnetometer: %s",
                mSensorManager.isOsServiceInstalled(), mSensorManager.isAnalyzingAppInstalled(), gyroscope, accelerometer, magnetometer));
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On resume");
        if (registered)
            pressRegisterSensor(typeAccelerometer, typeGyroscope, typeMagnetometer);
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "On resume");
        if (registered)
            pressUnRegisterSensor(typeAccelerometer, typeGyroscope, typeMagnetometer);
    }
}