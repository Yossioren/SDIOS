package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyArrays {

    @NonNull
    public static int[] get_shape_array(JSONObject parameters, String field) throws JSONException {
        JSONArray array = parameters.getJSONArray(field);
        return get_int_array(array);
    }

    @NonNull
    private static int[] get_int_array(JSONArray array) throws JSONException {
        int[] output = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            output[i] = array.getInt(i);
        }
        return output;
    }
}
