package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.InputManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.InputParser;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork.Classifier;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.Preprocessor;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork.NeuronalNetworksManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.PreprocessManager;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineManager {
    private final InputManager inputManager;
    private final PreprocessManager preprocessManager;
    private final NeuronalNetworksManager neuronalNetworksManager;
    private final Map<String, Pipeline> names_to_pipeline = new HashMap<>();

    public PipelineManager(InputManager inputManager, PreprocessManager preprocessManager,
                           NeuronalNetworksManager neuronalNetworksManager) {
        this.inputManager = inputManager;
        this.preprocessManager = preprocessManager;
        this.neuronalNetworksManager = neuronalNetworksManager;
    }

    public Pipeline get_pipeline(String pipeline_name) {
        if (names_to_pipeline.containsKey(pipeline_name))
            return names_to_pipeline.get(pipeline_name);
        InputParser input_parser = inputManager.get_input_parser(pipeline_name);
        Preprocessor<List<SensorEventSdios>, ?> preprocessor = preprocessManager.get_preprocessor(pipeline_name);
        List<Classifier<TensorBuffer, TensorBuffer>> classifiers = neuronalNetworksManager
                .get_classifiers(pipeline_name);
        assert classifiers != null;
        Pipeline new_pipeline = new Pipeline(pipeline_name, input_parser, preprocessor, classifiers);
        names_to_pipeline.put(pipeline_name, new_pipeline);
        return new_pipeline;
    }
}
