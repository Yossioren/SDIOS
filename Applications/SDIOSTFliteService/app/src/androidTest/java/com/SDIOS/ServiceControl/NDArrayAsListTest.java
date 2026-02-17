package com.SDIOS.ServiceControl;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.NonNull;

public class NDArrayAsListTest {
    @Test
    public void testSimpleArray() {
        int[] array1 = new int[]{1, 2, 3};
        compare_iterables(Arrays.stream(array1).iterator(), new NDArrayAsList<>(array1, 3).iterator());
        double[] array2 = new double[]{1.1, 2.2, 3.3};
        compare_double_iterables(Arrays.stream(array2).iterator(), new NDArrayAsList<Double>(array2, 3).iterator_element());
        float[] array3 = new float[]{1.1f, 2.2f, 3.3f};
        Iterator<Float> float_iterator = new Iterator<Float>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < array3.length;
            }

            @Override
            public Float next() {
                return array3[i++];
            }
        };
        compare_float_iterables(float_iterator, new NDArrayAsList<Float>(array3, 3).iterator_element());
        Integer[] array4 = new Integer[]{1, 2, 3};
        compare_iterables(Arrays.stream(array4).iterator(), new NDArrayAsList<>(array4, 3).iterator());
        Double[] array5 = new Double[]{1.1, 2.2, 3.3};
        compare_double_iterables(Arrays.stream(array5).iterator(), new NDArrayAsList<Double>(array5, 3).iterator_element());
        Float[] array6 = new Float[]{1.1f, 2.2f, 3.3f};
        compare_float_iterables(Arrays.stream(array6).iterator(), new NDArrayAsList<Float>(array6, 3).iterator_element());
        List<Integer> list1 = Arrays.stream(array4).collect(Collectors.toList());
        compare_iterables(list1.iterator(), new NDArrayAsList<>(list1, 3).iterator());
        List<Double> list2 = Arrays.stream(array5).collect(Collectors.toList());
        compare_double_iterables(list2.iterator(), new NDArrayAsList<Double>(list2, 3).iterator_element());
        List<Float> list3 = Arrays.stream(array6).collect(Collectors.toList());
        compare_float_iterables(list3.iterator(), new NDArrayAsList<Float>(list3, 3).iterator_element());
    }

    private void compare_iterables(Iterator<?> iterator1, Iterator<?> iterator2) {
        while (iterator1.hasNext() && iterator2.hasNext())
            assert iterator1.next() == iterator2.next();
        assert !iterator1.hasNext() && !iterator2.hasNext();
    }

    private void compare_float_iterables(Iterator<Float> iterator1, Iterator<Float> iterator2) {
        while (iterator1.hasNext() && iterator2.hasNext()) {
            assert Math.abs(iterator1.next() - iterator2.next()) < 0.0001f;
        }
        assert !iterator1.hasNext() && !iterator2.hasNext();
    }

    private void compare_double_iterables(Iterator<Double> iterator1, Iterator<Double> iterator2) {
        while (iterator1.hasNext() && iterator2.hasNext())
            assert Math.abs(((double) iterator1.next()) - ((double) iterator2.next())) < 0.00001;
        assert !iterator1.hasNext() && !iterator2.hasNext();
    }

    @Test
    public void testArrayShape() {
        int length = 5;
        int[][][] arr = get_integer_3d_array(length, 1);
        NDArrayAsList<Integer> ndArray = new NDArrayAsList<>(arr, length, length, length);
        assert ndArray.get_current_layer_size() == 5;
        assert ndArray.elements_count() == 125;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    assert arr[i][j][k] == ndArray.get_element(i, j, k);
                }
            }
        }
    }

    private int[][][] get_integer_3d_array(int length, int increment) {
        int m = 0;
        int[][][] arr = new int[length][length][length];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < length; j++)
                for (int k = 0; k < length; k++) {
                    arr[i][j][k] = m;
                    m += increment;
                }
        return arr;
    }

    private double[][][] get_double_3d_array(int length, int increment) {
        int m = 0;
        double[][][] arr = new double[length][length][length];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < length; j++)
                for (int k = 0; k < length; k++) {
                    arr[i][j][k] = m;
                    m += increment;
                }
        return arr;
    }

    @Test
    public void validateValues() {
        int length = 5;
        int[][][] arr = get_integer_3d_array(length, 1);
        NDArrayAsList<Integer> ndArray = new NDArrayAsList<>(arr, length, length, length);
        assert_integer_3d_iterator(ndArray.values(), 1);
    }

    private void assert_integer_3d_iterator(Stream<Integer> stream, int i) {
        final int[] m = {0};
        stream.forEach(num -> {
            assert num == m[0];
            m[0] += i;
        });
    }

    @NonNull
    private NDArray<Double> getDoubleNdArray(double[][][] arr, int length) {
        return new NDArrayAsList<>(arr, length, length, length);
    }

    @Test
    public void testNDArrayMath() {
        int length = 5;
        NDArray<Double> ndArray = getDoubleNdArray(get_double_3d_array(length, -1), length);
        NDArrayAsBufferTest.assert_double_3d_array(ndArray, -1);
        NDArray.abs(ndArray);
        NDArrayAsBufferTest.assert_double_3d_array(ndArray, 1);
        NDArray<Double> ndArray2 = getDoubleNdArray(get_double_3d_array(length, 1), length);
        NDArray.subtract(ndArray, ndArray2);
        assert 0 == NDArray.sum(ndArray);

        NDArray.scale(ndArray2, 2);
        NDArrayAsBufferTest.assert_double_3d_array(ndArray2, 2);

        length = 2;
        NDArray<Double> ndArray3 = getDoubleNdArray(get_double_3d_array(length, 1), length);
        NDArray<Double> ndArray4 = getDoubleNdArray(get_double_3d_array(length, 1), length);
        NDArray.multiple(ndArray3, ndArray4);
        NDArrayAsBufferTest.assert_double_3d_array(ndArray3, new Double[][][]{{{0., 1.}, {4., 9.}}, {{16., 25.}, {36., 49.}}});

        NDArray.add(ndArray4, ndArray4);
        NDArrayAsBufferTest.assert_double_3d_array(ndArray4, 2);
    }
}
