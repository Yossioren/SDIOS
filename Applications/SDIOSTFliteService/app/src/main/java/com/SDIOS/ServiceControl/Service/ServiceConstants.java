package com.SDIOS.ServiceControl.Service;

public class ServiceConstants {
    public static final String SENSOR_SERVICE_RAW = "sensor_raw";
    /**
     * Command to the service to display a message
     */
    public static final int MSG_KEEP_ALIVE = 0,
            MSG_REGISTER_SENSOR = 1, MSG_UNREGISTER_SENSOR = 2,
            MSG_ON_SENSOR_CHANGED = 3, MSG_ON_ACCURACY_CHANGED = 4,
            MSG_REGISTER_SENSOR_Sdios = 5, MSG_UNREGISTER_SENSOR_Sdios = 6,
            MSG_ACCEPT = 7, MSG_REFUSE = 8,
            MSG_ENABLE = 9, MSG_DISABLE = 10,
            MSG_CHANGE_PACKAGE = 11, MSG_UPDATE_USER_CONFIGURATIONS = 12;
}
