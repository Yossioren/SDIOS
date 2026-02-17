package com.SDIOS.ServiceControl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.SDIOS.ServiceControl.AnomalyDetection.PackageParser;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.DataCollector.SensorEventSdios;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Input.InputParser;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Pipeline;
import com.SDIOS.ServiceControl.AnomalyDetection.Pipeline.Preprocess.Preprocessor;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PipelinesTest {
    private final static long ms_to_timestamp_ns = 1000000;
    private static final String SENSOR_SERVICE_RAW = "sensor_raw";

    @Test
    public void testPipelineUsabilityApp() throws JSONException, InterruptedException {
        PackageParser packageParser = GetPackageParser();
        Pipeline pipeline = packageParser.pipelineManager.get_pipeline("x_pipeline");
        SensorManager sensorManager = getSensorManager();
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        TestPipelineEvents testPipelineEvents = new TestPipelineEvents(sensorManager, pipeline);
        sensorManager.registerListener(testPipelineEvents, mSensor, 10000);
        while (!testPipelineEvents.has_finished())
            Thread.sleep(5);
    }

    @Test
    public void testInputApp() throws JSONException, InterruptedException {
        PackageParser packageParser = GetPackageParser();
        Pipeline pipeline = packageParser.pipelineManager.get_pipeline("x_pipeline");
        SensorManager sensorManager = getSensorManager();
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        TestInputEvents testInputEvents = new TestInputEvents(sensorManager, pipeline, 2000, 400);
        sensorManager.registerListener(testInputEvents, mSensor, 10000);
        while (!testInputEvents.has_finished())
            Thread.sleep(5);
    }

    @NonNull
    private PackageParser GetPackageParser() throws JSONException {
        String example_model = "{" +
                "\"package_name\": \"GAF_anomaly_detector_lite\"," +
                "\"version\": 1.3," +
                "\"description\": \"Autoencoder trained on benign SDIOS dataset, converted to GAF\"," +
                "\"classifiers_map\": [" +
                "    {" +
                "        \"name\": \"gyroscope_x_pipeline\"," +
                "        \"input\": {" +
                "            \"sensors\": [" +
                "                \"gyroscope\"" +
                "            ]," +
                "            \"data_collection\": {" +
                "                \"method\": \"time\"," +
                "                \"parameters\": {" +
                "                    \"collect_ms\": 2000" +
                "                }" +
                "            }," +
                "            \"skip_samples\": {" +
                "                \"method\": \"time\"," +
                "                \"parameters\": {" +
                "                    \"skip_ms\": 2000" +
                "                }," +
                "                \"user_config\": [" +
                "                    {" +
                "                        \"var_name\": \"skip_ms\"," +
                "                        \"friendly_name\": \"Gyroscope X pipeline - evaluate the events every ms:\"," +
                "                        \"category\": \"pipelines\"," +
                "                        \"type\": \"bar\"," +
                "                        \"low\": 0," +
                "                        \"high\": 2000" +
                "                    }" +
                "                ]" +
                "            }" +
                "        }," +
                "        \"preprocess\": [" +
                "            {" +
                "                \"method\": \"extract_axes\"," +
                "                \"parameters\": {" +
                "                    \"axes\": [" +
                "                        \"x\"," +
                "                        \"t\"" +
                "                    ]," +
                "                    \"extract_timestamp\": true" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"fixed_time\"," +
                "                \"parameters\": {" +
                "                    \"samples_per_second\": 60" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"enforce_size\"," +
                "                \"parameters\": {" +
                "                    \"shape\": [" +
                "                        120" +
                "                    ]" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"normalize\"," +
                "                \"parameters\": {" +
                "                    \"from_start\": -30," +
                "                    \"from_end\": 30," +
                "                    \"to_start\": -1," +
                "                    \"to_end\": 1" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"gaf\"," +
                "                \"parameters\": {" +
                "                    \"method\": \"summation\"" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"normalize\"," +
                "                \"parameters\": {" +
                "                    \"from_start\": -1," +
                "                    \"from_end\": 1," +
                "                    \"to_start\": 0," +
                "                    \"to_end\": 1" +
                "                }" +
                "            }," +
                "            {" +
                "                \"method\": \"transform_to_tensorflow_buffer\"," +
                "                \"parameters\": {" +
                "                    \"type\": \"float32\"," +
                "                    \"shape\": [" +
                "                        1," +
                "                        120," +
                "                        120," +
                "                        1" +
                "                    ]" +
                "                }" +
                "            }" +
                "        ]," +
                "        \"classifiers\": [" +
                "            \"gyroscope_x_encoder\"," +
                "            \"gyroscope_x_decoder\"" +
                "        ]" +
                "    }" +
                "]," +
                "\"anomaly_detectors\": {" +
                "    \"gyroscope\": {" +
                "        \"analyzer\": {" +
                "            \"method\": \"threshold\"," +
                "            \"parameters\": {" +
                "                \"threshold_amount\": 0.04710747301578522," +
                "                \"pipelines\": [" +
                "                    \"gyroscope_x_pipeline\"," +
                "                    \"gyroscope_y_pipeline\"," +
                "                    \"gyroscope_z_pipeline\"" +
                "                ]," +
                "                \"loss\": \"MeanSquaredError\"" +
                "            }," +
                "            \"user_config\": [" +
                "                {" +
                "                    \"var_name\": \"threshold_amount\"," +
                "                    \"friendly_name\": \"Gyroscope allowed Loss threshold amount\"," +
                "                    \"category\": \"trust\"," +
                "                    \"type\": \"bar\"," +
                "                    \"low\": 0.0," +
                "                    \"high\": 0.5" +
                "                }" +
                "            ]" +
                "        }," +
                "        \"on_detect_action\": {" +
                "            \"method\": \"block\"," +
                "            \"parameters\": {" +
                "                \"trust_level\": 0.9" +
                "            }," +
                "            \"user_config\": [" +
                "                {" +
                "                    \"var_name\": \"trust_level\"," +
                "                    \"friendly_name\": \"Gyroscope allowed trust levels\"," +
                "                    \"category\": \"trust\"," +
                "                    \"type\": \"bar\"," +
                "                    \"low\": 0," +
                "                    \"high\": 1" +
                "                }" +
                "            ]" +
                "        }" +
                "    }" +
                "}," +
                "\"neural_networks\": [" +
                "    {" +
                "        \"name\": \"gyroscope_x_encoder\"," +
                "        \"method\": \"TensorFlowLiteNeuronalNetwork\"," +
                "        \"parameters\": {" +
                "            \"filename\": \"gyroscope_gyroscope_500msx4_120_120_300_encoderx.tflite\"," +
                "            \"input_shape\": [" +
                "                1," +
                "                120," +
                "                120," +
                "                1" +
                "            ]," +
                "            \"output_shape\": [" +
                "                300" +
                "            ]," +
                "            \"input_type\": \"float\"," +
                "            \"output_type\": \"float\"" +
                "        }" +
                "    }," +
                "    {" +
                "        \"name\": \"gyroscope_x_decoder\"," +
                "        \"method\": \"TensorFlowLiteNeuronalNetwork\"," +
                "        \"parameters\": {" +
                "            \"filename\": \"gyroscope_gyroscope_500msx4_120_120_300_decoderx.tflite\"," +
                "            \"input_shape\": [" +
                "                300" +
                "            ]," +
                "            \"output_shape\": [" +
                "                1," +
                "                120," +
                "                120," +
                "                1" +
                "            ]," +
                "            \"input_type\": \"float\"," +
                "            \"output_type\": \"float\"" +
                "        }" +
                "    }" +
                "]}";
        ClassifiersPackage classifiersPackage = new ClassifiersPackage(new JSONObject(example_model), false);
        return new PackageParser(classifiersPackage);
    }

    private SensorManager getSensorManager() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SensorManager sensorManager = (SensorManager) appContext.getSystemService(SENSOR_SERVICE_RAW);//deploy on my OS
        if (sensorManager == null) {
            sensorManager = (SensorManager) appContext.getSystemService(SENSOR_SERVICE);//to test on regular OS
        }
        return sensorManager;
    }


    public static class TestInputEvents implements SensorEventListener {
        private static final int fifty_ms = 50 * 1000000;
        private final InputParser inputParser;
        private final SensorManager sensorManager;
        private final int maximum_events = 1500;
        private final int maximum_pipelines_output = 5;
        private final long requested_diff;
        private final long skip_ns;
        private final String TAG = "TestInputEvents";
        private int events_count = 0;
        private int pipeline_output_count = 0;
        private long last_measure_ns = 0;

        public TestInputEvents(SensorManager sensorManager, Pipeline pipeline, int length_ms, int skip_ms) {
            inputParser = pipeline.inputParser;
            this.sensorManager = sensorManager;
            requested_diff = length_ms * ms_to_timestamp_ns;
            this.skip_ns = skip_ms * ms_to_timestamp_ns;
        }

        public boolean has_finished() {
            return pipeline_output_count >= maximum_pipelines_output;
        }

        private Object evaluate(SensorEvent sensorEvent) {
            inputParser.add(new SensorEventSdios(sensorEvent));
            List<SensorEventSdios> sensorEvents = inputParser.getCollected();
            if (sensorEvents == null) return null;
            SensorEventSdios first = sensorEvents.get(0);
            SensorEventSdios last = sensorEvents.get(sensorEvents.size() - 1);
            Log.d(TAG, "first: " + first.timestamp + ", second: " + last.timestamp + ", diff: " + (last.timestamp - first.timestamp) + ", size " + sensorEvents.size());
            assert Math.abs((last.timestamp - first.timestamp) - requested_diff) < fifty_ms;
            if (last_measure_ns > 0) {
                Log.d(TAG, "skipMS: " + (first.timestamp - last_measure_ns));
                assert Math.abs((first.timestamp - last_measure_ns) - skip_ns) < fifty_ms;// 200L * fifty_ms;
            }
            last_measure_ns = first.timestamp;
            return sensorEvents;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            events_count++;
            if (events_count > maximum_events) {
                sensorManager.unregisterListener(this);
                assert false;
            }
            if (evaluate(sensorEvent) != null) {
                Log.d(TAG, "Got input, current events_count " + events_count);
                if (++pipeline_output_count >= maximum_pipelines_output) {
                    sensorManager.unregisterListener(this);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }

    public static class TestPipelineEvents implements SensorEventListener {
        private final InputParser inputParser;
        private final Preprocessor preprocessor;
        private final SensorManager sensorManager;
        private final int maximum_events = 1500;
        private final int maximum_pipelines_output = 5;
        private final String TAG = "TestPipelineEvents";
        private int events_count = 0;
        private int pipeline_output_count = 0;

        public TestPipelineEvents(SensorManager sensorManager, Pipeline pipeline) {
            inputParser = pipeline.inputParser;
            preprocessor = pipeline.preprocessor;
            this.sensorManager = sensorManager;
        }

        private Object evaluate(SensorEvent sensorEvent) {
            inputParser.add(new SensorEventSdios(sensorEvent));
            List<SensorEventSdios> sensorEvents = inputParser.getCollected();
            if (sensorEvents == null) return null;
            return preprocessor.process(sensorEvents);
        }

        public boolean has_finished() {
            return pipeline_output_count >= maximum_pipelines_output;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            events_count++;
            if (events_count > maximum_events) {
                sensorManager.unregisterListener(this);
                assert false;
            }
            if (evaluate(sensorEvent) != null) {
                Log.d(TAG, "Got input, current events_count " + events_count);
                if (++pipeline_output_count >= maximum_pipelines_output) {
                    sensorManager.unregisterListener(this);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }
}

