package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import androidx.annotation.NonNull;

public class NDArrayFromBuffer<T extends Number> extends NDArray<T> {
    /*
     * The purpose of this class is to be an array holder of any type, similar to numpy.ndarray
     * It makes the whole processing and management more simpler in java
     */
    private final static String TAG = "NDArrayFromBuffer";
    private T[] data;
    private int offset = 0;

    public NDArrayFromBuffer(T[] data, int... shape) {
        super(shape);
        setup_data(data);
    }

    public NDArrayFromBuffer(int offset, T[] data, int... shape) {
        super(shape);
        this.offset = offset;
        setup_data(data);
    }

    public NDArrayFromBuffer(T[] data, List<Integer> shape) {
        super(shape);
        setup_data(data);
    }

    public static NDArray<Double> get_instance(double[] doubles, int... shape) {
        Double[] array = new Double[doubles.length];
        for (int i = 0; i < array.length; i++)
            array[i] = doubles[i];
        return new NDArrayFromBuffer<>(array, shape);
    }

    public static NDArray<Integer> get_instance(int[] ints, int... shape) {
        Integer[] array = new Integer[ints.length];
        for (int i = 0; i < array.length; i++)
            array[i] = ints[i];
        return new NDArrayFromBuffer<>(array, shape);
    }

    public static NDArray<Long> get_instance(long[] longs, int... shape) {
        Long[] array = new Long[longs.length];
        for (int i = 0; i < array.length; i++)
            array[i] = longs[i];
        return new NDArrayFromBuffer<>(array, shape);
    }

    public static NDArray<Double> get_instance(float[] floats, int... shape) {
        Double[] array = new Double[floats.length];
        for (int i = 0; i < array.length; i++)
            array[i] = (double) floats[i];
        return new NDArrayFromBuffer<>(array, shape);
    }

    protected static NDArray<Integer> get_integer_instance(NDArray<Double> doubleNDArray) {
        Integer[] ints = (Integer[]) doubleNDArray.values().mapToInt(Double::intValue).boxed().toArray(Integer[]::new);
        return new NDArrayFromBuffer<>(ints, doubleNDArray.shape);
    }

    protected static NDArray<Double> get_double_instance(NDArray<Integer> integerNDArray) {
        Double[] doubles = (Double[]) integerNDArray.values().mapToDouble(Integer::doubleValue).boxed().toArray(Double[]::new);
        return new NDArrayFromBuffer<>(doubles, integerNDArray.shape);
    }

    private void setup_data(T[] data) {
        this.data = data;
        assert data != null;
        assert this.data.length >= offset + elements_count;
    }

    public Object get(int... indexes) {
        assert shape.length >= indexes.length;
        int i = indexes.length - 1;
        int layer_size = 1;
        int index = 0;
        do {
            index += indexes[i] * layer_size;
            layer_size *= shape[i--];
        } while (i >= 0);
        assert index >= 0;
        assert index < elements_count;
        assert offset + index < data.length;
        return data[offset + index];
    }

    @Override
    public Stream<NDArray<T>> stream_sub_array() {
        assert shape.length > 1;
        int[] new_shape = Arrays.copyOfRange(shape, 1, shape.length);
        int offset = elements_count / get_current_layer_size();
        return IntStream.range(0, get_current_layer_size()).mapToObj(n -> new NDArrayFromBuffer<>(this.offset + offset * n, data, new_shape));
    }

    public Iterator<?> iterator() {
        if (shape.length > 1)
            return stream_sub_array().iterator();
        return new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements_count;
            }

            @Override
            public T next() {
                return data[offset + i++];
            }
        };
    }

    public Stream<T> stream() {
        return Arrays.stream(data).skip(offset).limit(elements_count);
    }

    public Stream<T> values() {
        return this.stream();
    }

    @NonNull
    @Override
    public String toString() {
        return "NDArray <" + Arrays.toString(shape) + ">";
    }

    protected void apply(Collection<T> collection) {
        assert shape.length == 1 && elements_count == collection.size();
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < elements_count; i++)
            data[offset + i] = iterator.next();
    }
}
