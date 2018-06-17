package com.example.misio.newhope;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

    public class TimeService extends Service implements BLEMiBand2Helper.BLEAction, LocationListener{
        int pulseFreq;
        int average;
        boolean connect = false;
        private SmsManager smsManager = SmsManager.getDefault();

        protected LocationManager locationManager;
        protected LocationListener locationListener;
        protected Context context;

        private long time = 0;

        String lat;
        String provider;
        protected String latitude,longitude;
        protected boolean gps_enabled,network_enabled;

        public RealmResults<RealmPulseReading> pulses2;
        public List<RealmPulseReading> pulses2ToAdd = new ArrayList<>();

        // run on another Thread to avoid crash
        private Handler mHandler = new Handler();
        // timer handling
        private Timer mTimer = null;

        Handler handler = new Handler(Looper.getMainLooper());
        BLEMiBand2Helper helper = null;

        @Override
        public void onCreate() {
            connectToMiBand();

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Healthband")
                    .setContentText("Measuring pulse now.")
                    .setContentIntent(pendingIntent).build();

            startForeground(1337, notification);

            pulseFreqUpdate();

            // cancel if already existed
            if (mTimer != null) {
                mTimer.cancel();
            } else {
                // recreate new
                mTimer = new Timer();
            }
            // schedule task
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, pulseFreq * 1000);

        }

        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Settings.saveSetting(Settings.LONGITUDE_KEY, String.valueOf(longitude), this);
            Settings.saveSetting(Settings.LATITUDE_KEY, String.valueOf(latitude), this);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude","disable");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude","enable");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Latitude","status");
        }

        public void pulseFreqUpdate(){
            pulseFreq = Settings.readInt(Settings.PULSE_FREQ_KEY, this);
        }

        public void ifFreqChange(){
            int pulseTemp = Settings.readInt(Settings.PULSE_FREQ_KEY, this);
            if (pulseTemp != pulseFreq) {
                mTimer.cancel();
                mTimer = new Timer();
                pulseFreq = pulseTemp;
                mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, pulseFreq * 1000);
            }

        }

        @Override
        public void onDisconnect() {
            connect = false;

        }

        @Override
        public void onConnect() {
            connect = true;
        }

        @Override
        public void onRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (characteristic.getUuid().toString().equals(Consts.UUID_CHARACTERISTIC_6_BATTERY_INFO.toString())){
                Log.d("odczyt baterii", " - odczyt baterii: " + characteristic.getValue()[1]);
               // Log.d("rok", " - odczyt baterii: " + characteristic.getValue()[1]);
                Settings.saveSetting(Settings.BATTERY_KEY, characteristic.getValue()[1], this);

                byte[] arr = {characteristic.getValue()[12] , characteristic.getValue()[11]};
                ByteBuffer wrapped = ByteBuffer.wrap(arr);
                short rok = wrapped.getShort();
                short miesiac = characteristic.getValue()[13];
                short dzien = characteristic.getValue()[14];
                short godzina = characteristic.getValue()[15];
                short minuta = characteristic.getValue()[16];
                short sekunda = characteristic.getValue()[17];

                Date data = new Date(rok-1900, miesiac-1, dzien, godzina, minuta, sekunda);
                Date date = new Date();
                long diffInMillies = Math.abs(date.getTime() - data.getTime());
                long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                long diffinh = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                long h = diffinh - days*24;

                Settings.saveSetting(Settings.BATTERYDAYS_KEY, String.valueOf(days), this);
                Settings.saveSetting(Settings.BATTERYHOURS_KEY, String.valueOf(h), this);

                Settings.saveSetting(Settings.READ_BATT_KEY, true, this);

            }
            else if (characteristic.getUuid().toString().equals(Consts.UUID_CHARACTERISTIC_7_REALTIME_STEPS.toString())){
                byte[] arr = {characteristic.getValue()[2] , characteristic.getValue()[1]};
                ByteBuffer wrapped = ByteBuffer.wrap(arr);
                short kroki = wrapped.getShort();
                int kalorie = characteristic.getValue()[9];

                byte[] arrr = {characteristic.getValue()[6] , characteristic.getValue()[5]};
                ByteBuffer wrapp = ByteBuffer.wrap(arrr);
                double km = wrapp.getShort();

                km -= km%10;
                km /= 1000;

                Log.d("odczyt kroków", " - odczyt kroków: " + kroki);
                Settings.saveSetting(Settings.STEPS_KEY, kroki, this);

                Log.d("odczyt kalorii", " - odczyt kalorii: " + kalorie);
                Settings.saveSetting(Settings.CALLORIES_KEY, String.valueOf(kalorie), this);

                Log.d("odczyt dystansu", " - odczyt dystansu: " + km);
                Settings.saveSetting(Settings.DISTANCE_KEY, String.format("%.2f", km), this);

                Settings.saveSetting(Settings.READ_STEPS_KEY, true, this);
            }
        }

        @Override
        public void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onNotification(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            UUID alertUUID = characteristic.getUuid();
            Log.d("odczyt pulsu", " - odczyt pulsu: " + characteristic.getValue()[1]);

            if (characteristic.getValue()[1] >= 0){
                final RealmPulseReading pulse = new RealmPulseReading();
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat simpleDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDate.setTimeZone(TimeZone.getTimeZone("Poland"));
                java.util.Date now = calendar.getTime();
                long x = now.getTime();
                pulse.setDate(x);
                pulse.setValue((int)(characteristic.getValue()[1]));

                Realm.init(this);
                RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
                Realm realm = Realm.getInstance(realmConfiguration);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(pulse);
                    }
                });
                lifeCheck();

                RequestQueue queue = Volley.newRequestQueue(this);
                final String url = "http://healthband-app.herokuapp.com/HBPulse/add-pulse/";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", "error");
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("pulsevalue", Integer.toString((int)(characteristic.getValue()[1])));
                        params.put("pulsedate", simpleDate.format(calendar.getTime()));
                        params.put("username", Settings.readString(Settings.USER_LOGIN_KEY, TimeService.this));
                        params.put("password", Settings.readString(Settings.USER_PASS_KEY, TimeService.this));

                        return params;
                    }
                };
                queue.add(postRequest);

                Settings.saveSetting(Settings.READ_PULSE_KEY, true, this);
            }
        }

        public void sendSms(int srednia)
        {
            String user = Settings.readString(Settings.USER_LOGIN_KEY, this);
            longitude = Settings.readString(Settings.LONGITUDE_KEY, this);
            latitude = Settings.readString(Settings.LATITUDE_KEY, this);

            smsManager.sendTextMessage(Settings.readString(Settings.NUMBER_KEY, this), null,
                    "User's "+ user
                        + " average of the 10 last pulse readings is "
                        + String.valueOf(srednia)
                        +" which is not within limit. Last known location when measured: "
                        + "Latitude: " + latitude + "Longitude: " + longitude
                        , null, null);
        }

        void lifeCheck() {
            Realm.init(this);
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
            Realm realm = Realm.getInstance(realmConfiguration);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (RealmPulseReading pulse : pulses2ToAdd) {
                        realm.insertOrUpdate(pulse);
                    }
                }
            });

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    pulses2 = realm.where(RealmPulseReading.class).findAll();
                    if (pulses2.size() > 9) {
                        average = 0;
                        for (int i=1; i<11; i++){
                            average += pulses2.get(pulses2.size() - i).getValue();
                        }
                        average /= 10;
                        Log.d("średnia", String.valueOf(average));
                        if (average > Settings.readMaxPulse(Settings.MAXPULSE_KEY, TimeService.this) || average < Settings.readMinPulse(Settings.MINPULSE_KEY, TimeService.this)) {

                            if (pulses2.get(pulses2.size() - 1).getValue() == 0){
                                odczytPulsu();
                            }

                            if(Settings.readBool(Settings.ALERTS_KEY, TimeService.this)){
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                // Vibrate for 500 milliseconds
                                v.vibrate(500);
                            }

                            if(time + 60000 > System.currentTimeMillis()) {
                                time = System.currentTimeMillis();
                                sendSms(average);
                            }
                        }

                    }
                }
            });
        }


        class TimeDisplayTimerTask extends TimerTask {

            @Override
            public void run() {
                // run on another thread
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // display toast
                        ifFreqChange();
                        if (connect)
                        {

                            helper.getNotificationsWithDescriptor(Consts.UUID_SERVICE_HEARTBEAT, Consts.UUID_NOTIFICATION_HEARTRATE, Consts.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
                            try {
                                Thread.sleep(1050);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            odczytPulsu();
                        }
                        else {
                            requestHehe();
                            connectToMiBand();
                        }
                    }

                });
            }

        }

        void connectToMiBand() {
            if (helper == null) {
                helper = new BLEMiBand2Helper(TimeService.this, handler);
                helper.addListener(this);
            }


            // Setup Bluetooth:
            helper.connect();

        }

        public void odczytPulsu(){
            helper.writeData(Consts.UUID_SERVICE_HEARTBEAT, Consts.UUID_START_HEARTRATE_CONTROL_POINT, new byte[]{21, 2, 1} );
        }

        void requestHehe(){
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                // Device doesn't support Bluetooth
            }
        }

        IBinder mBinder = new LocalBinder();

        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        public class LocalBinder extends Binder {
            public TimeService getServerInstance() {
                return TimeService.this;
            }
        }

        public void odczytBaterii(){
            helper.readData(Consts.UUID_SERVICE_MIBAND_SERVICE, Consts.UUID_CHARACTERISTIC_6_BATTERY_INFO);
        }

        public void odczytKrokow(){
            helper.readData(Consts.UUID_SERVICE_MIBAND_SERVICE, Consts.UUID_CHARACTERISTIC_7_REALTIME_STEPS);
        }


    }
