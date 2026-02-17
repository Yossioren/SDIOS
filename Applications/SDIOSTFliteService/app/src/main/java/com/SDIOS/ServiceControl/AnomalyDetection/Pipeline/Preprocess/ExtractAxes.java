package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExtractAxes implements Preprocessor<List<SensorEventSdios>, DataFrame> {
    private final List<String> axes = new LinkedList<>();
    private final Map<String, Integer> axis_array_location = new HashMap<String, Integer>() {{
        put("x", 0);
        put("y", 1);
        put("z", 2);
    }};

    public ExtractAxes(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        JSONArray axes = parameters.getJSONArray("axes");
        for (int i = 0; i < axes.length(); i++) {
            this.axes.add(axes.getString(i));
        }
    }

    @Override
    public DataFrame process(List<SensorEventSdios> input) {
        DataFrame dataFrame = new DataFrame();
        for (String axis : axes) {
            if (axis.equals("t"))
                dataFrame.put(axis, extract_timestamp(input));
            else if (axis_array_location.containsKey(axis))
                extract_axis(dataFrame, input, axis, axis_array_location.get(axis));
        }
        return dataFrame;
    }

    private void extract_axis(DataFrame output, List<SensorEventSdios> input, String axis, int location) {
        List<Double> axis_list = new ArrayList<>();
        input.stream().mapToDouble(event -> (double) event.values[location]).forEachOrdered(axis_list::add);
        output.put(axis, NDArrayAsList.get_1d_array(axis_list));
    }

    @NonNull
    private NDArray extract_timestamp(List<SensorEventSdios> input) {
        List<Long> timestamps = new ArrayList<>();
        input.stream().mapToLong(event -> event.timestamp).forEachOrdered(timestamps::add);
        return NDArrayAsList.get_1d_array(timestamps);
    }
}
