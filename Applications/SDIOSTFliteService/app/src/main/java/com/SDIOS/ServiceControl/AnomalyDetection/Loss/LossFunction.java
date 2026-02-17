package com.SDIOS.ServiceControl.AnomalyDetection.Loss;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;

public interface LossFunction {
    double calculate(NDArray<Double> array);
}
