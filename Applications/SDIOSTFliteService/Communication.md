# Communication

This table includes a the communication message types in the SDIOS service.

| Name                      | Message type | Intended to | Parameters | Explanation |
| :------------------------ | :----------: | :---------- | :--------- | :---------- |
| MSG_REGISTER_SENSOR       | 1 | Client to service | sensor info and sampling rate | Register | sensor listener, respond with MSG_ACCEPT or MSG_REFUSE. SensorEvents are regular. |
| MSG_UNREGISTER_SENSOR     | 2 | Client to service | SensorEventListener identifier | UnRegister sensor listener |
| MSG_ON_SENSOR_CHANGED     | 3 | Service to client | SensorEvent data | the processed sensor event |
| MSG_ON_ACCURACY_CHANGED   | 4 | Service to client | Sensor and accuracy | OnAccuracyChanged required params |
| MSG_REGISTER_SENSOR_SDIOS   | 5 | Client to service | Same as MSG_REGISTER_SENSOR | Same as MSG_REGISTER_SENSOR, but SensorEvent has trust value |
| MSG_UNREGISTER_SENSOR_SDIOS | 6 | Client to service | Same as MSG_UNREGISTER _SENSOR | - |
| MSG_ACCEPT                | 7 | Service to client | SensorEventListener identifier | Register listener accepted |
| MSG_REFUSE                | 8 | Service to client | None | Register listener denied |
| MSG_ENABLE                | 9 | Service controller application to service | None | Enable SDIOS application protection |
| MSG_DISABLE               | 10 | Service controller application to Service | None | Disable SDIOS application protection |
| MSG_CHANGE_PACKAGE        | 11 | Service controller application to Service | None | Downloaded a new package reload package config |
| MSG_UPDATE_USER_CONFIGURATIONS | 12 | Service controller application to Service | None | Dynamic feature had been modified |
