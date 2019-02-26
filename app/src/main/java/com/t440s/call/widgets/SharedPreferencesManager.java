package com.t440s.call.widgets;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String APP_SETTINGS = "APP_SETTINGS";
    private static final String PASSWORD = "PASSWORD";
    private static final String SETPASS = "SETPASS";


    private SharedPreferencesManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String getPassword(Context context) {
        return getSharedPreferences(context).getString(PASSWORD , "");
    }

    public static void setPassword(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PASSWORD , newValue);
        editor.commit();
    }

    public static boolean isLocked(Context context) {
        return getSharedPreferences(context).getBoolean(SETPASS , false);
    }

    public static void setLocked(Context context, boolean bool) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(SETPASS , bool);
        editor.commit();
    }

}
