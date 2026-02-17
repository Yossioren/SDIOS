package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayFromBuffer;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FixedInput implements Preprocessor<DataFrame, DataFrame> {
    private static final double nanosecond_to_second = 1000000000L;
    private final long samples_increment;
    private final boolean keep_timestamps;

    public FixedInput(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        int samples_per_second = parameters.getInt("samples_per_second");
        this.samples_increment = (long) (nanosecond_to_second / samples_per_second);
        this.keep_timestamps = parameters.optBoolean("keep_timestamps", false);
    }

    @Override
    public DataFrame process(DataFrame input) {
        assert !input.is_empty();
        assert input.containsKey("t");
        NDArray<Long> timestamps = input.get("t");
        DataFrame output = new DataFrame();
        NDArray<Long> new_timestamps = get_timestamps(timestamps);
        for (String key : input.get_keys()) {
            if (key.equals("t")) continue;
            output.put(key, fix_time(input.get(key), timestamps, new_timestamps));
        }
        if (keep_timestamps)
            output.put("t", new_timestamps);
        return output;
    }

    private NDArray<Long> get_timestamps(NDArray<Long> timestamps) {
        long start_time = timestamps.get_element(0);
        long end_time = timestamps.get_element(timestamps.get_current_layer_size() - 1);
        int amount = (int) ((end_time - start_time) / samples_increment);
        Long[] output = new Long[amount];
        long current_timestamp = 0;
        for (int i = 0; i < amount; i++) {
            output[i] = current_timestamp;
            current_timestamp += samples_increment;
        }
        return new NDArrayFromBuffer<>(output, amount);
    }

    private NDArray<Double> fix_time(NDArray<Double> data, NDArray<Long> data_timestamps, NDArray<Long> new_timestamps) {
        /*
        x, y are following SensorEvent
        We will calculate c which is the estimated value at fixed HZ divided space
        |-|-...-|-...-|-|, |-| is the fixed time difference between two following c, configured by samples_per_second
        x--c---y
        calculate diff y-c c-x use x and y with relative to the diff - the one that is closer is the effective
        */
        assert data_timestamps.shape_equals(data); // should be the same since they are representing one event
        List<Double> output = new ArrayList<>();
        long start_time = data_timestamps.get_element(0);
        Iterator<Double> data_iterator = data.iterator_element();
        Iterator<Long> data_time_iterator = data_timestamps.iterator_element();
        Iterator<Long> iterator = new_timestamps.iterator_element();
        long current_data_time = data_time_iterator.next();
        long previous_data_time = current_data_time;
        double current_data = data_iterator.next();
        double previous_data = current_data;
        while (iterator.hasNext()) {
            long current_time = iterator.next() + start_time;
            while (data_iterator.hasNext() && current_time >= current_data_time) {
                previous_data_time = current_data_time;
                current_data_time = data_time_iterator.next();
                previous_data = current_data;
                current_data = data_iterator.next();
            }
            assert previous_data_time <= current_time && current_time < current_data_time;

            long dptot = Math.max(current_data_time - previous_data_time, 1);
            long dp1 = (current_time - previous_data_time) / dptot;
            long dp2 = 1 - dp1;
            // The ratio multiplication should be inverse as used here - what is closer should get more weight
            output.add(previous_data * dp2 + current_data * dp1);
        }
        return NDArrayAsList.get_1d_array(output);
    }
}
