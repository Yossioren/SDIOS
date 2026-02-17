package com.SDIOS.ServiceControl.AnomalyDetection.ResultsAnalyzer.analyzers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.AnomalyDetection.Loss.LossFunction;
import com.SDIOS.ServiceControl.AnomalyDetection.Loss.MSE;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigUpdateCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ThresholdAnalyzer extends UserConfigUpdateCallback implements Analyzer<DataFrame> {
    private final static String TAG = "PreprocessManager";
    private final static Map<String, Class<? extends LossFunction>> names_to_loss = new HashMap<String, Class<? extends LossFunction>>() {{
        put("MeanSquaredError", MSE.class);
        put("mse", MSE.class);
    }};
    private final List<String> analyzer_pipelines = new LinkedList<>();
    private LossFunction lossFunction;
    private double threshold_amount;

    public ThresholdAnalyzer(UserConfigManager userConfigManager) throws JSONException {
        super(userConfigManager);
        load_parameters(userConfigManager.parameters);
    }

    @Override
    public void load_parameters(JSONObject parameters) throws JSONException {
        JSONArray pipelines = parameters.getJSONArray("pipelines");
        for (int i = 0; i < pipelines.length(); i++) {
            analyzer_pipelines.add(pipelines.getString(i));
        }
        threshold_amount = parameters.getDouble("threshold_amount");
        parse_loss(parameters.getString("loss"));
    }

    private void parse_loss(String lossName) {
        assert names_to_loss.containsKey(lossName);
        try {
            this.lossFunction = names_to_loss.get(lossName).getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException " + e + ", " + e.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            Log.e(TAG, "Reflection error " + lossName + ": " + e);
        }
    }

    @Override
    public double calculate_trust(@NonNull List<DataFrame> input) {
        double value = 0;
        int i = 0;
        for (DataFrame dataFrame : input) {
            NDArray<Double> before = dataFrame.get("nn_input");
            NDArray<Double> after = dataFrame.get("nn_output");
            NDArray.subtract(after, before);
            value += lossFunction.calculate(after);
            Log.d(TAG, i++ + "v:" + value);
        }
        Log.d(TAG, value + ">" + threshold_amount);
        return threshold_amount > value ? 1 : 0;
    }

    @Override
    public List<String> required_pipelines() {
        return analyzer_pipelines;
    }
}
