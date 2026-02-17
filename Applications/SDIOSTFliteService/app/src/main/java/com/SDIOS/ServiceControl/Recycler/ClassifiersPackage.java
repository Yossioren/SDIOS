package com.SDIOS.ServiceControl.Recycler;

import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassifiersPackage {
    private final static String TAG = "ClassifiersPackage";
    public String package_name = "Package parsing failed";
    public String version = "";
    public boolean installed = false;
    public String description = "";
    public JSONObject origin_json = new JSONObject();

    public ClassifiersPackage(JSONObject json_package, boolean installed) {
        this.installed = installed;
        init_parameters(json_package);
    }

    public ClassifiersPackage(JSONObject json_package) {
        init_parameters(json_package);
    }

    private void init_parameters(JSONObject json_package) {
        this.origin_json = json_package;
        this.package_name = json_package.optString("package_name");
        this.version = json_package.optString("version");
        this.description = json_package.optString("description");
    }

    public JSONObject extract_default() {
        JSONObject package_default = new JSONObject();
        try {
            package_default.put("package_name", this.package_name);
            package_default.put("version", this.version);
            setup_core_user_config();
            package_default.put("user_config", UserConfigManager.extract_default());
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
        return package_default;
    }

    private void setup_core_user_config() {
        /*
        Should always be called last so we make sure no extension override it!
        By mistake or by purpose.
         */
        try {
            UserConfigManager.add_user_config("{'enabled':'enabled'}",
                    "[{'var_name': 'enabled', 'category': 'aa_core'," +
                            "'friendly_name': 'SDIOS Protection status:'," +
                            "'type': 'radio', 'choices': ['enabled', 'disabled']}]",
                    "core");
        } catch (JSONException e) {
            Log.e(TAG, "JSONException - failed to set core utils " + e);
        }
    }
}
