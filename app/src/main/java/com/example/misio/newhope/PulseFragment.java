package com.example.misio.newhope;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PulseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PulseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PulseFragment extends Fragment implements BLEMiBand2Helper.BLEAction{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Handler handler = new Handler(Looper.getMainLooper());
    BLEMiBand2Helper helper = null;

    boolean connect=false;
    private TextView heartRateView;
    int heartRate = 60;
    private FloatingActionButton fap;
    TextView pulseTextView;
    public MainActivity mainActivity;

    public RealmResults<RealmPulseReading> pulses;
    public List<RealmPulseReading> pulsesToAdd = new ArrayList<>();






    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PulseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PulseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PulseFragment newInstance(String param1, String param2) {
        PulseFragment fragment = new PulseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pulse, container, false);


    }
@Override
public void onViewCreated(View view, Bundle savedInstanceState){
    heartRateView = (TextView) getView().findViewById(R.id.heartRate);

    setHeartRate(heartRate);

    fap = getView().findViewById(R.id.fap);

    fap.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mainActivity.mTimeService.odczytPulsu();
        }
    });

    requestHehe();
    if (getArguments() != null) {
        mParam1 = getArguments().getString(ARG_PARAM1);
        mParam2 = getArguments().getString(ARG_PARAM2);
    }
}


    // TODO: Rename method, update argument and hook method into UI event
    public void onClick(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void connectToMiBand() {
        if (helper == null) {
            helper = new BLEMiBand2Helper(getContext(), handler);
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

    @Override
    public void onDisconnect() {
        connect = false;

    }

    @Override
    public void onConnect() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        helper.getNotificationsWithDescriptor(Consts.UUID_SERVICE_HEARTBEAT, Consts.UUID_NOTIFICATION_HEARTRATE, Consts.UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        odczytPulsu();
    }

    @Override
    public void onRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID alertUUID = characteristic.getUuid();
        Log.d("odczyt", " - odczyt: " + Arrays.toString(characteristic.getValue()));

        final RealmPulseReading pulse = new RealmPulseReading();
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        long x = now.getTime();
        pulse.setDate(x);
        pulse.setValue((int)(characteristic.getValue()[1]));
        pulseTextView.setText(characteristic.getValue()[1]);

        Realm.init(getContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(pulse);
            }
        });
    }

    public void setHeartRate(int heartRate) {
        heartRateView.setText(String.valueOf(heartRate));
    }


    void UpdateGUI(){
        Realm.init(getContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(RealmPulseReading pulse : pulsesToAdd){
                    realm.insertOrUpdate(pulse);
                }
            }
        });

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                pulses = realm.where(RealmPulseReading.class).findAll();
                if(pulses.size()>0) {
                    heartRate = pulses.get(pulses.size() - 1).getValue();
                }
            }
        });

        setHeartRate(heartRate);



        realm.close();
    }

}
