package com.SDIOS.ServiceControl.dynamic_ui;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class UIRadioButton extends UIItem implements RadioGroup.OnCheckedChangeListener {
    private final List<String> choices = new LinkedList<>();
    private String value;

    public UIRadioButton(Context context, String key, Integer parameter_index, JSONObject ui_config) throws JSONException {
        super(context, key, parameter_index, ui_config);
        value = ui_config.getString("value");
        JSONArray choices = ui_config.getJSONArray("choices");
        for (int i = 0; i < choices.length(); i++)
            this.choices.add(choices.getString(i));
    }

    @Override
    public View build() {
        RadioGroup radioGroup = new RadioGroup(context);
        int i = 0;
        for (String choice : choices) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(choice);
            radioButton.setChecked(value.equals(choice));
            radioButton.setId(i);
            radioGroup.addView(radioButton, i++);
        }
        radioGroup.setOnCheckedChangeListener(this);
        return wrap_in_layout_label(radioGroup);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        value = choices.get(checkedId);
        update_user_config(value);
    }
}
