package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

public interface Preprocessor<I, O> {
    // get parameters as JSON in the constructor
    O process(I input);
}
