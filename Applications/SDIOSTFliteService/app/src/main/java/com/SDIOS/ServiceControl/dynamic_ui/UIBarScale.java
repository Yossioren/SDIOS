package com.SDIOS.ServiceControl.dynamic_ui;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;

import org.json.JSONException;
import org.json.JSONObject;

public class UIBarScale extends UIItem implements Slider.OnChangeListener {

    private final double fromValue;
    private final double toValue;
    private double value;

    public UIBarScale(Context context, String key, Integer parameter_index, JSONObject ui_config) throws JSONException {
        super(context, key, parameter_index, ui_config);
        fromValue = ui_config.getDouble("low");
        toValue = ui_config.getDouble("high");
        value = ui_config.getDouble("value");
    }

    @Override
    public View build() {
        Slider slider = new Slider(context);
        slider.setValueFrom((float) fromValue);
        slider.setValueTo((float) toValue);
        slider.setValue((float) value);
        slider.addOnChangeListener(this);
        return wrap_in_layout_label(slider);
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        this.value = value;
        update_user_config(this.value);
    }
}
