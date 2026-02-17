package com.SDIOS.ServiceControl;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.EnforceSize;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.ExtractAxes;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.FixedInput;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.GAF;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.InitializeTensorFlowBuffer;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.L2norm;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.Normalizer;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayFromBuffer;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.utils.ContextHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PreprocessTest {
    @Before
    public void init() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContextHolder.set_context(appContext);
    }

    public List<SensorEventSdios> generateDefaultsEvents(int size) {
        return generateEvents(size, 3, 1000000000 / 120, -30, 30);
    }

    public List<SensorEventSdios> generateEvents(int size, int axes, long time_diff, double min, double max) {
        List<SensorEventSdios> list = new LinkedList<>();
        Random random = new Random();
        long timestamp = System.currentTimeMillis() / 1000;
        for (int i = 0; i < size; i++) {
            float[] values = new float[axes];
            for (int j = 0; j < axes; j++)
                values[j] = (float) (min + random.nextDouble() * (max - min));
            timestamp += time_diff;
            list.add(new SensorEventSdios(timestamp, values));
        }
        return list;
    }

    public UserConfigManager wrapParameters(JSONObject parameters) throws JSONException {
        // should be implemented with mock but could not get it working in this environment
        UserConfigManager userConfigManager = new UserConfigManager(new JSONObject("{\"method\": test,\"parameters\":" + parameters.toString() + "}"), "test");
        userConfigManager.parameters = parameters;
        return userConfigManager;
    }

    @Test
    public void testExtractAxes() throws JSONException {
        int size = 300;
        ExtractAxes extractAxes = new ExtractAxes(wrapParameters(new JSONObject("{\"axes\": [\"x\", \"y\", \"missing_axis\", \"t\"]}")));
        List<SensorEventSdios> list = generateDefaultsEvents(size);
        DataFrame processed_output = extractAxes.process(list);
        assert processed_output.containsKey("x") && processed_output.get("x").get_current_layer_size() == size;
        assert processed_output.containsKey("y") && processed_output.get("y").get_current_layer_size() == size;
        assert processed_output.containsKey("t") && processed_output.get("t").get_current_layer_size() == size;
        assert !processed_output.containsKey("missing_axis");
        assert !processed_output.containsKey("z");
    }

    @Test
    public void testL2norm() throws JSONException {
        int size = 300;
        List<SensorEventSdios> list = generateDefaultsEvents(size);
        DataFrame processed_output = new ExtractAxes(wrapParameters(new JSONObject("{\"axes\": [\"x\", \"y\", \"z\", \"t\"]}"))).process(list);
        processed_output = new L2norm(wrapParameters(new JSONObject("{\"axes\": [\"x\", \"y\", \"z\"]}"))).process(processed_output);
        assert processed_output.containsKey("l2norm") && processed_output.get("l2norm").get_current_layer_size() == size;
        assert processed_output.containsKey("t") && processed_output.get("t").get_current_layer_size() == size;
    }

    @Test
    public void testEnforceSize() throws JSONException {
        int size = 300;
        List<SensorEventSdios> list = generateDefaultsEvents(size);
        DataFrame processed_output = new ExtractAxes(wrapParameters(new JSONObject("{\"axes\": [\"x\", \"y\", \"t\"]}"))).process(list);
        assert processed_output.containsKey("x") && processed_output.get("x").get_current_layer_size() == size;
        assert processed_output.containsKey("y") && processed_output.get("y").get_current_layer_size() == size;
        assert processed_output.containsKey("t") && processed_output.get("t").get_current_layer_size() == size;
        int wanted_size = 120;
        processed_output = new EnforceSize(wrapParameters(new JSONObject("{\"shape\": [" + wanted_size + "]}"))).process(processed_output);
        assert processed_output.containsKey("x") && processed_output.get("x").get_current_layer_size() == wanted_size;
        assert processed_output.containsKey("y") && processed_output.get("y").get_current_layer_size() == wanted_size;
        assert processed_output.containsKey("t") && processed_output.get("t").get_current_layer_size() == wanted_size;
    }

    @Test
    public void testNormalize() throws JSONException {
        int size = 300;
        List<SensorEventSdios> events = generateDefaultsEvents(size);
        String[] axes = new String[]{"x", "y", "z"};
        DataFrame processed_axes = new ExtractAxes(wrapParameters(new JSONObject("{\"axes\": " + Arrays.toString(axes) + "}"))).process(events);
        for (String axis : axes)
            assert processed_axes.containsKey(axis) && processed_axes.get(axis).get_current_layer_size() == size;
        test_normalizer_range(processed_axes, axes, size, -0.5, 2);
        test_normalizer_range(processed_axes, axes, size, -20, -10);
        test_normalizer_range(processed_axes, axes, size, 0, 1);
    }

    private void test_normalizer_range(DataFrame processed_axes, String[] axes, int size, double lower_value, double upper_value) throws JSONException {
        DataFrame normalized_output = new Normalizer(wrapParameters(new JSONObject("{\"from_start\": -30, \"from_end\": 30, \"to_start\": " + lower_value + ", \"to_end\": " + upper_value + "}"))).process(processed_axes);
        for (String axis : axes)
            assert normalized_output.containsKey(axis) && normalized_output.get(axis).get_current_layer_size() == size;
        for (String key : normalized_output.get_keys()) {
            NDArray<Double> array = normalized_output.get(key);
            for (Iterator<Double> it = array.iterator_element(); it.hasNext(); ) {
                double value = value = it.next();
                assert lower_value <= value && value <= upper_value;
            }
        }
    }

    @Test
    public void testFixedInput() throws JSONException {
        fixed_input_test_helper(2, 120, 60);
        fixed_input_test_helper(2, 30, 60);
    }

    private void fixed_input_test_helper(int time, int sampling, int fixed_sampling) throws JSONException {
        int size = sampling * time + 2;
        int new_size = fixed_sampling * time;
        List<SensorEventSdios> list = generateEvents(size, 3, 1000000000 / sampling, -30, 30);
        String[] axes = new String[]{"x", "y", "z", "t"};
        DataFrame processed_output = new ExtractAxes(wrapParameters(new JSONObject("{\"axes\": " + Arrays.toString(axes) + "}"))).process(list);
        for (String axis : axes)
            assert processed_output.containsKey(axis) && processed_output.get(axis).get_current_layer_size() == size;
        DataFrame fixed_output = new FixedInput(wrapParameters(new JSONObject("{\"samples_per_second\": " + fixed_sampling + ", \"keep_timestamps\": true}"))).process(processed_output);
        for (String axis : axes) {
//            Log.e("fixed_input_test_helper", axis + ":" + fixed_output.get(axis).size());
            assert fixed_output.containsKey(axis) && Math.abs(fixed_output.get(axis).get_current_layer_size() - new_size) <= 2;
        }
    }

    /*
    from pyts.image import GramianAngularField
    import numpy as np
    def get_gaf(data, method):
        return np.array(GramianAngularField(method=method).fit_transform(data))
    get_gaf([[-2,-1,0,1,2]], "summation")
    */
    @Test
    public void testGaf() throws JSONException {
        double[][] wanted_result1 = (double[][]) Arrays.asList(
                DoubleStream.of(1., 0.5, -0., -0.5, -1.).toArray(),
                DoubleStream.of(0.5, -0.5, -0.8660254, -1., -0.5).toArray(),
                DoubleStream.of(-0., -0.8660254, -1., -0.8660254, 0.).toArray(),
                DoubleStream.of(-0.5, -1., -0.8660254, -0.5, 0.5).toArray(),
                DoubleStream.of(-1., -0.5, 0., 0.5, 1.).toArray()).toArray();

        test_gaf_helper("summation", Arrays.asList(-2.0, -1.0, 0.0, 1.0, 2.0), wanted_result1);
        double[][] wanted_result2 = (double[][]) Arrays.asList(
                DoubleStream.of(0., 0.8660254, 1., 0.8660254, 0.).toArray(),
                DoubleStream.of(-0.8660254, 0., 0.5, 0.8660254, 0.8660254).toArray(),
                DoubleStream.of(-1., -0.5, 0., 0.5, 1.).toArray(),
                DoubleStream.of(-0.8660254, -0.8660254, -0.5, 0., 0.8660254).toArray(),
                DoubleStream.of(-0., -0.8660254, -1., -0.8660254, 0.).toArray()).toArray();
        test_gaf_helper("difference", Arrays.asList(-2.0, -1.0, 0.0, 1.0, 2.0), wanted_result2);

        double[][] wanted_result3 = (double[][]) Arrays.asList(
                DoubleStream.of(-0.25560005, -0.61008194, 0.25182303, 0.61008194, -0.13069418).toArray(),
                DoubleStream.of(-0.61008194, 1., -0.92043656, -1., -0.70580807).toArray(),
                DoubleStream.of(0.25182303, -0.92043656, 0.69440693, 0.92043656, 0.37274246).toArray(),
                DoubleStream.of(0.61008194, -1., 0.92043656, 1., 0.70580807).toArray(),
                DoubleStream.of(-0.13069418, -0.70580807, 0.37274246, 0.70580807, -0.00366993).toArray()).toArray();
        test_gaf_helper("summation", Arrays.asList(0.6956980556498422, 22.03915232383573, -3.418402905006822, -4.473106125593635, -0.5732599041415725), wanted_result3);

        double[][] wanted_result4 = (double[][]) Arrays.asList(
                DoubleStream.of(0., 0.79233833, -0.49082103, -0.79233833, -0.12705484).toArray(),
                DoubleStream.of(-0.79233833, 0., -0.39089197, -0., -0.70840311).toArray(),
                DoubleStream.of(0.49082103, 0.39089197, 0., -0.39089197, 0.37614541).toArray(),
                DoubleStream.of(0.79233833, 0., 0.39089197, 0., 0.70840311).toArray(),
                DoubleStream.of(0.12705484, 0.70840311, -0.37614541, -0.70840311, 0.).toArray()).toArray();
        test_gaf_helper("difference", Arrays.asList(0.6956980556498422, 22.03915232383573, -3.418402905006822, -4.473106125593635, -0.5732599041415725), wanted_result4);

        double[][] wanted_result5 = (double[][]) Arrays.asList(
                DoubleStream.of(0., 0., 0., 0., 0.).toArray(),
                DoubleStream.of(0., 0., 0., 0., 0.).toArray(),
                DoubleStream.of(0., 0., 0., 0., 0.).toArray(),
                DoubleStream.of(0., 0., 0., 0., 0.).toArray(),
                DoubleStream.of(0., 0., 0., 0., 0.).toArray()).toArray();
        test_gaf_helper("difference", Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0), wanted_result5);

        double[][] wanted_result6 = (double[][]) Arrays.asList(
                DoubleStream.of(1., 1., 1., 1., 1.).toArray(),
                DoubleStream.of(1., 1., 1., 1., 1.).toArray(),
                DoubleStream.of(1., 1., 1., 1., 1.).toArray(),
                DoubleStream.of(1., 1., 1., 1., 1.).toArray(),
                DoubleStream.of(1., 1., 1., 1., 1.).toArray()).toArray();
        test_gaf_helper("summation", Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0), wanted_result6);
    }

    private void test_gaf_helper(String method, List<Double> input, double[][] wanted_result) throws JSONException {
        GAF gaf = new GAF(wrapParameters(new JSONObject("{\"method\":\"" + method + "\"}")));
        DataFrame dataFrame = new DataFrame();
        dataFrame.put("x", NDArrayAsList.get_1d_array(input));
        DataFrame output = gaf.process(dataFrame);
        NDArray<Double> current_result = output.get("x");
        assert !Double.isNaN(current_result.get_element(0, 0));
        assert current_result != null && current_result.get_current_layer_size() == wanted_result.length;
        double epsilon = 0.0001;
        NDArray.subtract(current_result, new NDArrayAsList<>(wanted_result, wanted_result.length, wanted_result[0].length));
        NDArray.abs(current_result);
        double sum = NDArray.sum(current_result);
        assert !Double.isNaN(sum);
        assert sum < epsilon;
    }

    @Test
    public void test_tensor_load_unpack() throws JSONException {
        int size = 120;
        int elements = size * size;
        int expected_sum = elements * (elements - 1 - 0) / 2;
        double epsilon = 0.0001;
        DataFrame dataFrame = new DataFrame();
        NDArray<Double> origin_nd = NDArrayFromBuffer.get_instance(
                IntStream.range(0, elements).asDoubleStream().toArray(),
                size, size);
        assert NDArray.sum(origin_nd) - expected_sum < epsilon;
        InitializeTensorFlowBuffer tensor_converter =
                new InitializeTensorFlowBuffer(wrapParameters(
                        new JSONObject(String.format("{\"shape\":[%d,%d]}", size, size))));
        dataFrame.put("x", origin_nd);
        Map<String, TensorBuffer> output = tensor_converter.process(dataFrame);
        TensorBuffer tensorBuffer = output.get("x");
        NDArray<Double> new_nd =
                NDArrayFromBuffer.get_instance(tensorBuffer.getFloatArray(), tensorBuffer.getShape());
        NDArray.subtract(new_nd, origin_nd);
        assert NDArray.sum(new_nd) < epsilon;
    }
}

