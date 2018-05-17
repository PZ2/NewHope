package com.example.misio.newhope;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements PulseFragment.OnFragmentInteractionListener, BatteryFragment.OnFragmentInteractionListener, StepsFragment.OnFragmentInteractionListener {

    private Notifications mNotifications;
    Handler handler = new Handler(Looper.getMainLooper());
    BLEMiBand2Helper helper = null;
    final PulseFragment pulseFragment = new PulseFragment();
    boolean mBounded;
    TimeService mTimeService;

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
                    transaction.replace(R.id.content_main, new BatteryFragment()).commit();

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
                    transaction.replace(R.id.content_main, new StepsFragment()).commit();

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

        mNotifications = new Notifications(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            // Write you code here if permission already given.
        }

        Intent mIntent = new Intent(this, TimeService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        //int osiem = mTimeService.test();

        //startService(new Intent(this, TimeService.class));


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
                    pulseFragment.UpdateGUI();
                }
            });}
        }, 0, 5000);

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

    void requestHehe(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);

        }

    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service is disconnected", 1000).show();
            mBounded = false;
            mTimeService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this, "Service is connected", 1000).show();
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


