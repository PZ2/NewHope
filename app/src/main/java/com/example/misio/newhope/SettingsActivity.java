package com.example.misio.newhope;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    private boolean isAlertOn = false;
    private boolean isNotificationsOn = false;
    private int pulseFreqVal = 30;
    private String phoneNumber = "";
    private String miBandAddress = "D9:E3:90:3D:6F:93";

    private EditText pulseFreqText;
    private Switch alerts;
    private Switch notifications;
    private EditText number;
    private EditText miBand;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pulseFreqText = findViewById(R.id.pulseFreq);
        alerts = (Switch) findViewById(R.id.alerts);
        notifications = findViewById(R.id.notifications);
        number = findViewById(R.id.number);
        miBand = findViewById(R.id.miBand);

        settings = new Settings(this);
        settings.readSettings();

        //update settings view
        pulseFreqText.setText(String.valueOf(settings.getPulseFreqVal()));
        alerts.setChecked(settings.getIsAlertOn());
        notifications.setChecked(settings.getIsNotificationsOn());
        number.setText(settings.getPhoneNumber());
        miBand.setText(settings.getMiBandAddress());
        // steps.setChecked(isStepCountOn);

        Toolbar appSettingsToolbar =
                (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(appSettingsToolbar);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void saveSettings() {
        pulseFreqVal = Integer.parseInt(pulseFreqText.getText().toString());
        isAlertOn = alerts.isChecked();
        isNotificationsOn = notifications.isChecked();
        phoneNumber = number.getText().toString();
        miBandAddress = miBand.getText().toString();

        SharedPreferences prefs = getSharedPreferences(Settings.APP , Context.MODE_PRIVATE);

        prefs.edit().putBoolean(Settings.ALERTS_KEY, isAlertOn).apply();
        prefs.edit().putInt(Settings.PULSE_FREQ_KEY, pulseFreqVal).apply();
        prefs.edit().putBoolean(Settings.NOTIFICATIONS_KEY, isNotificationsOn).apply();
        prefs.edit().putString(Settings.NUMBER_KEY, phoneNumber).apply();
        prefs.edit().putString(Settings.ADDRESS_KEY, miBandAddress).apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:

                return true;

            case R.id.action_help:

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        // call the superclass method first
        super.onPause();

        saveSettings();

    }
}
