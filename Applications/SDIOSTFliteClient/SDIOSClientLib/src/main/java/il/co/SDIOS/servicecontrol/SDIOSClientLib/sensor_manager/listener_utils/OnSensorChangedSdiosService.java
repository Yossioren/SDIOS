package il.co.SDIOS.servicecontrol.SDIOSClientLib.sensor_manager.listener_utils;

import android.os.Bundle;

import androidx.annotation.NonNull;

public interface OnSensorChangedSdiosService {
    void onSensorChanged(@NonNull Bundle bundle);

    void onAccuracyChanged(@NonNull Bundle bundle);
}
