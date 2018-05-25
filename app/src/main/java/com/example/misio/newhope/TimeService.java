package com.example.misio.newhope;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

    public class TimeService extends Service implements BLEMiBand2Helper.BLEAction {
        int pulseFreq;
        int average;
        boolean connect=false;
        private SmsManager smsManager = SmsManager.getDefault();

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

            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("My Awesome App")
                    .setContentText("Doing some work...")
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
                Settings.saveSetting(Settings.BATTERY_KEY, characteristic.getValue()[1], this);
            }
            else if (characteristic.getUuid().toString().equals(Consts.UUID_CHARACTERISTIC_7_REALTIME_STEPS.toString())){
                byte[] arr = {characteristic.getValue()[2] , characteristic.getValue()[1]};
                ByteBuffer wrapped = ByteBuffer.wrap(arr);
                short kroki = wrapped.getShort();

                Log.d("odczyt kroków", " - odczyt kroków: " + kroki);
                Settings.saveSetting(Settings.STEPS_KEY, kroki, this);
            }
        }

        @Override
        public void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID alertUUID = characteristic.getUuid();
            Log.d("odczyt pulsu", " - odczyt pulsu: " + characteristic.getValue()[1]);

            if (characteristic.getValue()[1] >= 0){
                final RealmPulseReading pulse = new RealmPulseReading();
                Calendar calendar = Calendar.getInstance();
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
            }
        }

        public void sendSms()
        {
            smsManager.sendTextMessage(Settings.readString(Settings.NUMBER_KEY, this), null, "elo", null, null);
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
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            v.vibrate(500);

                            if (pulses2.get(pulses2.size() - 1).getValue() == 0){
                                odczytPulsu();
                            }
                            sendSms();
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
                            Toast.makeText(getApplicationContext(), getDateTime(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            requestHehe();
                            connectToMiBand();
                        }
                    }

                });
            }

            private String getDateTime() {
                // get date time in custom format
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                return sdf.format(new Date());
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
