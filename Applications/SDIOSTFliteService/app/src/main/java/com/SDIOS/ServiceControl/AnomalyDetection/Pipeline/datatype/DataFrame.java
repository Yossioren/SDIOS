package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataFrame {
    /*
     * The purpose of this class is to manage the map and array usage of any type, similar to pandas.dataframe
     * It makes the whole processing and management more simpler in java
     */
    private final Map<String, NDArray<?>> data = new HashMap<>();

    public void put(String key, NDArray<?> new_value) {
        data.put(key, new_value);
    }

    public NDArray get(String key) {
        return data.get(key);
    }

    public Collection<String> get_keys() {
        return data.keySet();
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean is_empty() {
        return data.isEmpty();
    }
}
