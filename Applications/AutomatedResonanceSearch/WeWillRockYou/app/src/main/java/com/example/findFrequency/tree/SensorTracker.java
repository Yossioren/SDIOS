package com.example.findFrequency.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SensorTracker {
    public static final String min = "min";
    public static final String max = "max";
    public static final String mean_of_temporal_max = "mean_of_temporal_max";
    public static final String mean = "mean";
    public static final String av = "av";//average-deviation - av^2 = var
    public static final String std = "std";//Standard Deviation
    public static final String rms = "rms";//root mean sequare
    public static final String zcr = "zcr";//Zero-crossing rate
    public static final String x = "x";
    public static final String y = "y";
    public static final String z = "z";
    public static final String t = "timestamp";
    public static final String l2 = "l2norm";
    public static String XYZ[] = new String[]{x, y, z};
    public static String functionsToComp[] = new String[]{mean, mean_of_temporal_max};//{mean, av, zcr};
    public static String XYZl[] = new String[]{x, y, z, l2};
    public static String functions[] = new String[]{min, max, mean, av, std, rms, mean_of_temporal_max}; // zcr is not supported
    private List<Long> timeStamps = new LinkedList<>();
    private List<Double>[] values = new LinkedList[4];

    public SensorTracker() {
        values[0] = new LinkedList<>();
        values[1] = new LinkedList<>();
        values[2] = new LinkedList<>();
        values[3] = new LinkedList<>();
    }

    public static double mean(List<Double> tomean) {
        double sum = 0;
        Iterator<Double> iter = tomean.iterator();
        while (iter.hasNext()) sum += iter.next();
        return sum / tomean.size();
    }

    public static double std(List<Double> list, double list_mean) {
        double square_sum = 0;

        Iterator<Double> iter = list.iterator();
        while (iter.hasNext()) {
            double diff = iter.next() - list_mean;
            square_sum += diff * diff;
        }
        return Math.sqrt(square_sum / (list.size() - 1));
    }

    public static double rms(List<Double> tomean) {
        double sum = 0;
        Iterator<Double> iter = tomean.iterator();
        while (iter.hasNext()) {
            double cur = iter.next();
            sum += cur * cur;
        }
        return Math.sqrt(sum / tomean.size());
    }

    public static double[] max_min_mean(List<Double> tomean) {
        Iterator<Double> iter = tomean.iterator();
        double cur = iter.next();
        double max = cur;
        double sum = cur;
        double min = cur;


        while (iter.hasNext()) {
            cur = iter.next();
            sum += cur;
            if (cur > max) max = cur;
            else if (cur < min) min = cur;
        }
        return new double[]{max, min, sum / tomean.size()};
    }

    public static double mean_of_temporal_max(List<Double> tomean) {
        Iterator<Double> iter = tomean.iterator();
        double cur = iter.next();
        double max = cur;
        int count = 1;
        int group_size = 10;
        List<Double> maxes = new LinkedList<>();

        while (iter.hasNext()) {
            cur = iter.next();
            if (cur > max) max = cur;
            count += 1;
            if (count > group_size) {
                maxes.add(max);
                if (iter.hasNext()) {
                    cur = iter.next();
                    count = 1;
                    max = cur;
                }
            }
        }
        return mean(maxes);
    }

    public static double avg_dev(List<Double> list, double list_mean) {
        double sum = 0;

        Iterator<Double> iter = list.iterator();
        while (iter.hasNext()) {
            sum += Math.abs(iter.next() - list_mean);
        }
        return sum / list.size();
    }

    public static double zcr(List<Double> list) {
        double sum = 0.0;

        Iterator<Double> iter = list.iterator();
        double last = iter.next();

        while (iter.hasNext()) {
            double cur = iter.next();

            if (cur * last < 0) sum += 1;

            last = cur;
        }
        return sum / (list.size() - 1);
    }

    public void addEntry(long timeStamp, float[] vals) {
        values[0].add((double) vals[0]);
        values[1].add((double) vals[1]);
        values[2].add((double) vals[2]);
        values[3].add(Math.sqrt(
                vals[0] * vals[0] +
                        vals[1] * vals[1] +
                        vals[2] * vals[2]));
        timeStamps.add(timeStamp);
    }

    public void reset() {
        values[0].clear();
        values[1].clear();
        values[2].clear();
        values[3].clear();
        timeStamps.clear();
    }

    public Map<String, Double> getValues(List<Double> values) {
        Map<String, Double> out = new HashMap<>();
        double[] maxminmean = SensorTracker.max_min_mean(values);
        out.put(max, maxminmean[0]);
        out.put(min, maxminmean[1]);
        out.put(mean, maxminmean[2]);
        out.put(av, SensorTracker.avg_dev(values, maxminmean[2]));
        out.put(std, SensorTracker.std(values, maxminmean[2]));
        out.put(rms, SensorTracker.rms(values));
        out.put(mean_of_temporal_max, SensorTracker.mean_of_temporal_max(values));
        //out.put(zcr, SensorTracker.zcr(values));

        return out;
    }

    public Map<String, Map<String, Double>> getValuesXYZ() {
        Map<String, Map<String, Double>> out = new HashMap<>();
        out.put(x, getValues(values[0]));
        out.put(y, getValues(values[1]));
        out.put(z, getValues(values[2]));
        out.put(l2, getValues(values[3]));
        return out;
    }

    public SensorData getXYZ() {
        Map<String, List<Double>> out = new HashMap<>();
        List<Double> clone0 = new LinkedList<>();
        for (Double d : values[0])
            clone0.add(d);
        List<Double> clone1 = new LinkedList<>();
        for (Double d : values[1])
            clone1.add(d);
        List<Double> clone2 = new LinkedList<>();
        for (Double d : values[2])
            clone2.add(d);
        List<Long> timeclone = new LinkedList<>();
        for (Long l : timeStamps)
            timeclone.add(l);
        out.put(x, clone0);
        out.put(y, clone1);
        out.put(z, clone2);

        //Log.d("Server_connectionfs", clone0.size() + " " + values[0].size() + " " + clone1.size() + " " + values[1].size() + " " + clone2.size() + " " + values[2].size() + " " + timeclone.size() + " " + timeStamps.size() + " ");
        return new SensorData(out, timeclone);
    }

    public class SensorData {
        public Map<String, List<Double>> xyz;
        public List<Long> timeStamps;

        SensorData(Map<String, List<Double>> xyz, List<Long> timeStamps) {
            this.xyz = xyz;
            this.timeStamps = timeStamps;
        }
    }
}
