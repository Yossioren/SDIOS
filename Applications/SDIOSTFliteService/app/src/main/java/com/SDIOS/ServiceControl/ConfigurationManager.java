package com.SDIOS.ServiceControl;

import android.util.Log;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;
import com.SDIOS.ServiceControl.Service.PassCommandsToService;
import com.SDIOS.ServiceControl.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigurationManager {
    private final static String config_name = "user_settings.json";
    private final static String default_config_name_format = "default_config_%s.json";
    private final static String global_config = "global_settings.json";
    /*
        TODO - Add smarter package management
        List of downloaded packages - smart download according to the version + we may already have the files downloaded
    */
    private final static String TAG = "ConfigurationManager";
    private static JSONObject userSettingsCache;
    private final FileUtils fileUtils;
    private final PassCommandsToService serviceManager = PassCommandsToService.getInstance();


    public ConfigurationManager(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public static ConfigurationManager getInstance() {
        return new ConfigurationManager(FileUtils.get_instance());
    }

    public String getUsedPackageName() {
        JSONObject user_settings = this.fileUtils.getJsonFile(config_name);
        try {
            return user_settings.getString("package_name");
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
        return "";
    }

    public String getSDIOSServerAddress() {
        JSONObject user_settings = this.fileUtils.getJsonFile(global_config);
        try {
            return user_settings.getString("SDIOS_server");
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
        return "";
    }

    public ClassifiersPackage getCurrentPackage() {
        return new ClassifiersPackage(getConfigurations(getUsedPackageName()));
    }

    @NonNull
    public JSONObject getUserSettings() {
        if (userSettingsCache == null)
            userSettingsCache = this.fileUtils.getJsonFile(config_name);
        return userSettingsCache;
    }

    private void flushSettings(JSONObject settings) {
        Log.d(TAG, "set settings configurations: " + settings);
        this.fileUtils.writeToFile(settings.toString(), config_name);
        userSettingsCache = null;
        serviceManager.userConfigUpdate();
    }

    public JSONArray get_user_config(String configuration_id) throws JSONException {
        JSONObject userSettings = getUserSettings();
        JSONObject user_config = userSettings.getJSONObject("user_config");
        return user_config.getJSONArray(configuration_id);
    }

    public void set_user_config(JSONObject ui_config, String configuration_id, int parameter_index) throws JSONException {
        Log.d(TAG, "Update " + configuration_id + ":" + parameter_index);
        JSONObject userSettings = getUserSettings(); // write is not optimized - should use flush method
        userSettings.getJSONObject("user_config").getJSONArray(configuration_id).put(parameter_index, ui_config);
        flushSettings(userSettings);
    }

    public void addConfigurations(JSONObject configurations) {
        try {
            String package_name = configurations.getString("package_name");
            Log.d(TAG, "set configurations: " + configurations);
            this.fileUtils.writeToFile(configurations.toString(), package_name + "_info.json");
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
    }

    public JSONObject getConfigurations(String package_name) {
        return this.fileUtils.getJsonFile(package_name + "_info.json");
    }

    public void setSDIOSServerAddress(String sdios_server) {
        JSONObject user_settings = this.fileUtils.getJsonFile(global_config);
        try {
            user_settings.put("SDIOS_server", sdios_server);
            this.fileUtils.writeToFile(user_settings.toString(), global_config);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
    }

    public void setDefaultConfiguration() {
        String package_name = getUsedPackageName();
        setDefaultConfiguration(package_name);
    }

    public void setDefaultConfiguration(String package_name) {
        Log.d(TAG, "Setting default configuration " + package_name);
        this.fileUtils.copyFile(String.format(default_config_name_format, package_name), config_name);
        userSettingsCache = null;
    }

    public void setPackageDefaultUserSettings(JSONObject extract_default) {
        assert extract_default.has("package_name");
        String package_name = extract_default.optString("package_name");
        this.fileUtils.writeToFile(extract_default.toString(), String.format(default_config_name_format, package_name));
    }
}
