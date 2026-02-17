# Dynamic Interfaces Listing

Dynamic interfaces are classes that can be set by packaging and configured by the user in the SDIOS-Service's main menu.

This table includes a representation of every dynamic class that exists in the SDIOS Service application.

| Interface    | Name | Class | Parameters | Explanation |
|:------------ | :--- | :---- | :--------- | :---------- |
| SearchStart  | remove_oldest or default | RemoveOldest | None | Process the collected events without the oldest SensorEvent |
| SearchStart  | time                     | SearchTimestamps | collect_ms | Process the collected SensorEvent, from the newest to the closest SensorEvent, where the timestamp difference between it and the newest is the required milliseconds \\\specialrule{.1em}{.05em}{.05em}
| SkipScan     | scan_all or default      | ScanAll | None | Invoke evaluation on every SensorEvent |
| SkipScan     | time                     | SkipByTimestamp | skip_ms | Invoke evaluation when the SensorEvent timestamp surpasses the last by the required milliseconds \\\specialrule{.1em}{.05em}{.05em}
| Classifier   | TensorFlow Lite NeuralNetwork | TensorFlow Lite Classifier | filename, input_shape, output_shape | Configure the TensorFlow Lite model to compute on the preprocessed data \\\specialrule{.1em}{.05em}{.05em}
| Preprocessor | enforce_size | EnforceSize | shape | Assert that the data length is suitable to shape. If it is longer, trim it to the correct size |
| Preprocessor | extract_axes | ExtractAxes | axes | Extract the required axes from the input list of SensorEvents |
| Preprocessor | fixed_time   | FixedInput | samples_per _second | Converts the SensorEvents list to a new list that has exact samples per second as required, and the timestamp difference between the following SensorEvents is constant |
| Preprocessor | gaf          | GAF | method | convert the input data to GAF 2d matrix as explained in \cref{GAF_EXPLAINED} |
| Preprocessor | l2norm    | L2norm | axes | For each SensorEvent, take the axes, square each one, and return the square root of the sum |
| Preprocessor | normalize | Normalizer | from_start, from_end, to_start, to_end | Translate the data minimum and maximum amplitude from [from_start, from_end] to [to_start, to_end] |
| Preprocessor | transform_to_ tensorflow_ buffer | InitializeTensorFlow Buffer | shape | Create TensorBuffer from the data by the shape to be used by the TensorFlow Lite model\\\specialrule{.1em}{.05em}{.05em}
| Analyzer     | threshold | ThresholdAnalyzer | pipelines, loss, threshold_amount | Compute the sum of the loss over the original data, and the output of the autoencoder model. Return trust value one if the sum is smaller than the provided threshold \\\specialrule{.1em}{.05em}{.05em}
| OnDetect     | block or default | DropEvent | None | Drop SensorEvent that has been flagged by the model |
| OnDetect     | default_event    | DefaultEvent | default_event | Replace SensorEvent values with default_event values. An accelerometer can use this since its resting values can be earth gravity on the y-axis. |
| OnDetect     | nothing | DoNothing | None | Do not act \\\specialrule{.1em}{.05em}{.05em}
| LossFunction | mse or MeanSquaredError | MSE | None | MeanSquaredError calculation over two elements of the same shape \\\specialrule{.1em}{.05em}{.05em}
| UIItem       | radio   | UIRadioButton | value, choices | Dynamic feature radio button configuration |
| UIItem       | bar     | UIBarScale      | value, low, high | Dynamic feature range bar configuration |
