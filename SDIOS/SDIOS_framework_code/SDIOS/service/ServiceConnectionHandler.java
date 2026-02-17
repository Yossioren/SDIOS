package android.hardware.SDIOS.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import android.hardware.SDIOS.NonNull;
import android.hardware.SDIOS.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class ServiceConnectionHandler implements ServiceConnection {
    private static final String TAG = "Framework-ServConnHandler";
    private static final Queue<Message> waitingForBind = new ConcurrentLinkedQueue<>();
    private final Handler handler;
    /**
     * Messenger for communicating with the service.
     */
    private Messenger mService = null, mServiceReceive = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean bound;
    private boolean waiting_for_connection = true;

    ServiceConnectionHandler(@NonNull ServiceMessagingParser serviceMessagingParser) {
        this.handler = new Handler(Looper.getMainLooper(), message -> {
//            Log.d(TAG, "Got message " + message.what + " " + message);
            serviceMessagingParser.handleMessage(message);
            return true;
        });
    }

    public void sendToService(@NonNull Message obtain) {
        // Create and send a message to the service, using a supported 'what' value
        synchronized (this) {
            if (!bound) {
                Log.d(TAG, "adding message to waiting queue for bind");
                waitingForBind.add(obtain);
                return;
            }
        }
        obtain.replyTo = mServiceReceive;
        try {
            mService.send(obtain);
        } catch (RemoteException e) {
            Log.e(TAG, "reply exception: " + e);
        }
    }

    public void onServiceConnected(@Nullable ComponentName className, @Nullable IBinder service) {
        Log.i(TAG, "service connected");
        synchronized (this) {
            mService = new Messenger(service);
            mServiceReceive = new Messenger(this.handler);
            bound = true;
            waiting_for_connection = false;
        }
        if (!waitingForBind.isEmpty()) {
            Log.i(TAG, "Sending pending " + waitingForBind.size() + " requests");
            Message req;
            while ((req = waitingForBind.poll()) != null)
                sendToService(req);
        }
    }

    public void onServiceDisconnected(@Nullable ComponentName className) {
        Log.i(TAG, "service disconnected");
        synchronized (this) {
            mService = null;
            mServiceReceive = null;
            bound = false;
            waiting_for_connection = false;
        }
    }

    public boolean isBound() {
        return bound;
    }

    public boolean isConnecting() {
        return waiting_for_connection;
    }

    public void onNullBinding(@Nullable ComponentName className) {
        Log.i(TAG, "service connection failed " + className);
    }
}
