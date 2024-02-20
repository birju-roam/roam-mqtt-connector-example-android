package com.roam.mqttconnector.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static String shared_prefer = "roam";

    private static SharedPreferences getInstance(Context context) {
        return context.getSharedPreferences(shared_prefer, Context.MODE_PRIVATE);
    }

    private static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
        editor.commit();
    }

    private static boolean getBoolean(Context context, String key) {
        return getInstance(context).getBoolean(key, false);
    }

    private static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.putInt(key.toUpperCase(), value);
        editor.apply();
        editor.commit();
    }

    private static int getInt(Context context, String key) {
        return getInstance(context).getInt(key.toUpperCase(), 0);
    }

    private static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getInstance(context).edit();
        editor.putString(key.toUpperCase(), value);
        editor.apply();
        editor.commit();
    }

    private static String getString(Context context, String key) {
        return getInstance(context).getString(key.toUpperCase(), null);
    }

    public static void setUserId(Context context, String userId) {
         setString(context, Constants.USER_ID, userId);
    }

    public static String getUserId(Context context) {
        return getString(context, Constants.USER_ID);
    }
}



