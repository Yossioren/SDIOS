# Run the `main.py` script to upload models to your server
For example:
```bash
python3 main.py --action upload --package_name GAF_anomaly_detector
```
Note: change the hardcoded server address in the script.

## Authorization
To execute upload and remove actions, the sender needs to sign his request hash with a private RSA key; the hash is sha3_512.

In addition, the request includes a timestamp, which invalidates after a few minutes. These steps enforce content integrity and authenticity.
Lastly, each action on the server is logged.

## Package
Need to have a unique name.

If you upload a package with a name that exists on the server, it will override the package.

SDIOS package comprised of '.tflite' model files and a JSON configuration file containing the fields package_name, version, description, classifiers_map, anomaly_detectors, and neural_networks. The field classifiers_map defines the package's pipelines, neural_networks defines the package's TensorFlow Lite models, and anomaly_detectors defines sensor protection based on pipelines and neural models.

### Package example
In the packages directory are two examples of our actual configurations and models to upload.

GAF_anomaly_detector - contains the models ordered in directories. Due to their big size, we included a placeholder file instead. Info.json contains three main parts
1. classifiers_map - Define an entire pipeline - which sensors to use, how to preprocess data, which models to use, and more.
- data_collection - how much data we need to gather before we can start
- skip_samples - how long to wait between each pipeline evaluation
- preprocess - which actions should be taken with the data?
- classifiers - which models to run? In which order?
```json
"classifiers_map": [
    {
        "input": {
            "sensors": ["gyroscope"],
            "data_collection": {
                "method": "time",
                "parameters": {"collect_ms": 2000}
            },
            "skip_samples": {
                "method": "time",
                "parameters": {"skip_ms": 400},
                "user_config": [...]
            }
        },
        "preprocess": [...],
        "classifiers": ["gyroscope_x_encoder", "gyroscope_x_decoder"]
    },
...
]
```
Preprocess defines which actions your data most undergoes before being passed as input to the first classifier.
```json
"preprocess": [
    {
        "method": "extract_axes",
        "parameters": {"axes": ["x", "t"], "extract_timestamp": true}
    },
    {"method": "fixed_time", "parameters": {"samples_per_second": 60}},
    {"method": "enforce_size", "parameters": {"shape": [120]}},
    {
        "method": "normalize",
        "parameters": {
            "from_start": -30,
            "from_end": 30,
            "to_start": -1,
            "to_end": 1
        }
    },
    {"method": "gaf", "parameters": {"method": "summation"}},
    {
        "method": "normalize",
        "parameters": {
            "from_start": -1,
            "from_end": 1,
            "to_start": 0,
            "to_end": 1
        }
    },
    {
        "method": "transform_to_tensorflow_buffer",
        "parameters": {"type": "float32", "shape": [1, 120, 120, 1]}
    }
]
```
2. anomaly_detectors - The model supported protection sensors; for each, we need to configure an analyzer and on_detect_action.
For example:
```json
"anomaly_detectors": {
    "gyroscope": {
        "analyzer": {
            ...
        },

        "on_detect_action": {
            "method": "block",
            "parameters": {"trust_level": 0.9},
            ...
        }
    }
    ...
}
```
The 'analyzer' defines which pipelines to use and how. The 'analyzer' will trigger an anomaly if the sum of the autoencoders'
 'loss' of reconstruct vs. actual is greater than the 'threshold'. Upon detection, on_detect_action will start taking effect. 

Below, we can see 'user_config'. You can add it to each part of the JSON to make the variable modifiable in the SDIOS-Service menu.
```json
"analyzer": {
    "method": "threshold",
    "parameters": {
        "threshold_amount": 0.04710747301578522,
        "pipelines": [
            "gyroscope_x_pipeline",
            "gyroscope_y_pipeline",
            "gyroscope_z_pipeline"
        ],
        "loss": "MeanSquaredError"
    },
    "user_config": [
        {
            "var_name": "threshold_amount",
            "friendly_name": "Gyroscope allowed Loss threshold amount",
            "category": "trust",
            "type": "bar",
            "low": 0.0,
            "high": 0.5
        }
    ]
}
```
3. neural_networks - The model name and file for example
```json
"neural_networks": [
    {
        "name": "gyroscope_x_encoder",
        "method": "TensorFlowLiteNeuronalNetwork",
        "parameters": {
            "filename": "gyroscope/gyroscope_500msx4_(120, 120)_300_encoderx.h5",
            "input_shape": [1, 120, 120, 1],
            "output_shape": [300],
            "input_type": "float",
            "output_type": "float"
        }
    },
    ...
]
```

GAF_anomaly_detector_lite: The same as above, but the 'skip_samples' timeout is 2000 ms, allowing slower phones to keep up.
