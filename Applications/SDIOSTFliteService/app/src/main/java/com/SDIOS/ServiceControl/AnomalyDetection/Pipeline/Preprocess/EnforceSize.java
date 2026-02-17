package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import static com.SDIOS.ServiceControl.AnomalyDetection.uilts.MyArrays.get_shape_array;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class EnforceSize implements Preprocessor<DataFrame, DataFrame> {
    private static final int allowed_missing = 1;
    private final int[] shape;

    public EnforceSize(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        this.shape = get_shape_array(parameters, "shape");
    }

    @Override
    public DataFrame process(DataFrame input) {
        assert !input.is_empty();
        for (String key : input.get_keys()) {
            input.put(key, fix_shape(input.get(key)));
        }
        return input;
    }

    private NDArray<?> fix_shape(NDArray<?> array) {
        assert array.shape.length == shape.length;
        int wanted_size = shape[shape.length - 1];
        return fix_shape_recursive(array, wanted_size);
    }

    @NonNull
    private NDArray<? extends Number> fix_shape_recursive(NDArray<?> array, int wanted_size) {
        if (array.shape.length > 1)
            return new NDArrayAsList<>(array.stream_sub_array().map(ndarray -> this.fix_shape_recursive(ndarray, wanted_size)).collect(Collectors.toList()));
        if (array.shape[0] != wanted_size)
            return this.fix_shape_helper(array, wanted_size);
        return array;
    }

    private NDArray<? extends Number> fix_shape_helper(NDArray<?> array, int wanted_size) {
        List<Object> list = array.stream().collect(Collectors.toList());
        if (list.size() > wanted_size) {
            list.subList(0, list.size() - wanted_size).clear();
        } else if (list.size() < wanted_size) {
            int missing_count = wanted_size - list.size();
            assert missing_count <= allowed_missing;
            Object last_value = list.get(list.size() - 1);
            for (; missing_count > 0; missing_count--) {
                list.add(last_value);
            }
        }
        return new NDArrayAsList<>(list, wanted_size);
    }
}
