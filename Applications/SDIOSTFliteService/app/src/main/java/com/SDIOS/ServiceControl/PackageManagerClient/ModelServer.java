package com.SDIOS.ServiceControl.PackageManagerClient;

import android.content.Context;
import android.util.Log;

import com.SDIOS.ServiceControl.ConfigurationManager;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ModelServer {
    private final static String TAG = "HTTPModelServer";
    private final static String model_uri = "/get_model";
    private final ConfigurationManager configurationManager;

    public ModelServer(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    List<ClassifiersPackage> get_options(String current_package) {
        String response = HTTP.send(this.configurationManager.getSDIOSServerAddress(), "GET", "");
        if (response == null || response.isEmpty())
            return null;
        List<ClassifiersPackage> packages = new ArrayList<>();
        try {
            JSONArray jsonResponse = new JSONArray(response);
            for (int i = 0; i < jsonResponse.length(); i++) {
                boolean isInstalled = current_package.equals(jsonResponse.getJSONObject(i).getString("package_name"));
                packages.add(new ClassifiersPackage(jsonResponse.getJSONObject(i), isInstalled));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
        }
        return packages;
    }

    boolean get_model(Context context, String body, String file) {
        Log.i(TAG, String.format("getting %s", file));
        return HTTP.download_file(context, this.configurationManager.getSDIOSServerAddress() + ModelServer.model_uri, "POST", body, file);
    }
}
