package com.example.findFrequency;

import android.util.Log;
import android.util.SparseArray;

import com.example.findFrequency.tree.SensorTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ExperimentManager {

    private SensorRegister sr;
    private SparseArray<Map<String, Map<String, Double>>> results;

    public ExperimentManager(SensorRegister sr) {
        this.sr = sr;
        results = new SparseArray<>();
    }

    private void clear() {
        sr.clearAll();
    }

    public void removeEntry(int i) {
        results.remove(i);
    }

    public void startExp() {
        clear();
    }

    public void endExp(int num) {
        results.put(num, sr.getArrTree(0));
    }

    public List<SensorTracker.SensorData> endExpGetData() {
        return sr.getArrData();
    }

    public int computeScoreBias(int evaluateMe, int biased, double bias) {
        Map<String, Map<String, Double>> a = results.get(evaluateMe);
        Map<String, Map<String, Double>> b = results.get(biased);
        int score = 0;
        Log.d("whoIsBetterA", a.values().toString());
        Log.d("whoIsBetterB", b.values().toString());
        for (int i = 0; i < SensorTracker.XYZl.length; i++) {
            for (int j = 0; j < SensorTracker.functionsToComp.length; j++) {
                if (Math.abs(a.get(SensorTracker.XYZl[i]).get(SensorTracker.functionsToComp[j])) >
                        bias * Math.abs(b.get(SensorTracker.XYZl[i]).get(SensorTracker.functionsToComp[j])))
                    score++;
            }
        }
        return score;
    }

    public int computeScore(int first, int second) {
        Map<String, Map<String, Double>> a = results.get(first);
        Map<String, Map<String, Double>> b = results.get(second);
        int score = 0;
        Log.d("whoIsBetterA", a.values().toString());
        Log.d("whoIsBetterB", b.values().toString());
        for (int i = 0; i < SensorTracker.XYZl.length; i++) {
            for (int j = 0; j < SensorTracker.functionsToComp.length; j++) {
                if (Math.abs(a.get(SensorTracker.XYZl[i]).get(SensorTracker.functionsToComp[j])) >
                        Math.abs(b.get(SensorTracker.XYZl[i]).get(SensorTracker.functionsToComp[j])))
                    score++;
                else
                    score--;
            }
        }
        return score;
    }

    //now will check just gyro
    public boolean whoIsBetter(int first, int second) {
        int score = computeScore(first, second);
        Log.d("whoIsBetterSCORE", (score > 0 ? first : second) + " is better with score:" + score);
        return score > 0;
    }
}
