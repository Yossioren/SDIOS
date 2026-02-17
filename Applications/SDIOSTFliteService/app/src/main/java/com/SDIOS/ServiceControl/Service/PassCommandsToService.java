package com.SDIOS.ServiceControl.Service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.SDIOS.ServiceControl.utils.ContextHolder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PassCommandsToService {
    public static final String PACKAGE_NAME = "com.SDIOS.ServiceControl";
    public static final String PACKAGE_CLASS_NAME = "com.SDIOS.ServiceControl.Service.SDIOSAnalyzerService";
    private final static String TAG = "PassCommandsToService";
    private static PassCommandsToService instance = null;
    private final List<Message> waitingForBind = new CopyOnWriteArrayList<>();
    /**
     * Messenger for communicating with the service.
     */
    private Messenger mService = null, mServiceReceive = null;
    private Intent serviceIntent = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean bound = false;
    private boolean connecting = false;
    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "service connected");
            mService = new Messenger(service);
            mServiceReceive = new Messenger(new ServiceHandler());
            bound = true;
            connecting = false;
            if (!waitingForBind.isEmpty()) {
                Log.i(TAG, "sending pending requests");
                for (Message req : waitingForBind) {
                    sendToService(req);
                }
                waitingForBind.clear();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "service disconnected");
            mService = null;
            mServiceReceive = null;
            bound = false;
            connecting = false;
        }

        public void onNullBinding(ComponentName className) {
            Log.i(TAG, "service connection failed " + className);
        }
    };

    private PassCommandsToService() {
        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(PACKAGE_NAME, PACKAGE_CLASS_NAME));
        this.connect();
    }

    public static PassCommandsToService getInstance() {
        Log.i(TAG, "Getting instance");
        if (instance == null)
            instance = new PassCommandsToService();
        return instance;
    }

    public void pushUpdate() {
        pushMessage(ServiceConstants.MSG_CHANGE_PACKAGE);
    }

    public void userConfigUpdate() {
        pushMessage(ServiceConstants.MSG_UPDATE_USER_CONFIGURATIONS);
    }

    public void connect() {
        if (!bound && !connecting) {
            connecting = true;
            Log.i(TAG, "binding");
            boolean out = ContextHolder.get().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.i(TAG, "binding result " + out);
        }
    }

    private void disconnect() {
        if (bound) {
            connecting = false;
            Log.i(TAG, "detaching");
            ContextHolder.get().unbindService(mConnection);
        }
    }

    private void pushMessage(int action) {
        Message obtain = Message.obtain(null, action);
        if (bound)
            sendToService(obtain);
        else
            waitingForBind.add(obtain);
    }

    private void sendToService(Message obtain) {
        obtain.replyTo = mServiceReceive;
        try {
            mService.send(obtain);
        } catch (RemoteException e) {
            Log.e(TAG, "reply exception: " + e);
        }
    }

    private static class ServiceHandler extends Handler {
        /*
        Simple handler, does not care about the response
         */
        @Override
        public void handleMessage(Message message) {
            Log.i(TAG, message.toString());
            super.handleMessage(message);
        }
    }
}
