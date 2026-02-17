package com.SDIOS.ServiceControl;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayFromBuffer;

import org.junit.Test;

import java.util.stream.Stream;

import androidx.annotation.NonNull;

public class NDArrayAsBufferTest {
    static void assert_integer_3d_iterator(Stream<Integer> stream, int start_from, int increment) {
        final int[] m = {start_from};
        stream.forEach(num -> {
            assert num == m[0];
            m[0] += increment;
        });
    }

    static void assert_double_3d_iterator(Stream<Double> stream, double start_from, double increment) {
        final double[] m = {start_from};
        stream.forEach(num -> {
            assert num - m[0] < 0.00001;
            m[0] += increment;
        });
    }

    static void assert_integer_3d_sub_streams(NDArray<Integer> ndArray, int start_from, int increment) {
        if (ndArray.shape.length > 1) {
            final int[] m = {start_from};
            ndArray.stream_sub_array().forEach(x -> {
                int layer_size = ndArray.elements_count() / ndArray.get_current_layer_size();
                assert_integer_3d_sub_streams(x, m[0], increment);
                m[0] += layer_size * increment;
            });
        } else
            assert_integer_3d_iterator(ndArray.stream_element(), start_from, increment);
    }

    static void assert_integer_3d_array(NDArray<Integer> ndArray, int increment) {
        int m = 0;
        assert ndArray.shape.length == 3;
        for (int i = 0; i < ndArray.shape[0]; i++)
            for (int j = 0; j < ndArray.shape[1]; j++)
                for (int k = 0; k < ndArray.shape[2]; k++) {
                    assert m == ndArray.get_element(i, j, k);
                    m += increment;
                }
    }

    static void assert_double_3d_array(NDArray<Double> ndArray, double increment) {
        double m = 0;
        assert ndArray.shape.length == 3;
        for (int i = 0; i < ndArray.shape[0]; i++)
            for (int j = 0; j < ndArray.shape[1]; j++)
                for (int k = 0; k < ndArray.shape[2]; k++) {
                    assert Math.abs(m - ndArray.get_element(i, j, k)) < 0.00001;
                    m += increment;
                }
    }

    static void assert_double_3d_array(NDArray<Double> ndArray, Double[][][] array) {
        assert ndArray.shape.length == 3;
        for (int i = 0; i < ndArray.shape[0]; i++)
            for (int j = 0; j < ndArray.shape[1]; j++)
                for (int k = 0; k < ndArray.shape[2]; k++)
                    assert array[i][j][k] - ndArray.get_element(i, j, k) < 0.00001;
    }

    @Test
    public void validateValues() {
        int length = 5;
        int[] arr = get_integer_3d_array(length, 1);
        NDArray<Integer> ndArray = getNdArray(length, arr);
        assert_integer_3d_iterator(ndArray.values(), 0, 1);
        assert_double_3d_iterator(NDArray.toDouble(ndArray).values(), 0, 1);
        assert_integer_3d_iterator(NDArray.toInteger(NDArray.toDouble(ndArray)).values(), 0, 1);
    }

    @Test
    public void testArrayShape() {
        int length = 5;
        int[] arr = get_integer_3d_array(length, 1);
        NDArray<Integer> ndArray = getNdArray(length, arr);
        assert ndArray.get_current_layer_size() == length;
        assert ndArray.elements_count() == 125;
        assert_integer_3d_array(ndArray, 1);
        assert_integer_3d_sub_streams(ndArray, 0, 1);
    }

    @NonNull
    private NDArray<Integer> getNdArray(int length, int[] arr) {
        return NDArrayFromBuffer.get_instance(arr, length, length, length);
    }

    @NonNull
    private NDArray<Double> getDoubleNdArray(int length, int[] arr) {
        return NDArray.toDouble(NDArrayFromBuffer.get_instance(arr, length, length, length));
    }

    private int[] get_integer_3d_array(int length, int increment) {
        int m = 0;
        int[] arr = new int[length * length * length];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < length; j++)
                for (int k = 0; k < length; k++) {
                    int index = length * length * i + length * j + k;
                    arr[index] = m;
                    m += increment;
                }
        return arr;
    }

    @Test
    public void testNDArrayMath() {
        int length = 5;
        NDArray<Integer> ndArray = getNdArray(length, get_integer_3d_array(length, -1));
        assert_integer_3d_array(ndArray, -1);
        NDArray<Double> ndArray_d = NDArray.toDouble(ndArray);
        NDArray.abs(ndArray_d);
        assert_double_3d_array(ndArray_d, 1);
        NDArray<Double> ndArray2 = getDoubleNdArray(length, get_integer_3d_array(length, 1));
        NDArray.subtract(ndArray_d, ndArray2);
        assert 0 == NDArray.sum(ndArray_d);

        NDArray.scale(ndArray2, 2);
        assert_double_3d_array(ndArray2, 2);
        NDArray<Double> ndArray3 = getDoubleNdArray(2, get_integer_3d_array(2, 1));
        NDArray<Double> ndArray4 = getDoubleNdArray(2, get_integer_3d_array(2, 1));
        NDArray.multiple(ndArray3, ndArray4);
        assert_double_3d_array(ndArray3, new Double[][][]{{{0., 1.}, {4., 9.}}, {{16., 25.}, {36., 49.}}});

        NDArray.add(ndArray4, ndArray4);
        assert_double_3d_array(ndArray4, 2);
    }
}
