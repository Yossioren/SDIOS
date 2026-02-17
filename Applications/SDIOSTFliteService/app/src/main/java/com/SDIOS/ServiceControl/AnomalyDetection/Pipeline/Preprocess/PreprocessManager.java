package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import android.util.Log;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PreprocessManager {
    private final static String TAG = "PreprocessManager";
    private final static Map<String, Class<? extends Preprocessor<?, ?>>> names_to_preprocessors_class = new HashMap<String, Class<? extends Preprocessor<?, ?>>>() {{
        put("enforce_size", EnforceSize.class);
        put("extract_axes", ExtractAxes.class);
        put("fixed_time", FixedInput.class);
        put("gaf", GAF.class);
        put("l2norm", L2norm.class);
        put("normalize", Normalizer.class);
        put("transform_to_tensorflow_buffer", InitializeTensorFlowBuffer.class);
    }};

    private final Map<String, Preprocessor<?, ?>> names_to_preprocessors = new HashMap<>();
    private final ClassifiersPackage package_config;

    public PreprocessManager(ClassifiersPackage package_config) {
        this.package_config = package_config;
        parse_preprocess_map();
    }

    public Preprocessor get_preprocessor(String pipeline_name) {
        return names_to_preprocessors.getOrDefault(pipeline_name, null);
    }


    private void parse_preprocess_map() {
/* Map objects built like this:
       {
        "name": "x_pipeline",
        "input": {...},
        "preprocess": [
          { "method": "fixed_time", "parameters": {"samples_per_second": 60} },
          { "method": "enforce_size", "parameters": {"sample_size": 120} },
          { "method": "normalilze", "parameters": {"max": 30, "min": -30} },
          { "method": "gaf", "parameters": {"method": "summation"} }
        ],
        "classifiers": ["gyro_x_encoder", "gyro_x_decoder"]}
 */
        JSONArray classifiers = this.package_config.origin_json.optJSONArray("classifiers_map");
        assert classifiers != null;
        for (int i = 0; i < classifiers.length(); i++) {
            JSONObject nn_config = classifiers.optJSONObject(i);
            String pipeline_name = nn_config.optString("name");
            JSONArray preprocess = nn_config.optJSONArray("preprocess");
            assert preprocess != null && !pipeline_name.isEmpty();
            List<Preprocessor> list = build_preprocessor(preprocess);
            names_to_preprocessors.put(pipeline_name, new Chain(list));
        }
    }

    @NonNull
    private List<Preprocessor> build_preprocessor(JSONArray preprocess_config) {
        List<Preprocessor> list = new LinkedList<>();
        for (int i = 0; i < preprocess_config.length(); i++) {
            String method = "";
            try {
                JSONObject preprocess = preprocess_config.getJSONObject(i);
                UserConfigManager userConfigManager = new UserConfigManager(preprocess, "classifiers_map_preprocess");
                method = userConfigManager.method;
                assert names_to_preprocessors_class.containsKey(method);
                list.add(names_to_preprocessors_class.get(method).getDeclaredConstructor(UserConfigManager.class).newInstance(userConfigManager));

            } catch (JSONException e) {
                Log.e(TAG, "JSONException " + e);
            } catch (InvocationTargetException e) {
                Log.e(TAG, "InvocationTargetException " + e + ", " + e.getTargetException());
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                Log.e(TAG, "Reflection error " + method + ": " + e);
            }
        }
        return list;
    }
}
