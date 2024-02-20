package com.roam.mqttconnector.util;
import android.os.Build;

@SuppressWarnings("ALL")
public class GrantPermissions {
    public static boolean isAbove29() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
