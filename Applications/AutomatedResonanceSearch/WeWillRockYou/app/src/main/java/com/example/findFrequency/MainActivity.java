package com.example.findFrequency;

import static com.example.findFrequency.ConnectToServer.noNetWork;
import static com.example.findFrequency.ConnectToServer.updateFinalResponse;
import static com.example.findFrequency.ConnectToServer.updateTimeUI;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.BreakIterator;

public class MainActivity extends Activity {

    private TextView comment;
    private EditText server_url;
    private EditText measure_instructions;
    private ProgressBar progressBar;

    public ConnectToServer CTS;
    private boolean inMeasure = false;
    private SensorRegister sr;
    private CountDownTimer currentTimer;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            //Log.d("Main_Activity", "Message handler: " + message.what);
            switch (message.what) {
                case updateTimeUI:
                    double[] arr = (double[]) message.obj;
                    updateEstimation(arr[0], (int) arr[1]);
                    runProgressBar((int) arr[0], (int) arr[1]);
                    break;
                case updateFinalResponse:
                    netResponse((int) message.obj);
                    break;
                case noNetWork:
                default:
                    Log.d("Main_Activity", "Message handler: " + message.what);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        Log.d("Main_Activity", "started main");
        sr = new SensorRegister((SensorManager) getSystemService(SENSOR_SERVICE), new Grapher(findViewById(R.id.graph)));
        initUI();
    }

    private void initUI() {
        this.comment = findViewById(R.id.comment);
        this.server_url = findViewById(R.id.server_dest);
        this.measure_instructions = findViewById(R.id.measure_instructions);
        this.progressBar = findViewById(R.id.progressBar);
        setButtons();
    }

    private void presentWaitingToast(int seconds, Operation on_finish) {
        Toast toast = Toast.makeText(getApplicationContext(), String.format("start in %d seconds", seconds), Toast.LENGTH_SHORT);
        toast.show();

        new CountDownTimer(seconds * 1000L, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Toast toast = Toast.makeText(getApplicationContext(), "Go", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("Main_Activity", "start CTS");
                CTS.execute();
            }
        }.start();
    }

    private void setButtons() {
        RadioGroup radioGroup = findViewById(R.id.myRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radioGyro) {
                    sr.changeGraph(0);
                } else if (checkedId == R.id.radioAccel) {
                    sr.changeGraph(1);
                } else if (checkedId == R.id.radioMagnet) {
                    sr.changeGraph(2);
                } else {
                    sr.changeGraph(-1);
                }
            }
        });

        final Button start_rocking = findViewById(R.id.rock_me);
        start_rocking.setOnClickListener(v -> {
            if (inMeasure)
                return;
            inMeasure = true;

            this.comment.setText("");
            CTS = new ConnectToServer(mHandler, this.server_url.getText().toString(), new ExperimentManager(sr), this.measure_instructions.getText().toString(), "rock");
            presentWaitingToast(3, ()->CTS.execute());
        });
        final Button start_record = findViewById(R.id.record_me);
        start_record.setOnClickListener(v -> {
            if (inMeasure)
                return;
            inMeasure = true;

            this.comment.setText("");
            CTS = new ConnectToServer(mHandler, this.server_url.getText().toString(), new ExperimentManager(sr), this.measure_instructions.getText().toString(), "record");
            presentWaitingToast(3, ()->CTS.execute());
        });
        final Button stop_experiment = findViewById(R.id.stop_me);
        stop_experiment.setOnClickListener(v -> {
            if (!inMeasure || CTS == null)
                return;

            CTS.stop();
            CTS = null;
            this.comment.setText("Stoping");
        });
    }

    public void netResponse(int result) {
        Log.d("Main_Activity", "netResponse");
        if (result == 0) {
            finishedSearch("your frequency is " + CTS.getResonance());
            this.progressBar.getProgressDrawable().setColorFilter(
                    Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
            //TODO add recording options from here on
        } else if (result == 1) {
            this.comment.setText("failed");
            this.progressBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (result == 2) {
            this.comment.setText("Cannot connect server");
            inMeasure = false;
        } else if (result == 3) {
            this.comment.setText("Server crashed");
            inMeasure = false;
        } else if (result == 4) {
            this.comment.setText("fs failed");
            inMeasure = false;
        } else if (result == 5) {
            this.comment.setText("measure stopped");
            inMeasure = false;
        } else {
            this.comment.setText("NO");
        }
    }

    private String numberToFormat(int number, int digits) {
        String out = number + "";
        for (int i = 0; i < digits - out.length(); i++)
            out = "0" + out;
        return out;
    }

    public void updateEstimation(double timeMS, int iters) {
        TextView estimation = findViewById(R.id.comment);
        int time = (int) timeMS / 1000;
        String out = "";
        if (time > 120) {
            int seconds = time % 60;
            time = time - seconds;
            time = time / 60;
            out = String.format(getResources().getString(R.string.timeSt),
                    numberToFormat(time, 2), numberToFormat(seconds, 2));
        } else if (time > 0)
            out = time + " seconds";
        else
            out = timeMS + " MS";
        out = out + " * " + iters;
        estimation.setText(out);
    }

    void pushProgressBar(double part) {
        //Log.d("Main_Activity", ""+part*progressBar.getMax());
        this.progressBar.setProgress((int) (part * this.progressBar.getMax()));
    }

    void finishedSearch(String sol) {
        TextView fin = findViewById(R.id.fin);
        fin.setVisibility(View.VISIBLE);
        TextView estimation = findViewById(R.id.comment);
        estimation.setText(sol);
    }

    void runProgressBar(int millis, int iter) {
        final double totMillis = (double) millis * iter;
        if (currentTimer != null)
            currentTimer.cancel();
        currentTimer = new CountDownTimer((int) totMillis, iter) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Log.d("tick", ""+i++);
                double part = (totMillis - millisUntilFinished) / totMillis;
                pushProgressBar(part);
            }

            public void onFinish() {
                pushProgressBar(1);
            }
        }.start();
    }
}
