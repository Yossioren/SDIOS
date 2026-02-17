package com.SDIOS.ServiceControl.Service;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.SDIOS.ServiceControl.Statistics.StatisticsMemoryStore;
import com.SDIOS.ServiceControl.Statistics.StatisticsTracker;
import com.SDIOS.ServiceControl.utils.ContextHolder;

import java.util.HashMap;
import java.util.Map;

public class UidDictionary {
    private final static StatisticsTracker statisticsTracker = new StatisticsMemoryStore();
    private final static String TAG = "UidDictionary";
    private final static String this_package = "com.SDIOS.ServiceControl";
    private final static Map<Integer, String> uid_to_name = new HashMap<>();
    private final static Map<Messenger, Integer> messenger_to_uid = new HashMap<>();
    private final static Map<Integer, Boolean> nameToHighSamplePermission = new HashMap<>();

    public static void set(int callingApp, String packageName) {
        uid_to_name.put(callingApp, packageName);
        nameToHighSamplePermission.put(callingApp, checkPermission(packageName));
        Log.d(TAG, String.format("Register uid: %d_%s ", callingApp, packageName));
    }

    private static boolean checkPermission(String packageName) {
        PackageManager pm = ContextHolder.get().getPackageManager();
        // Loop each package requesting <manifest> permissions
        for (PackageInfo packageInfo : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (!packageInfo.packageName.equals(packageName)) continue;
            String[] requestedPermissions = packageInfo.requestedPermissions;
            if (requestedPermissions == null) // No permissions defined in <manifest>
                return false;
            // Loop each <uses-permission> tag to retrieve the permission flag
            for (int i = 0; i < requestedPermissions.length; i++) {
                if (requestedPermissions[i].equals("android.permission.HIGH_SAMPLING_RATE_SENSORS"))
                    return (packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
            }
        }
        return false;
    }

    public static boolean isPrivileged(int callingApp) {
        boolean result = uid_to_name.containsKey(callingApp) && this_package.equals(uid_to_name.get(callingApp));
        if (!result)
            Log.w(TAG, String.format("Access privileged api! %d_%s", callingApp,
                    uid_to_name.getOrDefault(callingApp, "Null")));
        return result;
    }

    public static boolean allowedHighSamplingRate(int callingApp) {
        boolean result = true;//nameToHighSamplePermission.getOrDefault(callingApp, false);
        if (!result)
            Log.w(TAG, String.format("Requested high sampling rate, but is not permitted! %d_%s",
                    callingApp, uid_to_name.getOrDefault(callingApp, "Null")));
        return result;
    }

    public static void set(Message message) {
        messenger_to_uid.put(message.replyTo, message.sendingUid);
        set(message.sendingUid);
    }

    private static void set(int sendingUid) {
        if (uid_to_name.containsKey(sendingUid)) return;
        String packageName = ContextHolder.get().getPackageManager().getNameForUid(sendingUid);
        if (packageName != null) {
            statisticsTracker.set_application_connection(packageName);
            set(sendingUid, packageName);
        } else
            Log.e(TAG, "Null package name " + sendingUid);
    }

    public static String getPackage(int sendingUid) {
        return uid_to_name.getOrDefault(sendingUid, "");
    }

    public static String getPackage(Messenger replyTo) {
        if (messenger_to_uid.containsKey(replyTo))
            return uid_to_name.getOrDefault(messenger_to_uid.get(replyTo), "");
        Log.w(TAG, "Request missing messenger");
        return "";
    }
}
