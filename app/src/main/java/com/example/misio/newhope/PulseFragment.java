package com.example.misio.newhope;

import android.content.Context;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PulseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PulseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PulseFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView heartRateView;
    int heartRate = 60;
    private FloatingActionButton fap;
    public MainActivity mainActivity;

    private Button graphButton;
    private TextView timeBeg, dateBeg, timeEnd, dateEnd;
    private int hoursBeg, minutesBeg, hoursEnd, minutesEnd;
    private int yearBeg, monthBeg, dayBeg, yearEnd, monthEnd, dayEnd;
    private String timeBegDate, timeEndDate;
    public long beg, end;

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

        /*Generuje graf pulsów między podanymi datami
        graphButton = getView().findViewById(R.id.graphButton);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                formatter.setLenient(false);

                Date oldDate = null;
                try {
                    oldDate = formatter.parse(timeBegDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                beg = oldDate.getTime();

                Date newDate = null;
                try {
                    newDate = formatter.parse(timeEndDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                end = newDate.getTime();


                //pulses = realm.where(RealmPulseReading.class).between("date" , beg, end).findAll();

            }
        });
        */

        timeBeg = getView().findViewById(R.id.timeBeg);
        timeBeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.setListener(PulseFragment.this, TimePickerFragment.TIME_BEG);
                dialog.show(getFragmentManager(), "Time Picker");
            }
        });

        dateBeg = getView().findViewById(R.id.dateBeg);
        dateBeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.setListener(PulseFragment.this, DatePickerFragment.DATE_BEG);
                dialog.show(getFragmentManager(), "Date Picker");
            }
        });

        timeEnd = getView().findViewById(R.id.timeEnd);
        timeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.setListener(PulseFragment.this, TimePickerFragment.TIME_END);
                dialog.show(getFragmentManager(), "Time Picker");
            }
        });

        dateEnd = getView().findViewById(R.id.dateEnd);
        dateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.setListener(PulseFragment.this, DatePickerFragment.DATE_END);
                dialog.show(getFragmentManager(), "Date Picker");
            }
        });


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        GraphView graph = (GraphView) getView().findViewById(R.id.graph);

        graph.getGridLabelRenderer();
        //graph.setBackgroundColor(getResources().getColor(R.color.colorAccent));


        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setHighlightZeroLines(false);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);



//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 60),
//                new DataPoint(1, 80),
//                new DataPoint(2, 70),
//                new DataPoint(3, 90),
//                new DataPoint(4, 60)
//        });
//        series.setAnimated(true);
//        series.setColor(R.color.colorAccent);
//        series.setDrawDataPoints(true);
//
//        graph.addSeries(series);

        createGraph();
    }

    public void createGraph(){
        GraphView graph = (GraphView) getView().findViewById(R.id.graph);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show date values
                    return getDate(value, "dd/MM HH:mm");
                } else {
                    // show normal y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(220);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(150000);
        graph.getViewport().setMaxX(999999999);

        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true);
    }

    public static Date getDate(long milliSeconds) {
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    public static String getDate(double milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) milliSeconds);
        return formatter.format(calendar.getTime());
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

        /*DataPoint[] dataPoints = new DataPoint[pulses.size()];
        int x = 0;
        for(RealmPulseReading pulse : pulses){
            DataPoint dataPoint = new DataPoint(getDate(pulse.getDate()), (double)pulse.getValue());
            dataPoints[x] = dataPoint;
            x++;
        }

        if (getView() != null) {
            GraphView graph = (GraphView) getView().findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

            //series.setAnimated(true);
            series.setColor(R.color.colorAccent);
            series.setDrawDataPoints(true);

            graph.removeAllSeries();
            graph.addSeries(series);
        }*/

        realm.close();
    }

    public void onTimeSet(String who, int hours, int minutes){
        switch (who){
            case TimePickerFragment.TIME_BEG:
                hoursBeg = hours;
                minutesBeg = minutes;
                break;

            case TimePickerFragment.TIME_END:
                hoursEnd = hours;
                minutesEnd = minutes;
                break;
        }

        updateTimeAndDate();
    }

    public void onDateSet(String who, int year, int month, int day){
        switch (who){
            case DatePickerFragment.DATE_BEG:
                yearBeg = year;
                monthBeg = month;
                dayBeg = day;
                break;

            case DatePickerFragment.DATE_END:
                yearEnd = year;
                monthEnd = month;
                dayEnd = day;
                break;
        }

        updateTimeAndDate();
    }

    private void updateTimeAndDate(){
        String time = "";
        time += hoursBeg;
        time += ":";
        time += minutesBeg;
        timeBeg.setText(time);

        String date = "";
        date += dayBeg;
        date += ".";
        date += monthBeg;
        date += ".";
        date += yearBeg;
        dateBeg.setText(date);

        timeBegDate += date;
        timeBegDate += " ";
        timeBegDate += time;

        time = "";
        time += hoursEnd;
        time += ":";
        time += minutesEnd;
        timeEnd.setText(time);


        date = "";
        date += dayEnd;
        date += ".";
        date += monthEnd;
        date += ".";
        date += yearEnd;
        dateEnd.setText(date);

        timeEndDate += date;
        timeEndDate += " ";
        timeEndDate += time;
    }
}
