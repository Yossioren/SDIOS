package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers.Analyzer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PipelinesEvaluator {
    private final static String TAG = "SensorClassifier";
    private final static ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<Pipeline> pipelines;
    private final String sensor;
    private Analyzer<DataFrame> analyzer;
    private List<DataFrame> last_pipeline_output;

    public PipelinesEvaluator(Analyzer<DataFrame> analyzer, List<Pipeline> pipelines, String sensor) {
        this.analyzer = analyzer;
        this.pipelines = pipelines;
        this.sensor = sensor;
    }

    public void reset() {
        Log.d(TAG, "Resets evaluator");
        for (Pipeline pipeline : pipelines)
            pipeline.reset();
    }

    public Future<Float> run_pipelines(SensorEventSdios event, long request_count) {
        boolean need_to_revaluate = false;
        for (Pipeline pipeline : pipelines)
            need_to_revaluate |= pipeline.add_event(event);
        if (!need_to_revaluate)
            return null;
        CountDownLatch latch = new CountDownLatch(pipelines.size());
        List<Future<?>> pipelines_products = new LinkedList<>();
        for (Pipeline pipeline : pipelines)
            pipelines_products.add(executor.submit(() -> {
                try {
                    return pipeline.run_pipeline(request_count);
                } catch (Throwable throwable) {
                    Log.e(TAG, String.format("Error in executor!! %s: %s", sensor, throwable));
                    return null;
                } finally {
                    latch.countDown();
                }
            }));
        return executor.submit(() -> {
            List<DataFrame> products = new LinkedList<>();
            try {
                latch.await();
                for (Future<?> future : pipelines_products)
                    products.add((DataFrame) future.get());
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, String.format("Failed waiting for executor %s: %s", sensor, e));
                return -1.f;
            }
            if (products.contains(null))
                Log.w(TAG, String.format("Future products are incomplete!  %s: %s", sensor, products));
            else if (!already_computed(products)) {
                last_pipeline_output = products;
                return (float) analyzer.calculate_trust(products);
            }
//            Log.i(TAG, "Finish pipelines!");
            return -1.f;
        });
    }

    private boolean already_computed(List<DataFrame> products) {
        for (int i = 0; i < products.size(); i++)
            if (last_pipeline_output == null || last_pipeline_output.get(i) != products.get(i))
                return false;
        return true;
    }
}
