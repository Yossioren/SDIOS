# Supported SensorManager functionality

This table shows the compatibility of SensorManagerSdios with SensorManager.

Calls to unsupported SDIOS API will result in using the original SensorManager as a fallback.

| Function signature | Is supported in SensorManagerSdios | Is deprecated |
| :----------------- | :------------------------------- | :-----------: |
| public boolean registerListener(SensorListener listener, int sensors) | Default SensorManager is used | yes |
| public boolean registerListener(SensorListener listener, int sensors, int rate) | Default SensorManager is used | yes |
| void unregisterListener(SensorListener listener)| Default SensorManager is used | yes |
| void unregisterListener(SensorListener listener, int sensors)| Default SensorManager is used | yes \\\specialrule{.1em}{.05em}{.05em}
| void unregisterListener(SensorEventListener listener, Sensor sensor) | Yes | No |
| void unregisterListener(SensorEventListener listener) | Yes | No |
| boolean registerListenerSdios(SensorEventListener listener, Sensor sensor, int samplingPeriodUs) | Yes | Added in SensorManagerSdios |
| boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs) | Yes | No |
| boolean registerListenerSdios(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) | Yes | Added in SensorManagerSdios |
| boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) | Yes | No \\\specialrule{.1em}{.05em}{.05em}
| boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler) | Default SensorManager is used | No |
| boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, Handler handler) | Default SensorManager is used | No |
| boolean flush(SensorEventListener listener) | Default SensorManager is used | No |
| SensorDirectChannel createDirectChannel(MemoryFile mem) | Default SensorManager is used | No |
| SensorDirectChannel createDirectChannel(HardwareBuffer mem) | Default SensorManager is used | No |
| registerDynamicSensorCallback(android.hardware. SensorManager.DynamicSensorCallback callback) | Default SensorManager is used | No |
| void registerDynamicSensorCallback(android.hardware. SensorManager.DynamicSensorCallback callback, Handler handler) | Default SensorManager is used | No |
| unregisterDynamicSensorCallback(android.hardware. SensorManager.DynamicSensorCallback callback) | Default SensorManager is used | No |
| boolean requestTriggerSensor(TriggerEventListener listener, Sensor sensor) | Default SensorManager is used | No |
| boolean cancelTriggerSensor(TriggerEventListener listener, Sensor sensor) | Default SensorManager is used | No |
