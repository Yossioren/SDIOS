package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork;

public interface Classifier<I, O> {
    // classifier constructor always get path in order to load the model
    O compute(I input);
}
