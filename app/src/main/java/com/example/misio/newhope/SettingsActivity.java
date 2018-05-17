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
    private final String APP = "com.example.het3crab.healthband";
    private final String ALERTS_KEY = "com.example.het3crab.healthband.alerts";
    private final String PULSE_FREQ_KEY = "com.example.het3crab.healthband.pulsefreq";
    private final String STEP_KEY = "com.example.het3crab.healthband.stepcount";

    private boolean isAlertOn = false;
    private boolean isStepCountOn = false;
    private int pulseFreqVal =30;

    private EditText pulseFreqText;
    private Switch alerts;
    private ToggleButton steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pulseFreqText = findViewById(R.id.pulseFreq);
        alerts = (Switch) findViewById(R.id.alerts);



        readSettings();

        //update settings view
        pulseFreqText.setText(String.valueOf(pulseFreqVal));
        alerts.setChecked(isAlertOn);
        // steps.setChecked(isStepCountOn);

        Toolbar appSettingsToolbar =
                (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(appSettingsToolbar);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
    }

    public void readSettings(){
        SharedPreferences prefs = this.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        isAlertOn = prefs.getBoolean(ALERTS_KEY, false);
        pulseFreqVal = prefs.getInt(PULSE_FREQ_KEY, 60);
        isStepCountOn = prefs.getBoolean(STEP_KEY, false);
    }

    private void saveSettings() {
        pulseFreqVal = Integer.parseInt(pulseFreqText.getText().toString());
        isAlertOn = alerts.isChecked();

        SharedPreferences prefs = this.getSharedPreferences(
                APP , Context.MODE_PRIVATE);

        prefs.edit().putBoolean(ALERTS_KEY, isAlertOn).apply();
        prefs.edit().putInt(PULSE_FREQ_KEY, pulseFreqVal).apply();
        prefs.edit().putBoolean(STEP_KEY, isStepCountOn).apply();
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
    public int getPulseFreqVal(){
        return pulseFreqVal;
    }

    public boolean getIsAlertOn(){
        return isAlertOn;
    }

    public boolean getIsStepCountOn(){
        return isStepCountOn;
    }


    @Override
    protected void onPause() {
        // call the superclass method first
        super.onPause();

        saveSettings();

    }
}
