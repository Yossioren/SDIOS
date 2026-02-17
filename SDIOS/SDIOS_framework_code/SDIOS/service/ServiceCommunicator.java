package android.hardware.SDIOS.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import android.hardware.SDIOS.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.hardware.SDIOS.listener_utils.OnSensorChangedSdiosService;
import android.hardware.SDIOS.listener_utils.RegisterSensorEventListenerCallback;

class ServiceCommunicator {
    private static final String TAG = "Framework-ServComm";
    private static final AtomicInteger requestIndexGenerator = new AtomicInteger(0);
    private static final List<Pair<Integer, RegisterSensorEventListenerCallback>> pendingRequests = new CopyOnWriteArrayList<>();
    private final Context context;
    private final Intent serviceIntent;
    private final OnSensorChangedSdiosService SensorManager;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnectionHandler mConnection;


    ServiceCommunicator(@NonNull Context context, @NonNull OnSensorChangedSdiosService SensorManager) {
        this.context = context;
        serviceIntent = new Intent();
        this.SensorManager = SensorManager;
        serviceIntent.setComponent(new ComponentName("com.SDIOS.ServiceControl", "com.SDIOS.ServiceControl.Service.SDIOSAnalyzerService"));
    }

    void sendToService(int what, @NonNull Bundle data) {
        Message obtain = Message.obtain(null, what, data);
        try {
            mConnection.sendToService(obtain);
        } catch (Exception e) {
            Log.e(TAG, "Send exception: " + e);
            reconnect();
        }
    }

    //should handle When code request multiple gyro at the same time
    public void sendPendingRequest(int action_type, @NonNull Bundle data, @NonNull RegisterSensorEventListenerCallback callback) {
        Log.d(TAG, "sending request " + action_type);
        int requestID = requestIndexGenerator.getAndIncrement();
        data.putInt("requestID", requestID);
        pendingRequests.add(new Pair<>(requestID, callback));
        sendToService(action_type, data);
    }

    private void reconnect() {
        unbind();
        bind();
    }

    synchronized void bind() throws RuntimeException {
        Log.d(TAG, "Binding");
        if (mConnection == null)
            mConnection = new ServiceConnectionHandler(
                    new ServiceMessagingParser(SensorManager, pendingRequests));
        else if (mConnection.isConnecting() || mConnection.isBound())
            return;
        if (!context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Cannot bind analyzing service of the SDIOS-application");
            throw new RuntimeException("Cannot bind analyzing service of the SDIOS-application");
        }
    }


    synchronized void unbind() {
        Log.i(TAG, "Unbinding");
        if (mConnection == null || !mConnection.isBound()) return;
        context.unbindService(mConnection);
        mConnection = null;
    }
}
