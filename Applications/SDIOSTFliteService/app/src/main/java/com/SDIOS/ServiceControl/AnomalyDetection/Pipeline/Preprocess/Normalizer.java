package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class Normalizer implements Preprocessor<DataFrame, DataFrame> {
    private final double from_start;
    private final double to_start;
    private final double to_end;
    private final double space_relation;

    public Normalizer(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        this.from_start = parameters.getDouble("from_start");
        double from_end = parameters.getDouble("from_end");
        this.to_start = parameters.optDouble("to_start", 0);
        this.to_end = parameters.optDouble("to_end", 1);
        this.space_relation = (to_end - to_start) / (from_end - from_start);
    }

    @Override
    public DataFrame process(DataFrame input) {
        for (String key : input.get_keys()) {
            NDArray<Double> array = input.get(key);
            input.put(key, new NDArrayAsList<>(normalize(array), array.shape));
        }
        return input;
    }

    private Double normalize_value(double value) {
        /*
        From start-end
        To   start-end
        for example -1->2 to 10->12
         */
        value -= from_start;
        value *= space_relation;
        value += to_start;
        value = Math.min(to_end, value);
        value = Math.max(to_start, value);
        return value;
    }

    private List<?> normalize(NDArray<Double> array) {
        if (array.shape.length > 1) {
            return array.stream_sub_array().map(this::normalize).collect(Collectors.toList());
        }
        return array.stream_element().mapToDouble(this::normalize_value).boxed().collect(Collectors.toList());
    }
}
