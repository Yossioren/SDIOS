# SDIOS - Android framework code
This directory contains the SDIOS changes to the framework, allowing SDIOS to protect any existing application without modification.
The framework communicates with [SDIOS-Service](../../Applications/SDIOSTFliteService/) similarly to how the [SDIOS-Library](../../Applications/SDIOSTFliteClient/SDIOSClientLib/) does.

## How to compile
Read the [lineageOS instructions](https://wiki.lineageos.org/devices/) for your device.

Typically, you need to run these commands
```bash
mkdir /path/to/project/LineageOS_20.0
cd /path/to/project/LineageOS_20.0
# Choose the LineageOS version supported by your device; changing it later requires redoing all the following steps
# Old device support is listed in https://wiki.lineageos.org/devices/ if the "Hide discontinued devices" filter is removed.
repo init -u https://github.com/LineageOS/android.git -b lineage-20.0 --git-lfs
repo sync
source build/envsetup.sh
breakfast <device_codename>

# run our patcher to apply SDIOS over Lineage
pushd SDIOS/SDIOS/SDIOS_framework_code
git lfs fetch --all && git lfs pull # apt install git-lfs
python3 patcher.py -c lineage20.conf -d /path/to/project/LineageOS_20.0
popd

# unlock device OEM
# This script extracts proprietary blobs from your device - crucial for building a new image
./extract-files.sh

croot
brunch <device_codename>
cd $OUT

# unlock OEM and
fastboot flash recovery out/target/product/<device_codename>/recovery.img
fastboot reboot recovery

# Run sideload on lineage recovery
adb sideload out/target/product/<device_codename>/lineage_<device_codename>-ota-eng.<username>.zip
```

## Patching LineageOS

This directory includes patching files and instructions for inserting SDIOS into your repo's application framework.

Changed files
```
# SDIOS code added here
frameworks/base/core/java/android/hardware/SDIOS

# This class extends SensorEvent, and the package must be shared with it.
SensorEventSdios.java
frameworks/base/core/java/android/hardware/SensorEventSdios.java

# Copy SDIOS-Service and sensor-testing applications
packages/apps

# Adds the previous applications to the product image
build/target/product/handheld_product.mk
```

Some system files change between Android versions. Therefore, we supported both LineageOS 18 and 20.

Support for the new version can be added easily.
Although this is rare, pay attention to changes to SensorManager.
Here, we explain the basics of each change:

```
# Adding 'sensors' add 'sensors_raw' definition
frameworks/base/core/java/android/content/Context.java

# Registers Context.SENSOR_RAW_SERVICE to 'SystemSensorManager' and Context.SENSOR_SERVICE to 'SystemSensorManagerSdios'
# The order is important since a user can query Service by class. To support this, we need to register 'SystemSensorManagerSdios' after SystemSensorManager.
frameworks/base/core/java/android/app/SystemServiceRegistry.java


# Add permission for any app to query our SDIOS Service app
# pkg.addQueriesPackage("com.SDIOS.ServiceControl".intern());
frameworks/base/services/core/java/com/android/server/pm/pkg/parsing/ParsingPackageUtils.java
```

### Patcher
Param - AOSP/LineageOS root directory path
This script replaces files as needed and has a minimal validation mechanism.
```
Usage:
$ patcher.py -c config -d path

# For example 18.1
python3 patcher.py -c lineage18.conf -d ~/android_build/LineageOS_18.1

# For example 20.0 
python3 patcher.py -c lineage20.conf -d ~/android_build/LineageOS_20.0

> file does not exist
? copy dir
_ replace file
```

# Note
As stated in our paper, we compiled and tested LineageOS version 18.1 and 20.
Lineage version 21.0 was recently released. Due to an enhanced build system, it is much heavier to compile than previous versions. We added relevant changes to support Lineage 21.0 but have not been able to fully compile and test them on a real device.
