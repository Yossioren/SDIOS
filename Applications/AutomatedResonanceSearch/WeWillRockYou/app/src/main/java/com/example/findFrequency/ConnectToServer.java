package com.example.findFrequency;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.example.findFrequency.tree.SensorTracker;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConnectToServer extends AsyncTask<String, Void, Integer> {

    protected static final int updateTimeUI = 0;
    protected static final int updateFinalResponse = 1;
    protected static final int noNetWork = 2;
    private static final double bias = 2;//3.2 for gyro else 1.2
    private static final double features = 3;
    private static final int pauseFrequencyHZ = 10;
    private static int recording_time = 30;//sec
    private static String timestamp = "timestamp";
    private static String[] sensorsName = {"Gyroscope", "Accelerometer", "Magnetometer"};
    private final String server_url;
    Handler mHandler;
    ExperimentManager mExperimentManager;
    private String mode;
    private String resonance = "";
    private boolean should_stop = false;
    private double from = 14000, to = 21000, jump = 50, dwell = 500;

    public ConnectToServer(Handler mHandler, String server_url, ExperimentManager mExperimentManager, String measure_instructions, String mode) {
        this.mode = mode;
        this.mHandler = mHandler;
        this.server_url = server_url;
        this.mExperimentManager = mExperimentManager;
        try {
            String[] measure_instructions_array = measure_instructions.split(" ");
            this.from = Double.parseDouble(measure_instructions_array[0]);
            this.to = Double.parseDouble(measure_instructions_array[1]);
            this.jump = Double.parseDouble(measure_instructions_array[2]);
            this.dwell = Double.parseDouble(measure_instructions_array[3]);
        } catch (Exception e) {
        } // Use defaults on exception
        setUpUnsignedSSL();
    }

    public String sendHTTPMessage(String uri, String method, byte[] message) {
        Log.d("Server_connection", String.format("Sending message /%s, %s", uri, method));
        try {
            URL url = new URL(String.format("%s/%s", this.server_url, uri));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
//            con.setRequestProperty("Content-Type", "application/json");
            if (null != message && message.length > 0) {
                Log.d("Server_connection", String.format("Adding body: %d bytes", message.length));
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(message, 0, message.length);
                }
            }
            return parse_response(con);
        } catch (IOException e) {
            Log.e("Server_connection", String.format("Message failed! %s", e));
        }
        return null;
    }

    @Nullable
    private String parse_response(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            Log.e("Server_connection", String.format("Message failed! %d", responseCode));
        }
        con.disconnect();
        return null;
    }

    private void setUpUnsignedSSL() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {
                    }

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier validHosts = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
        // All hosts will be valid
        HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.d("Server_connection", "onPostExecute");
        mHandler.sendMessage(mHandler.obtainMessage(updateFinalResponse, result));
    }

    public String getResonance() {
        return resonance;
    }

    private int mining() throws Exception {
        int sleepTime = (int) dwell;
        mHandler.sendMessage(mHandler.obtainMessage(updateTimeUI,
                /* seconds  (2.5*dwell*(to-from)/jump)        iterations */
                new double[]{dwell * 1.2, (to - from) / jump}));//search the top and the bottom of the domain


        int quiet = 0;
        int startM = 1;

        mExperimentManager.startExp();
        sleepFor(sleepTime);
        this.should_stop_background();
        mExperimentManager.endExp(0);

        int curExp = startM;
        Log.d("Server_connection", "start learning geology");

        int endM = collectDataFor(from, to, jump, curExp, sleepTime);
        if (endM == -1)
            return 3;
        List<Pair<Double, Double>> lst = getFrequencyDomainForQuiet(from, jump, quiet, startM, endM);
        jump = jump / 10;
        sleepTime = sleepTime / 2;
        curExp = startM;
        if (lst.isEmpty()) {
            Log.e("Server_connection", "lvl1 no points");
            return 1;
        }
        Log.d("Server_connection", lst.toString());
        List<Pair<Double, Double>> nextList = new LinkedList<>();

        mHandler.sendMessage(mHandler.obtainMessage(updateTimeUI,
                /* seconds                      iterations */
                new double[]{dwell * 1.2, lst.size()}));
        for (Pair<Double, Double> p : lst) {
            endM = collectDataFor(p.first, p.second, jump, curExp, sleepTime);
            if (endM == -1)
                return 3;
            nextList.add(getBestFreq(p.first, jump, startM, endM));
            curExp = startM;
        }

        jump = jump / 5;
        if (nextList.isEmpty()) {
            Log.e("Server_connection", "lvl2 no points");
            return 1;
        }
        Log.d("Server_connection", nextList.toString());
        List<Pair<Double, Double>> sol = new LinkedList<>();
        mHandler.sendMessage(mHandler.obtainMessage(updateTimeUI,
                /* seconds                      iterations */
                new double[]{dwell * 1.2, nextList.size()}));
        for (Pair<Double, Double> p : nextList) {
            endM = collectDataFor(p.first, p.second, jump, curExp, sleepTime);
            if (endM == -1)
                return 3;
            sol.add(getBestFreq(p.first, jump, startM, endM));
            curExp = startM;
        }

        resonance = sol.toString();
        Log.d("Server_connection", resonance);
        mExperimentManager.removeEntry(quiet);

        if (!ExamineResonance(sol)) {
            Log.d("Server_connection", "failed to save all to fs");
            return 4;
        }

        return 0;
    }

    private boolean ExamineResonance(List<Pair<Double, Double>> sol) throws Exception {
        int recordingTime = recording_time * 1000;
        mHandler.sendMessage(mHandler.obtainMessage(updateTimeUI,
                /* seconds                      iterations */
                new double[]{recordingTime * 1.2, sol.size()}));
        for (Pair<Double, Double> p : sol) {
            double rockFreq = (p.first + p.second) / 2;
            List<SensorTracker.SensorData> XYZT = measureFrequencyXYZ(recordingTime, rockFreq);
            if (XYZT == null)
                Log.e("ExamineResonance", "failed measure for frequency " + rockFreq);
            else
                writeToFS(rockFreq, XYZT);
            mExperimentManager.removeEntry(0);
        }
        return true;
    }

    private void writeToFS(double frequency, List<SensorTracker.SensorData> data) {
        Log.d("Server_connectionfs", "writing fs " + data.size());
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("sensor_name", sensorsName[i]);
            obj.put("build_model", Build.MODEL);
            obj.put("frequency", frequency);
            Map<String, Object> entries = new HashMap<>(data.get(i).xyz);
            entries.put("t", data.get(i).timeStamps);
            obj.put("entry", entries);
            sendHTTPMessage("save_results", "POST", new Gson().toJson(obj)
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    private Pair<Double, Double> getBestFreq(double from, double jump, int startM, int endM) {
        List<Integer> freqs = new LinkedList();

        for (int i = startM; i < endM; i++) {
            freqs.add(i);
        }
        freqs.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return mExperimentManager.computeScore(o2, o1);
            }
        });
        for (int i = startM; i < endM; i++) {
            mExperimentManager.removeEntry(i);
        }

        double domain = jump / 2;
        double bestFreq = from + jump * (freqs.get(0) - startM);
        return new Pair<>(bestFreq - domain, bestFreq + domain);
    }

    private List<Pair<Double, Double>> getFrequencyDomainForQuiet(double from, double jump, int quiet, int startM, int endM) {
        List<Pair<Double, Double>> lst = new LinkedList();
        double domain = jump / 2;
        for (int i = startM; i < endM; i++) {
            if (mExperimentManager.computeScoreBias(i, quiet, bias) >= features) {
                double restoredFreqHZ = from + jump * (i - startM);
                lst.add(new Pair<>(restoredFreqHZ - domain, restoredFreqHZ + domain));
            }
            mExperimentManager.removeEntry(i);
        }
        return lst;
    }

    private int collectDataFor(double cur, double to, double jump, int curExp, int sleepTime) throws Exception {
        while (cur <= to) {
            if (!measureFrequency(curExp++, sleepTime, cur)) {
                return -1;
            }
            cur += jump;
        }
        return curExp;
    }

    private List<SensorTracker.SensorData> measureFrequencyXYZ(int sleepTime, double frequency) throws Exception {
        String msg;
        msg = sendHTTPMessage("make_frequency", "POST", String.format("%f %d", frequency, sleepTime).getBytes(StandardCharsets.UTF_8));
        List<SensorTracker.SensorData> result = null;
        if (msg != null) {
            mExperimentManager.startExp();
            sleepFor(sleepTime);
            this.should_stop_background();
            result = mExperimentManager.endExpGetData();
            int pause = new Double(sleepTime * 0.25).intValue();
            if (!pauseFrequency(pause, 1))
                Log.e("Server_connection", "client not sane");
        }
        return result;
    }

    private void should_stop_background() throws Exception {
        if (this.should_stop)
            throw new Exception("Hard stopping background task");
    }

    //pause will help to measure more correctly - sensors can calm down but still jump from last meausre
    private boolean pauseFrequency(int sleepTime, double frequency) {
        //String msg = sendHTTPMessage("make_frequency", "POST", String.format("%f %d", frequency, sleepTime).getBytes(StandardCharsets.UTF_8));
        //if (msg != null) {
        sleepFor(sleepTime);
        return true;
        //}
        //return false;
    }

    private boolean measureFrequency(int exp, int sleepTime, double frequency) throws Exception {
        String msg = sendHTTPMessage("make_frequency", "POST", String.format("%f %d", frequency, sleepTime).getBytes(StandardCharsets.UTF_8));
        if (msg != null) {
            mExperimentManager.startExp();
            sleepFor(sleepTime);
            this.should_stop_background();
            mExperimentManager.endExp(exp);
            int pause = new Double(sleepTime * 0.2).intValue();
            return pauseFrequency(pause, pauseFrequencyHZ);
        }
        return false;
    }

    //we are trying to rock this boy!
    //startSearchForDiamonds
    @Override
    protected Integer doInBackground(String... strings) {
        if (null == sendHTTPMessage("", "GET", null)) {
            mHandler.sendMessage(mHandler.obtainMessage(noNetWork));
            return 2;
        }
        this.should_stop = false;

        try {
            switch (mode) {
                case "rock":
                    return mining();
                case "record":
                    return record();
                default:
                    return 5;
            }
        } catch (Exception e) {
            Log.e("Catch unhandled exception %s", e.toString());
            return 5;
        }
    }

    private Integer record() {
        mExperimentManager.startExp();
        while (!this.should_stop)
            sleepFor(500);
        List<SensorTracker.SensorData> result = mExperimentManager.endExpGetData();
        writeToFS(666, result);
        return 5;
    }

    private Integer record_swipe() {
        try {
            for (double cur_freq = from; cur_freq <= to; cur_freq += jump) {
                List<SensorTracker.SensorData> XYZT = measureFrequencyXYZ((int) dwell, cur_freq);
                if (XYZT == null)
                    return -1;
                writeToFS(cur_freq, XYZT);
            }
        } catch (Exception e) {
            Log.e("Catch unhandled exception %s", e.toString());
            return 5;
        }
        return 0;
    }

    private void sleepFor(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.should_stop = true;
        this.mExperimentManager.endExp(0);
    }
}
