package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class L2norm implements Preprocessor<DataFrame, DataFrame> {
    private final JSONArray axes;

    public L2norm(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        this.axes = parameters.getJSONArray("axes");
        assert axes.length() > 0;
    }

    @Override
    public DataFrame process(DataFrame input) {
        DataFrame output = new DataFrame();
        if (input.containsKey("t"))
            output.put("t", input.get("t"));
        output.put("l2norm", extract_l2norm(input));
        return output;
    }

    private NDArray extract_l2norm(DataFrame input) {
        assert !input.is_empty();
        String axis_0 = input.get_keys().iterator().next();
        int items_amount = input.get(axis_0).get_current_layer_size();
        List<Double> output = new ArrayList<>(Collections.nCopies(items_amount, 0.0));
        for (int i = 0; i < axes.length(); i++) {
            Iterator<Double> axis_iterator = get_axis(input, i);
            int j = 0;
            for (; j < output.size() && axis_iterator.hasNext(); j++) {
                double value = axis_iterator.next();
                output.set(j, output.get(j) + value * value);
            }
            assert j == output.size() && !axis_iterator.hasNext();
        }
        return NDArrayAsList.get_1d_array(output.parallelStream().
                mapToDouble(Math::sqrt).boxed().collect(Collectors.toList()));
    }

    private Iterator get_axis(DataFrame dataFrame, int index) {
        String axis = axes.optString(index);
        assert dataFrame.containsKey(axis);
        return dataFrame.get(axis).iterator();
    }
}
