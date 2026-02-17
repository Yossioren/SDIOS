package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class UserConfigUpdateCallback {
    private final static String TAG = "PreprocessManager";
    private final UserConfigManager userConfigManager;

    public UserConfigUpdateCallback(UserConfigManager userConfigManager) {
        this.userConfigManager = userConfigManager;
        UpdateObserver.registerObserver(this);
    }

    public abstract void load_parameters(JSONObject parameters) throws JSONException;

    public final void updateUserConfigurations() {
        userConfigManager.reload_configurations();
        try {
            load_parameters(userConfigManager.parameters);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot reload parameters! " + userConfigManager.parameters.toString());
        }
    }
}
