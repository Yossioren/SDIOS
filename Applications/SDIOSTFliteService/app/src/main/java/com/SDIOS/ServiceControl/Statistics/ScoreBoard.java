package com.SDIOS.ServiceControl.Statistics;

import android.hardware.Sensor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.SDIOS.ServiceControl.R;

import java.util.HashMap;
import java.util.Map;

public class ScoreBoard extends AppCompatActivity {
    public final static Map<Integer, String> sensorMap = new HashMap<Integer, String>() {{
        put(Sensor.TYPE_GYROSCOPE, "gyroscope");
        put(Sensor.TYPE_ACCELEROMETER, "accelerometer");
        put(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
    }};
    private TextView statistics_text;
    private StatisticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        statistics_text = findViewById(R.id.statistics);
        tracker = new StatisticsMemoryStore();
        Button reset_statistics = findViewById(R.id.reset_statistics);
        Button reload_statistics = findViewById(R.id.reload_statistics);
        reset_statistics.setOnClickListener(view -> tracker.reset());
        reload_statistics.setOnClickListener(view -> this.set_statistics());
        set_statistics();
    }

    private void set_statistics() {
        statistics_text.setText(tracker.print_statistics());
    }
}