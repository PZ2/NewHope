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
    public static final String MINPULSE_KEY = "com.example.het3crab.healthband.minpulse";
    public static final String MAXPULSE_KEY = "com.example.het3crab.healthband.maxpulse";
    public static final String BATTERYDAYS_KEY = "com.example.het3crab.healthband.batterydays";
    public static final String BATTERYHOURS_KEY = "com.example.het3crab.healthband.batteryhours";
    public static final String USER_LOGIN_KEY = "com.example.het3crab.healthband.login";
    public static final String USER_PASS_KEY = "com.example.het3crab.healthband.password";
    public static final String CALLORIES_KEY = "com.example.het3crab.healthband.callories";
    public static final String DISTANCE_KEY = "com.example.het3crab.healthband.distance";
    public static final String DATE_KEY = "com.example.het3crab.healthband.date";
    public static final String DATE_KEY1 = "com.example.het3crab.healthband.date1";
    public static final String STEPSGOAL_KEY = "com.example.het3crab.healthband.stepsgoal";
    public static final String ISLOGGED_KEY = "com.example.het3crab.healthband.isloggedin";
    public static final String LONGITUDE_KEY = "com.example.het3crab.healthband.longitude";
    public static final String LATITUDE_KEY = "com.example.het3crab.healthband.latitude";
    public static final String READ_BATT_KEY = "com.example.het3crab.healthband.read_batt";
    public static final String READ_STEPS_KEY = "com.example.het3crab.healthband.read_steps";
    public static final String READ_PULSE_KEY = "com.example.het3crab.healthband.read_pulse";
    public static final String INIT_KEY = "com.example.het3crab.healthband.init";

    //miBandAddress = "D9:E3:90:3D:6F:93";
    public static int readInt(String KEY, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        return prefs.getInt(KEY, 20);
    }

    public static boolean readBool(String KEY, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        return prefs.getBoolean(KEY, false);
    }

    public static String readString(String KEY, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        return prefs.getString(KEY, "");
    }

    public static void saveSetting(String KEY, int value, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putInt(KEY, value).apply();
    }

    public static void saveSetting(String KEY, boolean value, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putBoolean(KEY, value).apply();
    }

    public static void saveSetting(String KEY, String value, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY, value).apply();
    }
}
