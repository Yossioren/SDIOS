LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := SDIOS-Service
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := SDIOS-Service-release.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)