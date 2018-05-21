package com.example.misio.newhope;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by student on 20.05.2018.
 */

public class Settings {
    public static final String APP = "com.example.het3crab.healthband";
    public static final String ALERTS_KEY = "com.example.het3crab.healthband.alerts";
    public static final String PULSE_FREQ_KEY = "com.example.het3crab.healthband.pulsefreq";
    public static final String NOTIFICATIONS_KEY = "com.example.het3crab.healthband.notifications";
    public static final String NUMBER_KEY = "com.example.het3crab.healthband.number";
    public static final String ADDRESS_KEY = "com.example.het3crab.healthband.address";
    public static final String BATTERY_KEY = "com.example.het3crab.healthband.battery";
    public static final String STEPS_KEY = "com.example.het3crab.healthband.steps";

    //miBandAddress = "D9:E3:90:3D:6F:93";
    public static int readInt(String KEY, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        return prefs.getInt(KEY, 0);
    }

    public static boolean readBool(String KEY, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        return prefs.getBoolean(KEY, false);
    }

    public static  String readString(String KEY, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        return prefs.getString(KEY, "");
    }

    public static void saveSetting(String KEY, int value, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putInt(KEY, value).apply();
    }

    public static void saveSetting(String KEY, boolean value, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putBoolean(KEY, value).apply();
    }

    public static void saveSetting(String KEY, String value, Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY, value).apply();
    }
}
