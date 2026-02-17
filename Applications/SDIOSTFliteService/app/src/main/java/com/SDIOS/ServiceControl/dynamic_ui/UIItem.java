package com.SDIOS.ServiceControl.dynamic_ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.SDIOS.ServiceControl.ConfigurationManager;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class UIItem {
    private final static String TAG = "UIItem";
    protected final Context context;
    final String name;
    final String category;
    private final String key;
    private final int parameter_index;
    private final JSONObject ui_config;
    private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    protected UIItem(Context context, String key, Integer parameter_index, JSONObject ui_config) throws JSONException {
        this.context = context;
        this.key = key;
        this.parameter_index = parameter_index;
        this.ui_config = ui_config;
        this.name = ui_config.getString("friendly_name");
        this.category = ui_config.optString("category", "_");
    }

    protected View build_label() {
        TextView textView = new TextView(context, null);
        textView.setText(name);
        return textView;
    }

    protected View wrap_in_layout_label(View view) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(build_label());
        linearLayout.addView(view);
        return linearLayout;
    }

    public abstract View build();

    protected void update_user_config(String value) {
        try {
            ui_config.put("value", value);
            this.update_user_config();
        } catch (JSONException e) {
            Log.e(TAG, "Error setting user config " + e);
        }
    }

    protected void update_user_config(double value) {
        try {
            ui_config.put("value", value);
            this.update_user_config();
        } catch (JSONException e) {
            Log.e(TAG, "Error setting user config " + e);
        }
    }

    private void update_user_config() throws JSONException {
        configurationManager.set_user_config(ui_config, key, parameter_index);
    }
}
