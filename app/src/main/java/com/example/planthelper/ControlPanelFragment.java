package com.example.planthelper;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.planthelper.databinding.FragmentControlPanelBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ControlPanelFragment extends Fragment {

    private FragmentControlPanelBinding binding;
    private GraphView graphView;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;

    private DatabaseReference nameRef;
    private DatabaseReference targetRef;
    private DatabaseReference updateRef;
    private DatabaseReference soilRef;
    private DatabaseReference uvRef;

    private List<DataEntry> fbData = new ArrayList<DataEntry>();
    private List<List<DataEntry>> monthData = new ArrayList<>(12);

    private SeekBar sbTarget;
    private TextView tvTarget;
    private TextView tvSoilLive;
    private TextView tvUvLive;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentControlPanelBinding.inflate(inflater, container, false);

        MainActivity.setMenuItemVis(true);
        if (MainActivity.getMenuCreated())
            requireActivity().invalidateOptionsMenu();

        requireActivity().setTitle("Control panel");

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        graphView = binding.idGraphView;
        sbTarget = binding.sbTarget;
        tvTarget = binding.tvSoilTarget;
        tvSoilLive = binding.tvSoilLive;
        tvUvLive = binding.tvLightLive;

        // Firebase setup
        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance();
        nameRef = rdb.getReference("status/name");
        targetRef = rdb.getReference("status/soil_target");
        updateRef = rdb.getReference("status/update");
        soilRef = rdb.getReference("data/soil_moisture");
        uvRef = rdb.getReference("data/uv_lvl");

        // Add value event listeners to update the name, soil target and sensor readings
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = snapshot.getValue(String.class);
                Log.d("FBASE", "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("FBASE", "Failed to read value.", error.toException());
            }
        });

        sbTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String disp = String.valueOf(progress) + '%';
                tvTarget.setText(disp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int val = seekBar.getProgress();
                targetRef.setValue(val);
                updateRef.setValue(true); // Alerts the esp32 that the soil target was changed
            }
        });

        soilRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = String.valueOf(snapshot.getValue(Long.class)) + '%';
                tvSoilLive.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        uvRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = String.valueOf(snapshot.getValue(Long.class)) + '%';
                tvUvLive.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        targetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long value = snapshot.getValue(Long.class);
                sbTarget.setProgress((int) value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        readFirestoreData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.setMenuItemVis(false);
        requireActivity().invalidateOptionsMenu();

        binding = null;
    }


    /**
     * Read the data from the Firestore database into a list of DataEntry objects fbData.
     *
     * @see DataEntry
     */
    private void readFirestoreData() {
        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Read the fields
                                long soil = (long) document.get("soil");
                                long uv = (long) document.get("uv");
                                String timestamp = (String) document.get("timestamp");

                                // Create a new DataEntry and add it to fbData
                                try {
                                    DataEntry dataEntry = new DataEntry(document.getId(), soil, uv, timestamp);
                                    fbData.add(dataEntry);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            // sort the data
                            // TODO: implement sorting by date and time?
                            fbData.sort(new recNoCompare());


                            // TODO: add a drop down to the graph card that lets you select the time interval
                            // TODO: replace this call with a done flag to enable the drop down
//                            dispYear();
//                            dispMonth();
//                            dispWeek();
                            dispDay();
                        } else {
                            Log.w("FBASE", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /**
     * Calculate the average hourly levels of the current day. Calls setGraph to display the data.
     *
     * @see #setGraph(DataPoint[], DataPoint[], LabelFormatter, int)
     */
    private void dispDay() {
        Calendar calendar = Calendar.getInstance();
//        int month = Calendar.MARCH; // calendar.get(Calendar.MONTH);
//        int week = 2; // calendar.get(Calendar.WEEK_OF_MONTH);
        int day = 64; // calendar.get(Calendar.DAY_OF_YEAR);

        DataPoint[] soilData = new DataPoint[24];
        DataPoint[] uvData = new DataPoint[24];

        for (int i = 0; i < 24; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
//                int entMonth = entry.getCalendar().get(Calendar.MONTH);
//                int entWeek = entry.getCalendar().get(Calendar.WEEK_OF_MONTH);
                int entDay = entry.getCalendar().get(Calendar.DAY_OF_YEAR);
                int entHour = entry.getCalendar().get(Calendar.HOUR_OF_DAY);
                if ((entHour == i) && (entDay == day)) {
                    avgSoil += entry.getSoilLvl();
                    avgUv += entry.getUvLvl();
                    numEntries++;
                }
            }

            if (numEntries != 0) {
                avgSoil = avgSoil / numEntries;
                avgUv = avgUv / numEntries;
            }

            soilData[i] = new DataPoint(i, avgSoil);
            uvData[i] = new DataPoint(i, avgUv);
        }

        LabelFormatter labelFormatter = new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return (String.valueOf((int)value) + ":00"); // super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }

            }
        };

        setGraph(soilData, uvData, labelFormatter, 12);
    }


    /**
     * Calculate the average daily levels of the current week. Calls setGraph to display the data.
     *
     * @see #setGraph(DataPoint[], DataPoint[], LabelFormatter, int)
     */
    private void dispWeek() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.WEEK_OF_MONTH);

        DataPoint[] soilData = new DataPoint[7];
        DataPoint[] uvData = new DataPoint[7];

        for (int i = 1; i <= 7; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
                int entMonth = entry.getCalendar().get(Calendar.MONTH);
                int entWeek = entry.getCalendar().get(Calendar.WEEK_OF_MONTH);
                int entDay = entry.getCalendar().get(Calendar.DAY_OF_WEEK);
                if ((entDay == i) && (entWeek == week) && (entMonth == month)) {
                    avgSoil += entry.getSoilLvl();
                    avgUv += entry.getUvLvl();
                    numEntries++;
                }
            }

            if (numEntries != 0) {
                avgSoil = avgSoil / numEntries;
                avgUv = avgUv / numEntries;
            }

            soilData[i-1] = new DataPoint(i-1, avgSoil);
            uvData[i-1] = new DataPoint(i-1, avgUv);
        }

        LabelFormatter labelFormatter = new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                    return days[(int) value]; // super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }

            }
        };

        setGraph(soilData, uvData, labelFormatter, 7);
    }


    /**
     * Calculate the average weekly levels of the current month. Calls setGraph to display the data.
     *
     * @see #setGraph(DataPoint[], DataPoint[], LabelFormatter, int)
     */
    private void dispMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int weekMax = calendar.get(Calendar.WEEK_OF_MONTH);

        DataPoint[] soilData = new DataPoint[weekMax];
        DataPoint[] uvData = new DataPoint[weekMax];

        for (int i = 1; i <= weekMax; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
                int week = entry.getCalendar().get(Calendar.WEEK_OF_MONTH);
                int entMonth = entry.getCalendar().get(Calendar.MONTH);
                if ((week == i) && (entMonth == month)) {
                    avgSoil += entry.getSoilLvl();
                    avgUv += entry.getUvLvl();
                    numEntries++;
                }
            }

            if (numEntries != 0) {
                avgSoil = avgSoil / numEntries;
                avgUv = avgUv / numEntries;
            }

            soilData[i-1] = new DataPoint(i, avgSoil);
            uvData[i-1] = new DataPoint(i, avgUv);
        }

        LabelFormatter labelFormatter = new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return String.valueOf((int)value); // super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }

            }
        };

        setGraph(soilData, uvData, labelFormatter, weekMax);
    }


    /**
     * Calculate the average monthly levels. Calls setGraph to display the data.
     * 
     * @see #setGraph(DataPoint[], DataPoint[], LabelFormatter, int)
     */
    private void dispYear() {
        DataPoint[] soilData = new DataPoint[12];
        DataPoint[] uvData = new DataPoint[12];

        for (int i = 0; i<12; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
                int month = entry.getCalendar().get(Calendar.MONTH);
                if (month == i) {
                    avgSoil += entry.getSoilLvl();
                    avgUv += entry.getUvLvl();
                    numEntries++;
                }
            }

            if (numEntries != 0) {
                avgSoil = avgSoil / numEntries;
                avgUv = avgUv / numEntries;
            }

            soilData[i] = new DataPoint(i, avgSoil);
            uvData[i] = new DataPoint(i, avgUv);

        }

        LabelFormatter labelFormatter = new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    return months[(int) value]; // super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }

            }
        };

        setGraph(soilData, uvData, labelFormatter, 12);

