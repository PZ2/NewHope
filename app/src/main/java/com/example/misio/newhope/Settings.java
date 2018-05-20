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

    private boolean isAlertOn = false;
    private boolean isNotificationsOn = false;
    private int pulseFreqVal = 30;
    private String phoneNumber = "";
    private String miBandAddress = "D9:E3:90:3D:6F:93";

    private Context mContext;

    public Settings(Context context){
        mContext = context;
    }

    public void readSettings(){
        SharedPreferences prefs = mContext.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        isAlertOn = prefs.getBoolean(ALERTS_KEY, false);
        pulseFreqVal = prefs.getInt(PULSE_FREQ_KEY, 60);
        isNotificationsOn = prefs.getBoolean(NOTIFICATIONS_KEY, false);
        phoneNumber = prefs.getString(NUMBER_KEY, "");
        miBandAddress = prefs.getString(ADDRESS_KEY, "");
    }

    public int getPulseFreqVal(){
        return pulseFreqVal;
    }

    public boolean getIsAlertOn(){
        return isAlertOn;
    }

    public boolean getIsNotificationsOn(){
        return isNotificationsOn;
    }

    public String getMiBandAddress(){
        return miBandAddress;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }
}
