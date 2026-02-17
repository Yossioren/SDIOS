package com.SDIOS.ServiceControl.AnomalyDetection;

import android.util.Log;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Pipeline;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.PipelinesEvaluator;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers.Analyzer;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.on_detect.OnDetect;
import com.SDIOS.ServiceControl.Statistics.StatisticsMemoryStore;
import com.SDIOS.ServiceControl.Statistics.StatisticsTracker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SensorClassifier implements Evaluator {
    private final static String TAG = "SensorClassifier";
    private final String sensor;
    private final PackageParser packageParser;
    private final StatisticsTracker statisticsTracker = new StatisticsMemoryStore();
    private OnDetect on_detect;
    private PipelinesEvaluator pipelinesEvaluator;
    private double last_evaluation = -1;
    private long request_count = 0;
    private Future<Float> last_future;

    public SensorClassifier(String sensor, PackageParser packageParser) {
        this.sensor = sensor;
        this.packageParser = packageParser;
        setup();
    }

    private void setup() {
        Analyzer<DataFrame> analyzer = (Analyzer<DataFrame>) packageParser.resultAnalyzersManager.get_analyzer(sensor);
        on_detect = packageParser.resultAnalyzersManager.get_on_detect(sensor);
        List<Pipeline> pipelines = new LinkedList<>();
        for (String pipeline_name : analyzer.required_pipelines()) {
            pipelines.add(packageParser.pipelineManager.get_pipeline(pipeline_name));
        }
        pipelinesEvaluator = new PipelinesEvaluator(analyzer, pipelines, sensor);
    }

    private void query_last_future() {
        try {
            if (last_future != null && last_future.isDone()) {
                float new_evaluation = last_future.get();
                last_future = null;
                if (new_evaluation >= 0) {
                    if (on_detect.isTrustAmountMalicious(new_evaluation)) {
                        Log.w(TAG, String.format("Registered detection! %s %f", sensor, new_evaluation));
                        statisticsTracker.registerDetection(sensor);
                    }
                    last_evaluation = new_evaluation;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, String.format("Future get failure  %s: %s", sensor, e));
            last_future = null;
        }
    }

    @Override
    public SensorEventSdios ComputeTrust(SensorEventSdios event) {
        /*
         * to battle syncing issues in the ms_skip for each pipeline
         * we will cache the object they returned and test if we already compute a
         * detection for this
         * pipeline will return new dataframe only when they skip_ms is set
         */
        request_count++;
        query_last_future();
        Future<Float> products_future = pipelinesEvaluator.run_pipelines(event, request_count);
        if (products_future != null) {
            if (last_future == null)
                last_future = products_future;
            else
                Log.w(TAG, String.format("Old evaluation of %s not finished yet! Dropping new evaluation", sensor));
        }
        event.trust = (float) last_evaluation;
        statisticsTracker.registerTimeDifference(sensor, event);
        // on_detect can alter or null the event
        if (on_detect.isTrustAmountMalicious(event.trust))
            event = on_detect.actionOnDetection(event);
        return event;
    }

    @Override
    public void reset() {
        pipelinesEvaluator.reset();
        last_evaluation = -1;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Evaluator for %s ", this.sensor);
    }

    @Override
    public String getSensorName() {
        return this.sensor;
    }
}
