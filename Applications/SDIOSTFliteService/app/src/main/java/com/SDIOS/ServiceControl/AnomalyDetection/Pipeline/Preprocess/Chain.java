package com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess;

import android.util.Log;

import java.util.List;

public class Chain<I, O> implements Preprocessor<I, O> {
    private final static String TAG = "ChainPreprocessor";
    private final List<Preprocessor<Object, Object>> preprocessorList;

    public Chain(List<Preprocessor<Object, Object>> preprocessorList) {
        this.preprocessorList = preprocessorList;
    }

    @Override
    public O process(I input) {
        Object processed_input = input;
        for (Preprocessor<Object, Object> preprocessor : preprocessorList) {
            try {
                processed_input = preprocessor.process(processed_input);
            } catch (Throwable e) {
                Log.e(TAG, "Failed on preprocessor " + preprocessor.toString() + ", with: " + e);
                return null;
            }
        }
        return (O) processed_input;
    }
}
