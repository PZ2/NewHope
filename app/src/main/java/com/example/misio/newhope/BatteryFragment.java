package com.example.misio.newhope;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BatteryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BatteryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BatteryFragment extends Fragment {

    public MainActivity mainActivity;
    public TextView batteryRate;
    public TextView batteryTime;
    public int batt = 60;
    public int days = 10;
    public int hours = 10;
    public int p;
    public TextView estimatedBatteryTime;
    public TextView batteryText;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public BatteryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BatteryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BatteryFragment newInstance(String param1, String param2) {
        BatteryFragment fragment = new BatteryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorBatteryDark));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_battery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        batteryRate = (TextView) getView().findViewById(R.id.batteryRate);
        if (batteryRate != null) batteryRate.setText(String.valueOf(batt));

        batteryTime = (TextView) getView().findViewById(R.id.batteryTime);
        if (batteryTime != null) batteryTime.setText(days + " days " + hours + " hours");

        estimatedBatteryTime = (TextView) getView().findViewById(R.id.estimatedBatteryTime);
        if (batteryRate != null) estimatedBatteryTime.setText("0");

        batteryText = (TextView) getView().findViewById(R.id.batteryHoursText);

        if( days*24+hours != 0 && 100-batt != 0) {
            p = ((days * 24 + hours)/(100-batt))*batt;
            if(p != 0) {
                estimatedBatteryTime.setText(String.valueOf(p));
                batteryText.setText("hours");
            } else {
                estimatedBatteryTime.setText("Can't calculate yet.");
                batteryText.setText("");
            }
        } else {
            estimatedBatteryTime.setText("Can't calculate yet.");
            batteryText.setText("");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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

    void UpdateGUI(int battery, String day, String hour) {
        if (batteryRate != null) batteryRate.setText(String.valueOf(battery));
        if (batteryTime != null) batteryTime.setText(day + " days " + hour + " hours");

        if (batteryRate != null) {
            if (days * 24 + hours != 0 && 100 - batt != 0) {
                p = ((days * 24 + hours)/(100-batt))*batt;
                if (p != 0) {
                    estimatedBatteryTime.setText(String.valueOf(p));
                    batteryText.setText("hours");
                } else {
                    estimatedBatteryTime.setText("Can't calculate yet.");
                    batteryText.setText("");
                }
            } else {
                estimatedBatteryTime.setText("Can't calculate yet.");
                batteryText.setText("");
            }
        }
    }
}
