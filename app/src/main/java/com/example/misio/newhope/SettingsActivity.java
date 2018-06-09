package com.example.misio.newhope;

import android.content.Intent;
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

public class SettingsActivity extends AppCompatActivity {

    private boolean isAlertOn = false;
    private boolean isNotificationsOn = false;
    private int pulseFreqVal;
    private String phoneNumber = "";
    private String miBandAddress = "D9:E3:90:3D:6F:93";
    private int minPulseVal;
    private int maxPulseVal;

    private EditText pulseFreqText;
    private Switch alerts;
    private Switch notifications;
    private EditText number;
    private EditText miBand;
    private EditText minPulse;
    private EditText maxPulse;
    private Button signinButton;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        signinButton = findViewById(R.id.signinButton);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        pulseFreqText = findViewById(R.id.pulseFreq);
        alerts = (Switch) findViewById(R.id.alerts);
        notifications = findViewById(R.id.notifications);
        number = findViewById(R.id.number);
        miBand = findViewById(R.id.miBand);
        minPulse = findViewById(R.id.lifeCheck1);
        maxPulse = findViewById(R.id.lifeCheck2);

        //update settings view
        pulseFreqText.setText(String.valueOf(Settings.readInt(Settings.PULSE_FREQ_KEY, this)));
        alerts.setChecked(Settings.readBool(Settings.ALERTS_KEY, this));
        notifications.setChecked(Settings.readBool(Settings.NOTIFICATIONS_KEY, this));
        number.setText(Settings.readPhoneNumber(Settings.NUMBER_KEY, this));
        miBand.setText(Settings.readString(Settings.ADDRESS_KEY, this));
        minPulse.setText(String.valueOf(Settings.readMinPulse(Settings.MINPULSE_KEY,this)));
        maxPulse.setText(String.valueOf(Settings.readMaxPulse(Settings.MAXPULSE_KEY,this)));
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
        minPulseVal = Integer.parseInt(minPulse.getText().toString());
        maxPulseVal = Integer.parseInt(maxPulse.getText().toString());

        Settings.saveSetting(Settings.PULSE_FREQ_KEY, pulseFreqVal, this);
        Settings.saveSetting(Settings.ALERTS_KEY, isAlertOn, this);
        Settings.saveSetting(Settings.NOTIFICATIONS_KEY, isNotificationsOn, this);
        Settings.saveSetting(Settings.NUMBER_KEY, phoneNumber, this);
        Settings.saveSetting(Settings.ADDRESS_KEY, miBandAddress, this);
        Settings.saveSetting(Settings.MINPULSE_KEY, minPulseVal, this);
        Settings.saveSetting(Settings.MAXPULSE_KEY,maxPulseVal,this);
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
