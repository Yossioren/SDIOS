package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers;

import java.util.List;

import androidx.annotation.NonNull;

public interface Analyzer<I> {
    // constructor need to have two JSONObject parameters
    double calculate_trust(@NonNull List<I> inputs);

    List<String> required_pipelines();
}
