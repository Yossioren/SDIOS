package com.SDIOS.ServiceControl.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.SDIOS.ServiceControl.utils.ContextHolder;

public class SDIOSAnalyzerService extends Service {
    private static final String TAG = "SDIOSAnalyzerService";

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private Messenger mMessenger;

    public SDIOSAnalyzerService() {
        ContextHolder.set_context(this);
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding to service", Toast.LENGTH_SHORT).show();
        String packageName = intent.getPackage();
        if (packageName != null)
            Log.d(TAG, "onBind: " + packageName);
        else
            Log.d(TAG, "onBind: null package name " + intent);
        Log.d(TAG, "Connection from " + intent.getComponent().getPackageName()); // alternative
        mMessenger = new Messenger(new SensorEventHandler(this));
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        String packageName = intent.getPackage();
        if (packageName != null)
            Log.d(TAG, "Unbind: " + packageName);
        else
            Log.d(TAG, "Unbind: null package name " + intent);
        return false;//NO need to rebind (see below)
        //Return true if you would like to have the service's onRebind(Intent) method later called when new clients bind to it.
    }
}