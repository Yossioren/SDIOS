package com.SDIOS.ServiceControl.PackageManagerClient;

import android.os.AsyncTask;
import android.util.Log;

import com.SDIOS.ServiceControl.MainActivity;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import java.lang.ref.WeakReference;
import java.util.List;

public class GetOptions extends AsyncTask<String, Void, List<ClassifiersPackage>> {

    private final String TAG = "GetOptions";
    private final WeakReference<MainActivity> onComplete;
    private final String packageName;
    private final ModelServer modelServer;

    public GetOptions(MainActivity onComplete, ModelServer modelServer, String package_name) {
        this.modelServer = modelServer;
        this.onComplete = new WeakReference<>(onComplete);
        this.packageName = package_name;
    }

    @Override
    protected List<ClassifiersPackage> doInBackground(String... urls) {
        Log.i(TAG, "get options");
        return this.modelServer.get_options(packageName);
    }

    @Override
    protected void onPostExecute(List<ClassifiersPackage> feed) {
        Log.i(TAG, "got options!");
        if (onComplete.get() != null)
            onComplete.get().openRollMenu(feed);
        else
            Log.e(TAG, "WeakReference is null!");
    }
}