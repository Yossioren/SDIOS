package com.SDIOS.ServiceControl.AnomalyDetection.Loss;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;

public class MSE implements LossFunction {
    @Override
    public double calculate(NDArray<Double> array) {
        assert array.elements_count() > 0;
        double mean = NDArray.sum(array) / array.elements_count();
        return array.values().parallel().mapToDouble(value -> value - mean).map(value -> value * value).sum() / array.elements_count();
    }
}
