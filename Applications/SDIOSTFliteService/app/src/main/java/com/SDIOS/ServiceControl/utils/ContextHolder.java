package com.SDIOS.ServiceControl.utils;

import android.content.Context;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class ContextHolder {
    private static WeakReference<Context> context_ref = new WeakReference<>(null);

    public static void set_context(@NonNull Context context) {
        context_ref = new WeakReference<>(context);
    }

    public static Context get() {
        return context_ref.get();
    }
}
