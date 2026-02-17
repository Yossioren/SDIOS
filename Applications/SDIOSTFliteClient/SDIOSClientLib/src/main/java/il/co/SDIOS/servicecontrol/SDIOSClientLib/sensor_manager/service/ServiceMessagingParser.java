package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.service;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.OnSensorChangedSdiosService;
import il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils.RegisterSensorEventListenerCallback;

class ServiceMessagingParser {
    static final int MSG_REGISTER_SENSOR = 1, MSG_UNREGISTER_SENSOR = 2,
            MSG_ON_SENSOR_CHANGED = 3, MSG_ON_ACCURACY_CHANGED = 4,
            MSG_REGISTER_SENSOR_SDIOS = 5, MSG_UNREGISTER_SENSOR_SDIOS = 6,
            MSG_ACCEPT = 7, MSG_REFUSE = 8;
    private static final String TAG = "SDIOSLib-SerMsgHandler";
    private final List<Pair<Integer, RegisterSensorEventListenerCallback>> pendingRequests;
    private final OnSensorChangedSdiosService SensorManager;

    public ServiceMessagingParser(
            @NonNull OnSensorChangedSdiosService SensorManager,
            @NonNull List<Pair<Integer, RegisterSensorEventListenerCallback>> pendingRequests) {
        Log.d(TAG, "ServiceMessagingHandler is loaded");
        this.SensorManager = SensorManager;
        this.pendingRequests = pendingRequests;
    }

    public void handleMessage(@Nullable Message msg) {
        //Log.i(TAG, msg.toString());
        if (msg == null || msg.obj == null) {
            return;
        }

        Bundle data = (Bundle) msg.obj;
        switch (msg.what) {
            case MSG_ON_SENSOR_CHANGED:
                SensorManager.onSensorChanged(data);
                break;
            case MSG_ON_ACCURACY_CHANGED:
                SensorManager.onAccuracyChanged(data);
                break;
            case MSG_ACCEPT:
                accept(data);
                break;
            case MSG_REFUSE:
                refuse(data);
                break;
            default:
                break;
        }
    }

    private void accept(@NonNull Bundle bundle) {
        int requestID = bundle.getInt("requestID");
        for (Pair<Integer, RegisterSensorEventListenerCallback> pair : pendingRequests)
            if (pair.first == requestID) {
                pair.second.register(
                        bundle.getInt(SensorManagerSdiosHelper.SENSOR_EVENT_LISTENER_INDEX_KEY));
                pendingRequests.remove(pair);
                return;
            }
        Log.w(TAG, "Accept for requestID " + requestID + " was not found on pending request");
    }

    private void refuse(@NonNull Bundle bundle) {
        int requestID = bundle.getInt("requestID");
        Log.e(TAG, "Sensor request was refused");
        for (Pair<Integer, RegisterSensorEventListenerCallback> pair : pendingRequests)
            if (pair.first == requestID) {
                pendingRequests.remove(pair);
                pair.second.register_fallback();
                return;
            }
        Log.w(TAG, "Refuse for requestID " + requestID + " was not found on pending request");
    }
}
