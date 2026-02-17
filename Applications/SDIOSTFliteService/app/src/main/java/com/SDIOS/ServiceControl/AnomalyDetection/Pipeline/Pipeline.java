package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline;

import android.util.Log;

import androidx.annotation.Nullable;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.InputParser;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork.Classifier;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.Preprocessor;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayFromBuffer;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.List;
import java.util.Map;

public class Pipeline {
    public final String name;
    public final InputParser inputParser;
    public final Preprocessor<List<SensorEventSdios>, ?> preprocessor;
    public final List<Classifier<TensorBuffer, TensorBuffer>> classifiers;
    DataFrame last_evaluate = null;
    private long last_requests_count = -1;

    public Pipeline(String name, InputParser inputParser, Preprocessor<List<SensorEventSdios>, ?> preprocessor, List<Classifier<TensorBuffer, TensorBuffer>> classifiers) {
        this.name = name;
        assert inputParser != null && preprocessor != null && classifiers != null;
        this.inputParser = inputParser;
        this.preprocessor = preprocessor;
        this.classifiers = classifiers;
    }

    boolean add_event(SensorEventSdios sensorEvent) {
        inputParser.add(sensorEvent);
        return inputParser.should_scan_events();
    }

    @Nullable
    public DataFrame run_pipeline(long request_count) {
        /*
        pipeline result is cached to the last event
        this way pipeline can reused in multiple sensorClassifiers
        if skip time cause the calculation to fail - we will return the last_evaluate
         */
        if (request_count == last_requests_count) {
            return last_evaluate;
        }
        DataFrame output = run_pipeline_helper();
        last_requests_count = request_count;
        last_evaluate = output;
        return output;
    }

    @Nullable
    private DataFrame run_pipeline_helper() {
        List<SensorEventSdios> sensorEvents = inputParser.getCollected();
        if (sensorEvents == null) return last_evaluate;
        Map<String, TensorBuffer> output = (Map<String, TensorBuffer>) preprocessor.process(sensorEvents);
        if (output == null) return last_evaluate;
        // WARNING Here we expecting only to process one axis of the map
        for (TensorBuffer tensorBuffer : output.values()) {
            DataFrame dataFrame = new DataFrame();
            dataFrame.put("nn_input", NDArrayFromBuffer.get_instance(tensorBuffer.getFloatArray(), tensorBuffer.getShape()));
            for (Classifier<TensorBuffer, TensorBuffer> classifier : classifiers)
                tensorBuffer = classifier.compute(tensorBuffer);
            dataFrame.put("nn_output", NDArrayFromBuffer.get_instance(tensorBuffer.getFloatArray(), tensorBuffer.getShape()));
            return dataFrame;
        }
        return null;
    }

    public void reset() {
        inputParser.reset();
    }
}
