package com.SDIOS.ServiceControl.AnomalyDetection;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.InputManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork.NeuronalNetworksManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.PipelineManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.PreprocessManager;
import com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.ResultAnalyzersManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONException;


public class PackageParser {
    public final ResultAnalyzersManager resultAnalyzersManager;
    public final PipelineManager pipelineManager;

    public PackageParser(ClassifiersPackage classifiersPackage) throws JSONException {
        InputManager inputManager = new InputManager(classifiersPackage);
        PreprocessManager preprocessManager = new PreprocessManager(classifiersPackage);
        NeuronalNetworksManager neuronalNetworksManager = new NeuronalNetworksManager(classifiersPackage, false);
        pipelineManager = new PipelineManager(inputManager, preprocessManager, neuronalNetworksManager);
        resultAnalyzersManager = new ResultAnalyzersManager(classifiersPackage);
    }

    public PackageParser(ClassifiersPackage classifiersPackage, boolean allow_missing_files) throws JSONException {
        InputManager inputManager = new InputManager(classifiersPackage);
        PreprocessManager preprocessManager = new PreprocessManager(classifiersPackage);
        NeuronalNetworksManager neuronalNetworksManager = new NeuronalNetworksManager(classifiersPackage, allow_missing_files);
        pipelineManager = new PipelineManager(inputManager, preprocessManager, neuronalNetworksManager);
        resultAnalyzersManager = new ResultAnalyzersManager(classifiersPackage);
    }
}
