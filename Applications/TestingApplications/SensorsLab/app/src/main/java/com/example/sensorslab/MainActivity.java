package com.example.sensorslab;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static int[] bitsVals = {8, 4, 2, 1};
    MyRecyclerViewAdapter adapter;
    private Float[][][] sensorVals = new Float[3][1][3];
    private TextView TG, TA, TM, TBytes;
    private float textSize = 20.f;

    private static String unsignedByteGetHex(byte a) {
        StringBuilder result = new StringBuilder();
        int sum = 0, bitInd = 0;
        for (int i = Byte.SIZE - 1; i >= 0; i--) {
            int mask = 1 << i;
            sum += ((a & mask) != 0) ? bitsVals[bitInd] : 0;
            bitInd++;

            if (i % 4 == 0) {
                result.append("" + hexChars[sum]);
                sum = 0;
                bitInd = 0;
            }
        }
        return result.toString();
        //return result.toString() + " " + Integer.toHexString(a) + " " + unsignedByteGetBits(a, 4);
    }

    private static String unsignedByteGetBits(int number, int groupSize) {
        StringBuilder result = new StringBuilder();

        for (int i = Byte.SIZE - 1; i >= 0; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        Log.d("Main_Activity", "created main");

        setUpRecycler();
        setButtons();
    }

    private void presentBytes(Float a) {
        ByteBuffer BB = ByteBuffer.allocate(4);
        BB.putFloat(a);
        byte[] b = BB.array();
        /*String out = String.format("%f %d" +
                        "\n%-5d\t%4s\n%-5d\t%4s\n%-5d\t%4s\n%-5d\t%4s",
                BB.getFloat(0), BB.getInt(0),
                b[0], unsignedByteGetHex(b[0]),
                b[1], unsignedByteGetHex(b[1]),
                b[2], unsignedByteGetHex(b[2]),
                b[3], unsignedByteGetHex(b[3]));*/
        String out = String.format("%.8f\n%s%s %s%s",
                BB.getFloat(0),
                unsignedByteGetHex(b[0]), unsignedByteGetHex(b[1]),
                unsignedByteGetHex(b[2]), unsignedByteGetHex(b[3]));
        TBytes.setText(out);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number "
                + position, Toast.LENGTH_SHORT).show();
        presentBytes(sensorVals[position][0][0]);
    }

    private void setUpRecycler() {
        // data to populate the RecyclerView with
        ArrayList<String> sensors = new ArrayList<>();
        sensors.add("Gyroscope");
        sensors.add("Accelerometer");
        sensors.add("Magnetometer");

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, sensors);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        Timer T = new Timer();
        T.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupSensorManager();
                    }
                });
            }
        }, 1000);

    }

    private void setButtons() {
        findViewById(R.id.scaleup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextSize(2);
            }
        });

        findViewById(R.id.scaledown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextSize(-2);
            }
        });

        TBytes = findViewById(R.id.bytesText);
        TBytes.setOnClickListener(new View.OnClickListener() {
            float counter = 1;

            @Override
            public void onClick(View v) {
                presentBytes(counter);
                counter /= 2;
            }
        });
        Animation performAnimation = AnimationUtils.loadAnimation(this, R.anim.circle_jump);
        performAnimation.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.jumpcircle).startAnimation(performAnimation);
    }

    private void setTextSize(int change) {
        textSize += change;
        TG.setTextSize(textSize);
        TA.setTextSize(textSize);
        TM.setTextSize(textSize);
        TBytes.setTextSize(textSize);
    }

    private void setupSensorManager() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<TextView> arr = adapter.getTV();
        if (arr.size() < 3) {
            Log.e("Main_Activity", "failed to initialize recycler");
            return;
        }
        TG = arr.get(0);
        TA = arr.get(1);
        TM = arr.get(2);
        if (!sensorManager.registerListener(new MyEventListener(TG, sensorVals[0]), sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL))
            Log.e("Main_Activity", "failed to register TYPE_GYROSCOPE");
        if (!sensorManager.registerListener(new MyEventListener(TA, sensorVals[1]), sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL))
            Log.e("Main_Activity", "failed to register TYPE_ACCELEROMETER");
        if (!sensorManager.registerListener(new MyEventListener(TM, sensorVals[2]), sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL))
            Log.e("Main_Activity", "failed to register TYPE_MAGNETIC_FIELD");
    }
}


class MyEventListener implements SensorEventListener {
    private static final int screenUpdateTime = 2;
    private static final double remembering = 0;//0.85;
    private static final double forgetting = 1 - remembering;
    private TextView tv;
    private double[] eValues = new double[3];
    private int counter = 0;
    private Float[][] sensorVals;

    MyEventListener(TextView tv, Float[][] sensorVals) {
        this.tv = tv;
        this.sensorVals = sensorVals;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;

        for (int i = 0; i < values.length; i++) {
            eValues[i] = remembering * eValues[i] + forgetting * values[i];
            sensorVals[0][i] = values[i];
        }

        if (counter++ == screenUpdateTime) {
            counter = 0;
            updateScreen();
        }
    }

    private void updateScreen() {
        StringBuilder sb = new StringBuilder();
        for (double val : eValues) {
            sb.append(val);
            sb.append(" ");
        }

        tv.setText(sb.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}