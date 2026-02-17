package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import static com.SDIOS.ServiceControl.AnomalyDetection.uilts.MyArrays.get_shape_array;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class InitializeTensorFlowBuffer implements Preprocessor<DataFrame, Map<String, TensorBuffer>> {
    private final String type;
    private final int[] shape;

    public InitializeTensorFlowBuffer(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        type = parameters.optString("type", "float32");
        shape = get_shape_array(parameters, "shape");
    }

    public static TensorBuffer get_tensor_buffer(String type, int[] shape) {
        if (type.contains("int"))
            return TensorBuffer.createFixedSize(shape, DataType.UINT8);
        else if (type.contains("float"))
            return TensorBuffer.createFixedSize(shape, DataType.FLOAT32);
        throw new RuntimeException("No such TensorBuffer type: " + type + ", please choose uint8/float32");
    }

    @Override
    public Map<String, TensorBuffer> process(DataFrame input) {
        assert !input.is_empty();
        Map<String, TensorBuffer> output = new HashMap<>();
        for (String key : input.get_keys())
            output.put(key, process_axis(input.get(key)));
        return output;
    }

    @NonNull
    private TensorBuffer process_axis(NDArray input) {
        TensorBuffer buffer = get_tensor_buffer(type, shape);
        this.convert_array(buffer, this.flatten_double(input));
        return buffer;
    }

    private double[] flatten_double(NDArray<Double> array) {
        return flatten(array).mapToDouble(num -> (double) num).toArray();
    }

    private Stream flatten(NDArray<Double> array) {
        if (array.shape.length > 1) {
            Stream output = Stream.of();
            for (Iterator it = array.iterator(); it.hasNext(); )
                output = Stream.concat(output, flatten((NDArray<Double>) it.next()));
            return output;
        }
        return array.stream();
    }

    private void convert_array(TensorBuffer buffer, double[] input) {
        float[] copy = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            copy[i] = (float) input[i];
        }
        buffer.loadArray(copy, shape);
    }
}
