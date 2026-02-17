package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import android.util.Log;

import com.SDIOS.ServiceControl.ConfigurationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserConfigManager {
    private final static String TAG = "UserConfigManager";
    private final static Map<String, JSONArray> default_user_configs = new HashMap<>();
    public final String method;
    private final String configuration_id;
    public JSONObject parameters;

    public UserConfigManager(JSONObject input, String... method_identifiers) throws JSONException {
        assert input != null;
        assert method_identifiers != null;
        method = input.getString("method");
        configuration_id = get_key(method_identifiers, method);
        parameters = input.optJSONObject("parameters");
        parameters = parameters == null ? new JSONObject() : parameters;
        add_user_config(input);
        reload_configurations();
    }

    public UserConfigManager(String identifier) throws JSONException {
        assert identifier != null;
        method = "";
        parameters = new JSONObject();
        configuration_id = identifier;
        reload_configurations();
    }

    public static void add_user_config(String parameters_json, String user_config_array, String identifier) throws JSONException {
        Log.d(TAG, "Setting " + identifier);
        new UserConfigManager(new JSONObject(String.format("{'method': '', 'parameters': %s, 'user_config': %s}", parameters_json, user_config_array)), identifier);
    }

    public static void clear_default() {
        default_user_configs.clear();
    }

    public static JSONObject extract_default() throws JSONException {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, JSONArray> config : default_user_configs.entrySet()) {
            json.put(config.getKey(), config.getValue());
        }
        return json;
    }

    private void add_user_config(JSONObject input) throws JSONException {
        JSONArray user_configs = input.optJSONArray("user_config");

        if (user_configs != null) {
            for (int i = 0; i < user_configs.length(); i++) {
                JSONObject user_configuration = user_configs.getJSONObject(i);
                String var_name = user_configuration.getString("var_name");
                assert user_configuration.has("friendly_name");
                assert parameters.has(var_name);
                user_configuration.put("value", parameters.get(var_name));
            }
            default_user_configs.put(configuration_id, user_configs);
        }
    }

    private String get_key(String[] method_identifiers, String method) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String s : method_identifiers) {
            stringBuilder.append(s);
            stringBuilder.append("_");
        }
        stringBuilder.append(method);
        return stringBuilder.toString();
    }

    public void reload_configurations() {
        if (configuration_id == null) return;
        try {
            JSONArray user_configs = ConfigurationManager.getInstance().get_user_config(configuration_id);
            for (int i = 0; i < user_configs.length(); i++) {
                JSONObject user_configuration = user_configs.getJSONObject(i);
                String var_name = user_configuration.getString("var_name");
                Object value = user_configuration.get("value");
                parameters.put(var_name, value);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
    }
}
