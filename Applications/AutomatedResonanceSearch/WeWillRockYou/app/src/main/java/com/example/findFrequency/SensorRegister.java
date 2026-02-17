package com.example.findFrequency;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.findFrequency.tree.SensorTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


interface PostGraph {
    void postGraph(int i, long t, float[] values);
}

public class SensorRegister implements PostGraph {
    private static final int[] toTrack = new int[]{Sensor.TYPE_GYROSCOPE};
    private static final String seperate = ", ";
    private SensorManager sensorManager;
    private Grapher myGrapher;
    private ArrayList<SensorTracker> arr;
    private ArrayList<myEventListener> listenerArr;
    private int graphPost = 0;

    // private static final int[] toTrack = new int[]{Sensor.TYPE_GYROSCOPE, Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD};
    SensorRegister(SensorManager sensorManager, Grapher graph) {
        this.sensorManager = sensorManager;
        myGrapher = graph;

        newCollectingArr();
    }

    private void newCollectingArr() {
        arr = new ArrayList<>();
        listenerArr = new ArrayList<>();
        for (int i = 0; i < toTrack.length; i++) {
            SensorTracker ST = new SensorTracker();
            arr.add(ST);
            listenerArr.add(new myEventListener(this, i, ST));
            extractAndRegister(listenerArr.get(i), toTrack[i]);
        }
    }

    private void registerSensor(SensorEventListener listener, Sensor sensor) {
        if (!sensorManager.registerListener(listener, sensor, 0))//if out of memory change to game=2
            Log.e("Sensor_register", "failed to register gyro");
    }

    private String extractSensorData(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        sb.append(sensor.getName());
        sb.append(seperate);
        sb.append(sensor.getVendor());
        sb.append(seperate);
        sb.append(sensor.getVersion());
        Log.d("Sensor_register", "load sensor " + sb.toString());
        return sb.toString();
    }

    private String extractAndRegister(SensorEventListener listener, int type) {
        Sensor sensor = sensorManager.getDefaultSensor(type);
        registerSensor(listener, sensor);
        return extractSensorData(sensor);
    }

    public void postGraph(int i, long timestamp, float[] values) {
        if (i == graphPost)
            myGrapher.addData(timestamp, values);
    }

    public void changeGraph(int i) {
        graphPost = i;
        myGrapher.reset();
    }

    public Map<String, Map<String, Double>> getArrTree(int sensor_index) {
        for (int i = 0; i < toTrack.length; i++) {
            listenerArr.get(i).idle();
        }
        return arr.get(sensor_index).getValuesXYZ(); //graphPost
    }

    public List<SensorTracker.SensorData> getArrData() {
        for (int i = 0; i < toTrack.length; i++) {
            listenerArr.get(i).idle();
        }
        ArrayList<SensorTracker.SensorData> out = new ArrayList<>();
        for (int i = 0; i < toTrack.length; i++) {
            SensorTracker.SensorData values = arr.get(i).getXYZ();
            out.add(values);
        }
        return out;
    }

    public void clearAll() {
        for (int i = 0; i < toTrack.length; i++) {
            listenerArr.get(i).idle();
            arr.get(i).reset();
            listenerArr.get(i).active();
        }
    }
}

class myEventListener implements SensorEventListener {
    boolean toMeasure = true;
    private int id;
    private PostGraph poster;
    private SensorTracker ST;

    myEventListener(PostGraph poster, int id, SensorTracker ST) {
        this.id = id;
        this.poster = poster;
        this.ST = ST;
    }

    public void idle() {
        toMeasure = false;
    }

    public void active() {
        toMeasure = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("Sensor_register", "sensor " + event.sensor.getName() + " " + event.accuracy);

        poster.postGraph(id, event.timestamp, event.values);
        if (toMeasure)
            ST.addEntry(event.timestamp, event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}