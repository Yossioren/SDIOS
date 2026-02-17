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

public abstract class NDArray<T extends Number> {
    /*
     * The purpose of this class is to be an array holder of any type, similar to numpy.ndarray
     * It makes the whole processing and management more simpler in java
     * PAY Attention: this class is mutable - unlike numpy
     */
    public final int[] shape;
    protected final int elements_count;

    protected NDArray(int[] shape) {
        assert shape.length > 0;
        this.shape = shape;
        elements_count = Arrays.stream(shape).reduce((v1, v2) -> v1 * v2).getAsInt();
    }

    protected NDArray(List<Integer> shape) {
        this(shape.stream().mapToInt(Integer::intValue).toArray());
    }

    public static NDArray<Integer> toInteger(NDArray<Double> doubleNDArray) {
        return NDArrayFromBuffer.get_integer_instance(doubleNDArray);
    }

    public static NDArray<Double> toDouble(NDArray<Integer> intNDArray) {
        return NDArrayFromBuffer.get_double_instance(intNDArray);
    }

    public static void subtract(NDArray<Double> me, NDArray<Double> other) {
        me.recursive_math_operation(other, (v1, v2) -> ((Double) v1) - ((Double) v2));
    }

    public static void add(NDArray<Double> me, NDArray<Double> other) {
        me.recursive_math_operation(other, (v1, v2) -> ((Double) v1) + ((Double) v2));
    }

    public static void multiple(NDArray<Double> me, NDArray<Double> other) {
        me.recursive_math_operation(other, (v1, v2) -> ((Double) v1) * ((Double) v2));
    }

    public static void abs(NDArray<Double> doubleNDArray) {
        if (doubleNDArray.shape.length > 1)
            doubleNDArray.stream_sub_array().forEach(NDArray::abs);
        else {
            Collection<Double> tmp = doubleNDArray.stream_element().mapToDouble(Math::abs).boxed().collect(Collectors.toList());
            doubleNDArray.apply(tmp);
        }
    }

    public static double sum(NDArray<Double> doubleNDArray) {
        return doubleNDArray.values().mapToDouble(Double::doubleValue).sum();
    }

    public static boolean has_nan(NDArray<Double> doubleNDArray) {
        return doubleNDArray.values().mapToDouble(Double::doubleValue).anyMatch(Double::isNaN);
    }

    public static void scale(NDArray<Double> doubleNDArray, double factor) {
        if (doubleNDArray.shape.length > 1)
            doubleNDArray.stream_sub_array().forEach((ndArray) -> scale(ndArray, factor));
        else {
            List<Double> tmp = doubleNDArray.stream_element().mapToDouble(num -> num * factor).boxed().collect(Collectors.toList());
            doubleNDArray.apply(tmp);
        }
    }

    public abstract Object get(int... indexes);

    public T get_element(int... indexes) {
        return (T) get(indexes);
    }

    public abstract Iterator<?> iterator();

    public Iterator<T> iterator_element() {
        assert shape.length == 1;
        return (Iterator<T>) this.iterator();
    }

    public abstract Stream<?> stream();

    public Stream<T> stream_element() {
        assert shape.length == 1;
        return (Stream<T>) this.stream();
    }

    public abstract Stream<T> values();

    public Stream<NDArray<T>> stream_sub_array() {
        assert shape.length > 1;
        return (Stream<NDArray<T>>) this.stream();
    }

    protected abstract void apply(Collection<T> collection);

    private void recursive_math_operation(NDArray<Double> other, MathOperation operation) {
        assert this.get_current_layer_size() == other.get_current_layer_size();
        if (shape.length > 1) {
            Iterator it1 = iterator();
            Iterator it2 = other.iterator();
            while (it1.hasNext())
                ((NDArray<T>) it1.next()).recursive_math_operation((NDArray<Double>) it2.next(), operation);
            return;
        }
        Iterator<Double> it1 = (Iterator<Double>) iterator();
        Iterator<Double> it2 = (Iterator<Double>) other.iterator();
        List<Double> tmp = new ArrayList<>();
        while (it1.hasNext())
            tmp.add(operation.run(it1.next(), it2.next()));
        apply((Collection<T>) tmp);
    }

    @NonNull
    @Override
    public String toString() {
        return "NDArray <" + Arrays.toString(shape) + ">";
    }

    public int elements_count() {
        return elements_count;
    }

    public int get_current_layer_size() {
        return shape[0];
    }

    private boolean shape_equals_strict(NDArray<Double> other) {
        int length = this.shape.length;
        if (length != other.shape.length) return false;
        int i = 0;
        while (i < length && this.shape[i] == other.shape[i]) i++;
        return i == length;
    }

    public boolean shape_equals(NDArray<Double> other) {
        return elements_count == other.elements_count;
    }

    public boolean equals_debug(NDArray<Double> other, double epsilon) {
        int count = 0;
        Iterator<Double> me_iterator = (Iterator<Double>) values().iterator();
        for (Iterator<Double> it_other = (Iterator<Double>) other.values().iterator(); it_other.hasNext(); ) {
            Double a = me_iterator.next();
            Double b = it_other.next();
            if (Math.abs(a - b) > epsilon) {
                Log.e("NDARRAY_EQUALS_DEBUG", count + ": " + a + "," + b);
                return false;
            }
            count++;
        }
        return true;
    }

    public String print_all() {
        String output = toString();
        if (shape.length > 1) {
            int cur = 0;
            Iterator it1 = iterator();
            while (it1.hasNext()) {
                output += "\n" + cur + "/" + shape[0] + "\n";
                output += ((NDArray<T>) it1.next()).print_all();
                cur += 1;
            }
            return output;
        }
        Iterator<Double> it1 = (Iterator<Double>) iterator();
        while (it1.hasNext())
            output += it1.next() + ",";
        return output;
    }
}
