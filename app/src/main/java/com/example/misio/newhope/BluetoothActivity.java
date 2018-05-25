package com.example.misio.newhope;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;

    private List<BleDevice> devices;
    private EditText editText;

    private Button scanButton, pairedButton;

    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        editText = findViewById(R.id.editText);

        scanButton = findViewById(R.id.scanButton);
        pairedButton = findViewById(R.id.pairedButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });
        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaired();
            }
        });


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case RESULT_OK:
                connected = true;
                break;

            case RESULT_CANCELED:
                connected = false;
                break;
        }
    }

    private void scan(){
        editText.setText("");
        devices.clear();

        mBluetoothAdapter.startDiscovery();
    }

    private void showPaired(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        editText.setText("");
        devices.clear();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                devices.add(new BleDevice(deviceName, deviceHardwareAddress));

                String mDevice = "";
                mDevice += deviceName;
                mDevice += " ";
                mDevice += deviceHardwareAddress;
                mDevice += "\n";

                editText.append(mDevice);
            }
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                devices.add(new BleDevice(deviceName, deviceHardwareAddress));

                String mDevice = "";
                mDevice += deviceName;
                mDevice += " ";
                mDevice += deviceHardwareAddress;
                mDevice += "\n";
                editText.append(mDevice);
            }
        }
    };
}
