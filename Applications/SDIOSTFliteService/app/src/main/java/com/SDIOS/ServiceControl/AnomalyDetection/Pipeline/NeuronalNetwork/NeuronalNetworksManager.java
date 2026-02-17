package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NeuronalNetworksManager {
    private final static String TAG = "NeuronalNetworksManager";
    private final static Map<String, Class<? extends Classifier<?, ?>>> names_to_classifier_class = new HashMap<String, Class<? extends Classifier<?, ?>>>() {
        {
            put("TensorFlowLiteNeuronalNetwork", TensorFlowLiteClassifier.class);
        }
    };

    private final Map<String, Classifier<TensorBuffer, TensorBuffer>> names_to_classifier = new HashMap<>();
    private final Map<String, List<Classifier<TensorBuffer, TensorBuffer>>> pipeline_to_classifiers = new HashMap<>();
    private final ClassifiersPackage package_config;
    private boolean allow_missing_files = false;

    public NeuronalNetworksManager(ClassifiersPackage package_config, boolean allow_missing_files) throws JSONException {
        this.package_config = package_config;
        this.allow_missing_files = allow_missing_files;
        parse_networks();
        parse_pipeline_required_network();
    }

    private void parse_networks() {
        JSONArray files = this.package_config.origin_json.optJSONArray("neural_networks");
        assert files != null;
        try {
            load_models(files);
        } catch (InvocationTargetException e) {
            Log.e(TAG, String.format("InvocationTargetException %s, %s", e, e.getTargetException()));
            throw new AssertionError(String.format("InvocationTargetException %s, %s", e, e.getTargetException()));
        }
    }

    private void load_models(JSONArray files) throws InvocationTargetException {
        for (int i = 0; i < files.length(); i++) {
            JSONObject classifier_info = files.optJSONObject(i);
            String classifier_name = classifier_info.optString("name");
            String method = "";
            try {
                set_secure_path(classifier_info);
                UserConfigManager userConfigManager = new UserConfigManager(classifier_info, TAG, "classifier_info_",
                        classifier_name);
                method = userConfigManager.method;
                assert names_to_classifier_class.containsKey(method);

                if (this.allow_missing_files) {
                    Log.w(TAG, "load model invocation skip");
                    continue;
                }
                Classifier classifier = names_to_classifier_class.get(method)
                        .getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManager);
                names_to_classifier.put(classifier_name, classifier);
            } catch (JSONException e) {
                Log.e(TAG, "JSON error " + e);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                Log.e(TAG, "Reflection error " + method + ": " + e);
            }
        }
    }

    public List<Pair<String, String>> get_dependencies() throws JSONException {
        List<Pair<String, String>> output = new LinkedList<>();
        JSONArray files = this.package_config.origin_json.optJSONArray("neural_networks");
        assert files != null;
        for (int i = 0; i < files.length(); i++) {
            JSONObject file_info = files.optJSONObject(i);
            JSONObject file_parameters = file_info.getJSONObject("parameters");
            String package_dependency_filename = file_parameters.getString("filename");
            String package_dependency_path = file_parameters.getString("secure_filename");
            output.add(new Pair<>(package_dependency_filename, package_dependency_path));
        }
        return output;
    }

    private void set_secure_path(JSONObject file_info) throws JSONException {
        JSONObject file_parameters = file_info.getJSONObject("parameters");
        String package_dependency_filename = file_parameters.getString("filename");
        String secure_filename = get_secure_path(package_dependency_filename);
        file_parameters.put("secure_filename", secure_filename);
    }

    @NonNull
    private String get_secure_path(String filename) {
        String secure_filename = filename.replace(".", "_").replaceAll("[^\\w\\d_]", "");
        return this.package_config.package_name + "_" + secure_filename;
    }

    private void parse_pipeline_required_network() throws JSONException {
        /*
         * "name": "y_pipeline",
         * "input": ...,
         * "preprocess": ...,
         * "classifiers": [
         * "gyro_l2norm_encoder",
         * "gyro_l2norm_decoder"
         * ]
         */
        JSONArray classifiers_map = this.package_config.origin_json.optJSONArray("classifiers_map");
        assert classifiers_map != null;
        for (int i = 0; i < classifiers_map.length(); i++) {
            List<Classifier<TensorBuffer, TensorBuffer>> classifiers_names = new LinkedList<>();
            JSONObject pipeline = classifiers_map.optJSONObject(i);
            JSONArray classifiers = pipeline.getJSONArray("classifiers");
            String pipeline_name = pipeline.optString("name");
            Log.d(TAG, String.format("Loading pipeline %s-<%d> models", pipeline_name, classifiers.length()));
            for (int j = 0; j < classifiers.length(); j++) {
                classifiers_names.add(names_to_classifier.get(classifiers.getString(j)));
            }
            pipeline_to_classifiers.put(pipeline_name, classifiers_names);
        }
    }

    @Nullable
    public List<Classifier<TensorBuffer, TensorBuffer>> get_classifiers(String pipeline_name) {
        return pipeline_to_classifiers.getOrDefault(pipeline_name, null);
    }
}
