package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UpdateObserver {
    private static final List<WeakReference<UserConfigUpdateCallback>> callbacks = new LinkedList<>();

    public static void registerObserver(UserConfigUpdateCallback observer) {
        assert observer != null;
        callbacks.add(new WeakReference<>(observer));
    }

    public static void updateUserConfigurations() {
        Iterator<WeakReference<UserConfigUpdateCallback>> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            UserConfigUpdateCallback callback = iterator.next().get();
            if (callback == null) iterator.remove();
            else callback.updateUserConfigurations();
        }
    }
}
