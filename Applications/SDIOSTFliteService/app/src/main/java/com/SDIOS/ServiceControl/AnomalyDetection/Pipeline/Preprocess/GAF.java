package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import androidx.annotation.NonNull;

import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.DataFrame;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArray;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.datatype.NDArrayAsList;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;

import org.json.JSONObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class GAF implements Preprocessor<DataFrame, DataFrame> {
    private final String method;

    public GAF(UserConfigManager userConfigManager) {
        JSONObject parameters = userConfigManager.parameters;
        this.method = parameters.optString("method", "summation");
    }

    @Override
    public DataFrame process(DataFrame input) {
        assert !input.is_empty();
        for (String key : input.get_keys()) {
            input.put(key, gaf(input.get(key)));
        }
        return input;
    }

    private NDArray<Double> gaf(NDArray<Double> array) {
        /*
        @njit()
        def _gasf(X_cos, X_sin, n_samples, image_size):
            X_gasf = np.empty((n_samples, image_size, image_size))
            for i in prange(n_samples):
                X_gasf[i] = np.outer(X_cos[i], X_cos[i]) - np.outer(X_sin[i], X_sin[i])
            return X_gasf

        @njit()
        def _gadf(X_cos, X_sin, n_samples, image_size):
            X_gadf = np.empty((n_samples, image_size, image_size))
            for i in prange(n_samples):
                X_gadf[i] = np.outer(X_sin[i], X_cos[i]) - np.outer(X_cos[i], X_sin[i])
            return X_gadf
        */
        List<Double> MinMaxScaled = getMinMaxScaled(array, -1, 1);
        double[] X_cos = MinMaxScaled.stream().mapToDouble(Double::doubleValue).toArray();
        double[] X_sin = MinMaxScaled.stream().mapToDouble(num -> Math.sqrt(1 - num * num)).toArray();
        int list_size = array.get_current_layer_size();
        if (method.equals("summation")) {// cos(q1 + q2)
            return new NDArrayAsList<>(subtract(outer(X_cos, X_cos), outer(X_sin, X_sin)), list_size, list_size);
        } else if (method.equals("difference")) { // sin(q1 - q2)
            return new NDArrayAsList<>(subtract(outer(X_sin, X_cos), outer(X_cos, X_sin)), list_size, list_size);
        }
        throw new AssertionError(String.format("Method %s is not supported", method));
    }

    @NonNull
    private List<Double> getMinMaxScaled(NDArray<Double> array, double dest_min, double dest_max) {
        // implemented according to from sklearn MinMaxScaler
        // Reason - my data generated with pyts which use Sklearn.MinMaxScaler
        double data_min = array.stream_element().mapToDouble(Double::doubleValue).min().orElseThrow(NoSuchElementException::new);
        double data_max = array.stream_element().mapToDouble(Double::doubleValue).max().orElseThrow(NoSuchElementException::new);
        double data_range = data_max - data_min;
        if (data_range == 0) data_range = 1;
        double scale = (dest_max - dest_min) / data_range;
        double calc_min = dest_min - data_min * scale;
        return array.stream_element().mapToDouble(num -> num * scale + calc_min).map(num -> Math.min(num, dest_max)).map(num -> Math.max(num, dest_min)).boxed().collect(Collectors.toList());
    }

    private Double[][] subtract(Double[][] array1, Double[][] array2) {
        //Assume same dimension
        for (int i = 0; i < array1.length; i++)
            for (int j = 0; j < array1[i].length; j++)
                array1[i][j] -= array2[i][j];
        return array1;
    }

    private Double[][] outer(double[] array1, double[] array2) {
        /*
        Docstring: numpy.outer(a, b, out=None)
        Compute the outer product of two vectors.
        Given two vectors, ``a = [a0, a1, ..., aM]`` and ``b = [b0, b1, ..., bN]``,
        the outer product [1]_ is::
          [[a0*b0  a0*b1 ... a0*bN ]
           [a1*b0    .
           [ ...          .
           [aM*b0            aM*bN ]]
        */
        Double[][] output = new Double[array1.length][array2.length];
        for (int i = 0; i < array1.length; i++)
            for (int j = 0; j < array2.length; j++)
                output[i][j] = array1[i] * array2[j];
        return output;
    }
}
