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
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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
    int stepGoal;

    CardView isLoggedView;

    private TextView user;
    private TextView isLoggedInText;

    private boolean isLogged = false;

    String batteryDays;
    String batteryH;
    String distance;
    String callories;

    public TextView User;

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

        if(!Settings.readBool(Settings.INIT_KEY, this)){
            Settings.saveSetting(Settings.PULSE_FREQ_KEY, 30, this);
            Settings.saveSetting(Settings.ALERTS_KEY, false, this);
            Settings.saveSetting(Settings.NOTIFICATIONS_KEY, false, this);
            Settings.saveSetting(Settings.NUMBER_KEY, "", this);
            Settings.saveSetting(Settings.ADDRESS_KEY, "DD:B0:AF:B3:09:42", this);
            Settings.saveSetting(Settings.MINPULSE_KEY, 35, this);
            Settings.saveSetting(Settings.MAXPULSE_KEY, 180,this);
            Settings.saveSetting(Settings.STEPSGOAL_KEY, 10000, this);
            Settings.saveSetting(Settings.INIT_KEY, true, this);
        }

        Settings.saveSetting(Settings.READ_PULSE_KEY, false, this);
        Settings.saveSetting(Settings.READ_STEPS_KEY, false, this);
        Settings.saveSetting(Settings.READ_BATT_KEY, false, this);

        user = (TextView) findViewById(R.id.userTextView);
        isLoggedInText = (TextView) findViewById(R.id.textView11);


        isLogged = Settings.readBool(Settings.ISLOGGED_KEY,MainActivity.this);
        if(isLogged == false){
            isLoggedInText.setText("Not logged in");
            user.setText("");
        } else if(isLogged == true) {
            isLoggedInText.setText("Logged in as:");
            user.setText(Settings.readString(Settings.USER_LOGIN_KEY, MainActivity.this));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        isLoggedView = (CardView) findViewById(R.id.loginCardView);
        isLoggedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

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
                    batteryDays = Settings.readString(Settings.BATTERYDAYS_KEY, MainActivity.this);
                    batteryH = Settings.readString(Settings.BATTERYHOURS_KEY, MainActivity.this);
                    callories = Settings.readString(Settings.CALLORIES_KEY, MainActivity.this);
                    distance = Settings.readString(Settings.DISTANCE_KEY, MainActivity.this);
                    stepGoal = Settings.readInt(Settings.STEPSGOAL_KEY, MainActivity.this);

                    if(Settings.readBool(Settings.READ_PULSE_KEY, MainActivity.this)) pulseFragment.UpdateGUI();
                    if(Settings.readBool(Settings.READ_BATT_KEY, MainActivity.this)) batteryFragment.UpdateGUI(battery, batteryDays, batteryH);
                    if(Settings.readBool(Settings.READ_STEPS_KEY, MainActivity.this)) stepsFragment.UpdateGUI(steps, callories, distance, stepGoal);

                    int currentTime = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    int currentTime1 = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);


                    int previousTime = Settings.readInt(Settings.DATE_KEY, MainActivity.this);
                    int previousTime1 = Settings.readInt(Settings.DATE_KEY1, MainActivity.this);

                    if (Settings.readBool(Settings.READ_STEPS_KEY, MainActivity.this) && Settings.readBool(Settings.NOTIFICATIONS_KEY, MainActivity.this) && steps >= stepGoal && currentTime != previousTime) {
                        Settings.saveSetting(Settings.DATE_KEY, currentTime, MainActivity.this);
                        mNotifications.showNotification("Daily Goal Achieved", "You have reached " + stepGoal + " steps today! Congratulations!", "Daily Goal", MainActivity.class);
                    }

                    if (Settings.readBool(Settings.READ_BATT_KEY, MainActivity.this) && Settings.readBool(Settings.NOTIFICATIONS_KEY, MainActivity.this) && battery <= 15 && currentTime1 != previousTime1) {
                        Settings.saveSetting(Settings.DATE_KEY1, currentTime1, MainActivity.this);
                        mNotifications.showNotification("MiBand's battery is low", "Currently " + battery + " %", "Battery Low", MainActivity.class);
                    }
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

       UpdateGUI();
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
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();

        isLogged = Settings.readBool(Settings.ISLOGGED_KEY, this);
        UpdateGUI();
    }

    void UpdateGUI(){
        if(isLogged == false){
            isLoggedInText.setText("Not logged in");
            user.setText("");
        } else if(isLogged == true) {
            isLoggedInText.setText("Logged in as:");
            user.setText(Settings.readString(Settings.USER_LOGIN_KEY, MainActivity.this));
        }
    }

}


