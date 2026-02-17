package com.example.joy.viewsensorsoutput;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "view_sensors";
    SensorManager sensorManager;
    TextView text;
    Sensor Mag;
    Sensor Gyr;
    Sensor Acc;
    Sensor currentSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Log.d(TAG, sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).toString());
        Log.d(TAG, sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).toString());
        Log.d(TAG, sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).toString());
        if ((Mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)) == null)
            Log.e(TAG, "no mag");
        if ((Gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)) == null)
            Log.e(TAG, "no gyro");
        if ((Acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) == null)
            Log.e(TAG, "no acc");
        registerAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerAll();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_gyr:
                if (checked)
                    currentSensor = Gyr;
                break;
            case R.id.radio_acc:
                if (checked)
                    currentSensor = Acc;
                break;
            case R.id.radio_mag:
                if (checked)
                    currentSensor = Mag;
                break;
        }
    }

    private void registerAll() {
        if (!sensorManager.registerListener(this, Mag, SensorManager.SENSOR_DELAY_UI))
            Log.e(TAG, "Could not register mag");
        if (!sensorManager.registerListener(this, Gyr, SensorManager.SENSOR_DELAY_UI))
            Log.e(TAG, "Could not register gyr");
        if (!sensorManager.registerListener(this, Acc, SensorManager.SENSOR_DELAY_UI))
            Log.e(TAG, "Could not register acc");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == currentSensor) {
            text.setText(String.format("timestamp: %s\nvalues: %s\naccuracy: %s", event.timestamp, Arrays.toString(event.values), event.accuracy));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "accuracy: " + sensor.getStringType() + " " + accuracy);
    }
}
