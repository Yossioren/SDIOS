package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NDArrayAsList<T extends Number> extends NDArray<T> {
    /*
     * The purpose of this class is to be an array holder of any type, similar to numpy.ndarray
     * It makes the whole processing and management more simpler in java
     */
    private final static String TAG = "NDArrayAsList";
    private List<?> data;

    public NDArrayAsList(Object data, int... shape) {
        super(shape);
        setup_data(data);
    }

    public NDArrayAsList(Object data, List<Integer> shape) {
        super(shape);
        setup_data(data);
    }

    public static NDArrayAsList get_1d_array(Collection<?> data) {
        return new NDArrayAsList(data, data.size());
    }

    private void setup_data(Object data) {
        if (data instanceof Collection) {
            setup_data_collection((Collection<?>) data);
        } else if (data instanceof Object[]) {
            setup_data_array((Object[]) data);
        } else if (data instanceof double[]) {
            setup_data_array(Arrays.stream((double[]) data).boxed().toArray());
        } else if (data instanceof int[]) {
            setup_data_array(Arrays.stream((int[]) data).boxed().toArray());
        } else if (data instanceof float[]) {
            float[] float_arr = (float[]) data;
            Float[] tmp = new Float[float_arr.length];
            for (int i = 0; i < float_arr.length; i++)
                tmp[i] = float_arr[i];
            setup_data_array(tmp);
        } else {
            Log.e(TAG, String.format("UnSupported data %s %s", data.toString(), data.getClass()));
            throw new AssertionError(String.format("UnSupported data %s %s", data, data.getClass()));
        }
        assert this.data.size() == get_current_layer_size();
    }

    public void setup_data_collection(Collection<?> collection) {
        if (shape.length > 1) {
            int[] new_shape = Arrays.copyOfRange(shape, 1, shape.length);
            this.data = collection.stream().map(sub_list -> new NDArrayAsList<T>(sub_list, new_shape)).collect(Collectors.toList());
        } else
            set_data(collection);
    }

    private void set_data(Collection<?> data) {
        if (data instanceof ArrayList)
            this.data = (List<?>) data;
        else
            this.data = new ArrayList<>(data);
    }

    public void setup_data_array(Object[] data) {
        if (shape.length > 1) {
            int[] new_shape = Arrays.copyOfRange(shape, 1, shape.length);
            this.data = Arrays.stream(data).map(sub_list -> new NDArrayAsList<T>(sub_list, new_shape)).collect(Collectors.toList());
        } else
            this.data = Arrays.stream(data).collect(Collectors.toList());
    }

    public Object get(int... indexes) {
        assert shape.length >= indexes.length;
        assert indexes[0] >= 0;
        assert indexes[0] < shape[0];
        Object wanted_index = data.get(indexes[0]);
        if (indexes.length == 1) {
            return wanted_index;
        }
        return ((NDArrayAsList<T>) wanted_index).get(Arrays.copyOfRange(indexes, 1, indexes.length));
    }

    public Iterator<?> iterator() {
        return data.iterator();
    }

    public Stream<?> stream() {
        return data.stream();
    }

    public Stream<T> values() {
        if (shape.length > 1) {
            return this.stream_sub_array().map(NDArray::values).collect(Collectors.toList()).stream().reduce(Stream::concat).get();
        }
        return this.stream_element();
    }

    @NonNull
    @Override
    public String toString() {
        return "NDArray <" + Arrays.toString(shape) + ">";
    }

    protected void apply(Collection<T> collection) {
        assert shape.length == 1 && shape[0] == collection.size();
        set_data(collection);
    }
}
