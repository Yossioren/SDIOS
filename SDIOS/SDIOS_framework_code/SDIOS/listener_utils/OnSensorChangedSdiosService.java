package android.hardware.SDIOS.listener_utils;

import android.os.Bundle;

import android.hardware.SDIOS.NonNull;

public interface OnSensorChangedSdiosService {
    void onSensorChanged(@NonNull Bundle bundle);

    void onAccuracyChanged(@NonNull Bundle bundle);
}
