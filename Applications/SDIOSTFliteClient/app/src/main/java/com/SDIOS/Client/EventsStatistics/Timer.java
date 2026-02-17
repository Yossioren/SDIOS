package com.SDIOS.Client.EventsStatistics;

import android.os.SystemClock;

class Timer extends SkipMessages {
    //emulator - 60
    //emulator with SDIOS - 60-80
    //emulator regular - 60
    private long previous_time_ms = System.currentTimeMillis();

    public Timer() {
        super(0);
    }

    public Timer(int skip_amount) {
        super(skip_amount);
    }

    long timeBetween() {
        long time = System.currentTimeMillis();
        long diff = time - previous_time_ms;
        previous_time_ms = time;
        return diff;
    }

    //emulator - 1.5
    //emulator with SDIOS - 15-20
    //emulator regular - < 1 ms
    double timeDelay(long timestamp) {
        long diff = SystemClock.elapsedRealtimeNanos() - timestamp;
        return diff / 1000000.0;
    }

    //in real phone
    //original sensorManager
    String timeStr(long timestamp) {
        return String.format("%dms %3.3fns", timeBetween(), timeDelay(timestamp));
    }
}
