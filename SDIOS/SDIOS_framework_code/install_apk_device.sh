#/bin/sh
adb wait-for-device
for i in APP_PACKAGES/*/*.apk; do
    echo "installing $i"
    adb install -r $i
done