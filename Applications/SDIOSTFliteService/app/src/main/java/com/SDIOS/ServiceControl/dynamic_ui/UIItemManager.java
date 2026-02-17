package com.SDIOS.ServiceControl.dynamic_ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class UIItemManager {
    private final static String TAG = "UIItemManager";
    private final static Map<String, Class<? extends UIItem>> types_to_ui_items = new HashMap<String, Class<? extends UIItem>>() {{
        put("radio", UIRadioButton.class);
        put("bar", UIBarScale.class);
    }};
    private final String key;

    private UIItem item;
    private TextView fallback_item;

    public UIItemManager(Context context, String key, int parameter_index, JSONObject user_config) throws JSONException {
        this.key = key;
        String type = user_config.getString("type");
        assert types_to_ui_items.containsKey(type);
        try {
            this.fallback_item = new TextView(context);
            fallback_item.setText(key);
            this.item = types_to_ui_items.get(type).
                    getDeclaredConstructor(Context.class, String.class, Integer.class, JSONObject.class).
                    newInstance(context, key, parameter_index, user_config);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException " + e + ", " + e.getTargetException());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            Log.e(TAG, "Reflection error " + type + ": " + e);
        }
    }

    public String item_key() {
        if (item != null) return item.category + item.name;
        return key;
    }

    public View build() {
        if (item != null)
            return item.build();
        if (fallback_item != null)
            return fallback_item;
        return null;
    }
}
