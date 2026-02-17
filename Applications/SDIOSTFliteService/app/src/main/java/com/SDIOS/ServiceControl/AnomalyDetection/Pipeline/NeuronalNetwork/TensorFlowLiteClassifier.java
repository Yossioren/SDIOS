package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork;

import static com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.InitializeTensorFlowBuffer.get_tensor_buffer;
import static com.SDIOS.ServiceControl.AnomalyDetection.uilts.MyArrays.get_shape_array;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;


public class TensorFlowLiteClassifier implements Classifier<TensorBuffer, TensorBuffer> {
    private final static String TAG = "TensorFlowClassifier";
    private final String classifier_path;
    private final int[] input_shape;
    private final int[] output_shape;
    private final String input_type;
    private final String output_type;
    private final Interpreter interpreter;

    public TensorFlowLiteClassifier(UserConfigManager userConfigManager) throws JSONException {
        JSONObject parameters = userConfigManager.parameters;
        this.classifier_path = parameters.getString("secure_filename");
        Log.i(TAG, "loading model for " + classifier_path);
        ByteBuffer bb = FileUtils.get_instance().readFileBinary(classifier_path);
        interpreter = new Interpreter(bb);
        input_shape = get_shape_array(parameters, "input_shape");
        interpreter.resizeInput(0, input_shape);
        output_shape = get_shape_array(parameters, "output_shape");
        input_type = parameters.optString("input_type", "float");
        output_type = parameters.optString("output_type", "float");
    }

    @Override
    public TensorBuffer compute(TensorBuffer input) {
        TensorBuffer output = get_tensor_buffer(output_type, output_shape);
        interpreter.run(input.getBuffer(), output.getBuffer());
        return output;
    }
}