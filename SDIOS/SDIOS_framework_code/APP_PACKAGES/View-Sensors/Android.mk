LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := View-Sensors
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := view_sensors-release.apk
LOCAL_MODULE_CLASS := APPS
include $(BUILD_PREBUILT)