//        graphView.removeAllSeries();
//        LineGraphSeries<DataPoint> soilSeries = new LineGraphSeries<DataPoint>(soilData);
//        soilSeries.setTitle("Soil moisture level");
//        soilSeries.setColor(ContextCompat.getColor(requireContext(), R.color.soil_graph));
//
//        LineGraphSeries<DataPoint> uvSeries = new LineGraphSeries<DataPoint>(uvData);
//        uvSeries.setTitle("UV light exposure");
//        uvSeries.setColor(ContextCompat.getColor(requireContext(), R.color.uv_graph));
//
//        graphView.addSeries(soilSeries);
//
//        graphView.addSeries(uvSeries);
    }


    /**
     * Display the soil and UV data on the graph.
     *
     * @param soilData DataPoint array containing the soil data
     * @param uvData DataPoint array containing the UV data
     * @param labelFormatter Formatter for the labels
     * @param numTicks Number of labels on the x-axis
     */
    private void setGraph(DataPoint[] soilData, DataPoint[] uvData, LabelFormatter labelFormatter, int numTicks) {
//        DataPoint[] soilData = new DataPoint[fbData.size()];
//        DataPoint[] uvData = new DataPoint[fbData.size()];
//        for (int i = 0; i < fbData.size(); i++) { //fbData.size()
//            DataEntry entry = fbData.get(i);
//            soilData[i] = new DataPoint(entry.getDate(), entry.getSoilLvl());
//            uvData[i] = new DataPoint(entry.getDate(), entry.getUvLvl());
////            soilData[i] = new DataPoint(entry.getRecNo(), entry.getSoilLvl());
////            uvData[i] = new DataPoint(entry.getRecNo(), entry.getUvLvl());
//        }

        LineGraphSeries<DataPoint> soilSeries = new LineGraphSeries<DataPoint>(soilData);
//        BarGraphSeries<DataPoint> soilSeries = new BarGraphSeries<DataPoint>(soilData);
        soilSeries.setTitle("Soil moisture level");
        soilSeries.setColor(ContextCompat.getColor(requireContext(), R.color.soil_graph));

        LineGraphSeries<DataPoint> uvSeries = new LineGraphSeries<DataPoint>(uvData);
//        BarGraphSeries<DataPoint> uvSeries = new BarGraphSeries<DataPoint>(uvData);
        uvSeries.setTitle("UV light exposure");
        uvSeries.setColor(ContextCompat.getColor(requireContext(), R.color.uv_graph));


        graphView.removeAllSeries();

        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        graphView.setTitle("Today");

        // on below line we are setting
        // text color to our graph view.
//        graphView.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.
        graphView.setTitleTextSize(18);



        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graphView.getGridLabelRenderer().setLabelFormatter(labelFormatter);

//        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        graphView.getGridLabelRenderer().setNumHorizontalLabels(numTicks); // only 4 because of the space
//        graphView.getGridLabelRenderer().setHumanRounding(false);

        graphView.getViewport().setScrollable(true);
//        graphView.getViewport().setScrollableY(false);
        graphView.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        graphView.getGridLabelRenderer().setLabelHorizontalHeight(100);

//        graphView.getViewport().setScalable(true);
//        graphView.getViewport().setScalableY(false);

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(soilSeries);

        graphView.addSeries(uvSeries);
    }


    /**
     * Compare record numbers of DataEntry objects to sort
     */
    private static class recNoCompare implements Comparator<DataEntry> {
        @Override
        public int compare(DataEntry o1, DataEntry o2) {
            return (int) (o1.getRecNo() - o2.getRecNo());
        }
    }

    /**
     * Compare dates of DataEntry objects to sort
     */
    private static class dateCompare implements Comparator<DataEntry> {
        @Override
        public int compare(DataEntry o1, DataEntry o2) {
            Date d1 = o1.getDate();
            Date d2 = o2.getDate();
            return (d1.compareTo(d2));
        }
    }

}