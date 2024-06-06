package com.example.planthelper;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.planthelper.databinding.FragmentControlPanelBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ControlPanelFragment extends Fragment {

    private FragmentControlPanelBinding binding;
    private GraphView graphView;
    private FirebaseFirestore db;

    private static DatabaseReference nameRef;
    private DatabaseReference targetRef;
    private static DatabaseReference updateRef;

    private final List<DataEntry> fbData = new ArrayList<>();

    private SeekBar sbTarget;
    private TextView tvTarget;
    private TextView tvSoilLive;
    private TextView tvUvLive;
    private Spinner spnTimeframe;

    private Button btnDate;
    private CalendarView calendarView;
    private Calendar calendar;

    private int dispType;


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        graphView = binding.idGraphView;
        sbTarget = binding.sbTarget;
        tvTarget = binding.tvSoilTarget;
        tvSoilLive = binding.tvSoilLive;
        tvUvLive = binding.tvLightLive;
        spnTimeframe = binding.spnTimeframe;

        btnDate = binding.btnDate;
        calendarView = binding.calendarView;
        calendarView.setVisibility(View.GONE);
        calendar = Calendar.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.timeframe_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTimeframe.setAdapter(adapter);

        spnTimeframe.setEnabled(false);
        spnTimeframe.setSelection(0);
        dispType = 0;

        spnTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dispType = position;
                updateDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Firebase setup
        db = FirebaseFirestore.getInstance();
        FirebaseDatabase rdb = FirebaseDatabase.getInstance();
        nameRef = rdb.getReference("status/name");
        targetRef = rdb.getReference("status/soil_target");
        updateRef = rdb.getReference("status/update");
        DatabaseReference soilRef = rdb.getReference("data/soil_moisture");
        DatabaseReference uvRef = rdb.getReference("data/uv_lvl");

        // Add value event listeners to update the name, soil target and sensor readings
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = snapshot.getValue(String.class);
                requireActivity().setTitle(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
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


        calendarView.setDate(calendar.getTimeInMillis());
        calendarView.setMaxDate(calendar.getTimeInMillis());
        String today = (calendar.get(Calendar.DAY_OF_MONTH) + " " + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.ENGLISH));
        btnDate.setText(today);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.YEAR, year);
            String day = (dayOfMonth + " " + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.ENGLISH));
            btnDate.setText(day);

            calendarView.setVisibility(View.GONE);
            graphView.setVisibility(View.VISIBLE);
            updateDisplay();
        });

        btnDate.setOnClickListener(v -> {

            if (calendarView.getVisibility() == View.GONE) {
                graphView.setVisibility(View.GONE);
                calendarView.setVisibility(View.VISIBLE);
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
     * Update the graph
     */
    private void updateDisplay() {
        switch (dispType) {
            case 0: dispDay();
                break;

            case 1: dispWeek();
                break;

            case 2: dispMonth();
                break;

            case 3: dispYear();
                break;
        }
    }


    /**
     * Read the data from the Firestore database into a list of DataEntry objects fbData.
     *
     * @see DataEntry
     */
    private void readFirestoreData() {
        spnTimeframe.setEnabled(false);

        db.collection("data")
                .get()
                .addOnCompleteListener(task -> {
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
                        fbData.sort(new recNoCompare());
                        dispDay();

                        spnTimeframe.setEnabled(true);
                    } else {
                        Log.w("FBASE", "Error getting documents.", task.getException());
                    }
                });
    }

    /**
     * Calculate the average hourly levels of the current day. Calls setGraph to display the data.
     *
     * @see #setGraph(DataPoint[], DataPoint[], LabelFormatter, int)
     */
    private void dispDay() {
        int day = calendar.get(Calendar.DAY_OF_YEAR);

        DataPoint[] soilData = new DataPoint[24];
        DataPoint[] uvData = new DataPoint[24];

        for (int i = 0; i < 24; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
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
                    return ((int) value + ":00"); // super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, false);
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
        int week = calendar.get(Calendar.WEEK_OF_YEAR);

        DataPoint[] soilData = new DataPoint[7];
        DataPoint[] uvData = new DataPoint[7];

        for (int i = 1; i <= 7; i++) {
            long avgSoil = 0;
            long avgUv = 0;
            long numEntries = 0;
            for (DataEntry entry : fbData) {
                int entWeek = entry.getCalendar().get(Calendar.WEEK_OF_YEAR);
                int entDay = entry.getCalendar().get(Calendar.DAY_OF_WEEK);
                if ((entDay == i) && (entWeek == week)) {
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
                    return super.formatLabel(value, false);
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
        int month = calendar.get(Calendar.MONTH);
        int weekMax = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);

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
                    return super.formatLabel(value, false);
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
                    return super.formatLabel(value, false);
                }

            }
        };

        setGraph(soilData, uvData, labelFormatter, 12);
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

        LineGraphSeries<DataPoint> soilSeries = new LineGraphSeries<>(soilData);
        soilSeries.setTitle("Soil moisture level");
        soilSeries.setColor(ContextCompat.getColor(requireContext(), R.color.soil_graph));

        LineGraphSeries<DataPoint> uvSeries = new LineGraphSeries<>(uvData);
        uvSeries.setTitle("UV light exposure");
        uvSeries.setColor(ContextCompat.getColor(requireContext(), R.color.uv_graph));


        // Setup legend
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        // Setup labels
        graphView.getGridLabelRenderer().setLabelFormatter(labelFormatter);
        graphView.getGridLabelRenderer().setNumHorizontalLabels(numTicks);
        graphView.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        graphView.getGridLabelRenderer().setLabelHorizontalHeight(100);

        graphView.getViewport().setScrollable(true);


        // Set data
        graphView.removeAllSeries();
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

    public static void setDeviceName(String name) {
        nameRef.setValue(name);
        updateRef.setValue("1");
    }

}