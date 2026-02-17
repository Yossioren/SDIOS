package com.SDIOS.ServiceControl.PackageManagerClient;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.NeuronalNetwork.NeuronalNetworksManager;
import com.SDIOS.ServiceControl.Recycler.ChooseUpdate;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONException;

import java.lang.ref.WeakReference;

public class DownloadPackage extends AsyncTask<ClassifiersPackage, Void, Boolean> {
    private static final String TAG = "DownloadPackage";
    private final WeakReference<ChooseUpdate> onComplete;
    private final ModelServer modelServer;

    public DownloadPackage(ChooseUpdate onComplete, ModelServer modelServer) {
        this.modelServer = modelServer;
        this.onComplete = new WeakReference<>(onComplete);
    }

    @Override
    protected Boolean doInBackground(ClassifiersPackage... packages) {
        if (onComplete.get() == null)
            return false;
        try {
            for (ClassifiersPackage package_info : packages) {
                NeuronalNetworksManager tmp_package_classifiers = new NeuronalNetworksManager(package_info, true);
                for (Pair<String, String> dependency_install_path : tmp_package_classifiers.get_dependencies()) {
                    String filename = dependency_install_path.first;
                    String file_path = dependency_install_path.second;
                    if (!this.modelServer.get_model(onComplete.get(), package_info.package_name + "," + filename, file_path)) {
                        Log.e(TAG, "Could not get file " + filename);
                        return false;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeed) {
        if (succeed) {
            if (onComplete.get() != null)
                onComplete.get().succeedDownloadPackage();
            else
                Log.e(TAG, "WeakReference is null!");
        } else
            onComplete.get().failDownloadPackage();
    }
}