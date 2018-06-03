package com.example.misio.newhope;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements PulseFragment.OnFragmentInteractionListener, BatteryFragment.OnFragmentInteractionListener, StepsFragment.OnFragmentInteractionListener {

    private Notifications mNotifications;

    private int REQUEST_ENABLE_BT = 1;

    final PulseFragment pulseFragment = new PulseFragment();
    final BatteryFragment batteryFragment = new BatteryFragment();
    final StepsFragment stepsFragment = new StepsFragment();
    boolean mBounded;
    TimeService mTimeService;
    int battery;
    int steps;
    String batteryDays;
    String batteryH;

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
            int colorToBattery = getColor(R.color.colorBattery);
            int colorToPulse = getColor(R.color.colorPulse);
            int colorToSteps = getColor(R.color.colorSteps);
            int colorFrom = ((ColorDrawable)appToolbar.getBackground()).getColor();
            ValueAnimator colorAnimation;

            switch (item.getItemId()) {
                case  R.id.bottom_menu_battery:
                    transaction.replace(R.id.content_main, batteryFragment).commit();

                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorToBattery);
                    colorAnimation.setDuration(500);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
                            appToolbar.setBackgroundColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                    batteryFragment.batt = battery;
                    mTimeService.odczytBaterii();

                    return true;

                case R.id.bottom_menu_pulse:
                    transaction.replace(R.id.content_main,pulseFragment).commit();

                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorToPulse);
                    colorAnimation.setDuration(500);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
                            appToolbar.setBackgroundColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                    return true;

                case R.id.bottom_menu_steps:
                    transaction.replace(R.id.content_main, stepsFragment).commit();

                    colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorToSteps);
                    colorAnimation.setDuration(500);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
                            appToolbar.setBackgroundColor((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                    stepsFragment.step = steps;
                    mTimeService.odczytKrokow();

                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pulseFragment.mainActivity = this;
        batteryFragment.mainActivity = this;
        stepsFragment.mainActivity = this;

        mNotifications = new Notifications(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            //if bluetooth is not supported exit
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        if(!mBluetoothAdapter.isEnabled())
        {
            //if bluetooth is off ask for enable
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        Intent mIntent = new Intent(this, TimeService.class);
        startService(new Intent(this, TimeService.class));
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(appToolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_menu_pulse);

        transaction.replace(R.id.content_main, pulseFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {runOnUiThread(new Runnable() {
                public void run() {
                    battery = Settings.readInt(Settings.BATTERY_KEY, MainActivity.this);
                    steps = Settings.readInt(Settings.STEPS_KEY, MainActivity.this);
                    batteryDays = Settings.batteryDate(Settings.BATTERYDAYS_KEY, MainActivity.this);
                    batteryH = Settings.batteryDate(Settings.BATTERYHOURS_KEY, MainActivity.this);
                    pulseFragment.UpdateGUI();
                    batteryFragment.UpdateGUI(battery, batteryDays, batteryH);
                    stepsFragment.UpdateGUI(steps);
                }
            });}
        }, 0, 5000);

        Timer smsTimer = new Timer();
        smsTimer.schedule(new TimerTask() {
            @Override
            public void run() {runOnUiThread(new Runnable() {
                public void run() {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
                    }
                }
            });}
        }, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mTimeService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            TimeService.LocalBinder mLocalBinder = (TimeService.LocalBinder)service;
            mTimeService = mLocalBinder.getServerInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }



}


