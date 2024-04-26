package com.example.planthelper;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.planthelper.databinding.FragmentControlPanelBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ControlPanelFragment extends Fragment {

    private FragmentControlPanelBinding binding;
    private GraphView graphView;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;

    private DatabaseReference nameRef;

    private List<DataEntry> fbData = new ArrayList<DataEntry>();;


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
        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance();
        nameRef = rdb.getReference("status/name");

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


        readFirestoreData();

//        testGraph();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.setMenuItemVis(false);
        requireActivity().invalidateOptionsMenu();

        binding = null;
    }

    private void readRealtimeData() {

    }


    private void readFirestoreData() {

        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("FBASE", document.getId() + " => " + document.getData());
//                                document.get("name");
//                                Log.d("FBASE", document.getId() + " name => " + document.get("name"));
                                long soil = (long) document.get("soil");
                                long uv = (long) document.get("uv");
                                String timestamp = (String) document.get("timestamp");
//                                Log.i("FBASE", "rec: "+document.get("name")+" | soil: "+soil+" | uv: "+uv+" | timestamp: "+timestamp);


                                try {
                                    DataEntry dataEntry = new DataEntry(document.getId(), soil, uv, timestamp);
                                    fbData.add(dataEntry);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }


                            // sort the data
                            // TODO: implement sorting by date and time
                            fbData.sort(new recNoCompare());

                            // Display the data
                            setGraph();
                        } else {
                            Log.w("FBASE", "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void setGraph() {
        DataPoint[] soilData = new DataPoint[fbData.size()];
        DataPoint[] uvData = new DataPoint[fbData.size()];
        for (int i = 0; i < fbData.size(); i++) { //fbData.size()
            DataEntry entry = fbData.get(i);
            soilData[i] = new DataPoint(entry.getRecNo(), entry.getSoilLvl());
            uvData[i] = new DataPoint(entry.getRecNo(), entry.getUvLvl());
        }

        LineGraphSeries<DataPoint> soilSeries = new LineGraphSeries<DataPoint>(soilData);
        soilSeries.setTitle("Soil moisture level");
        soilSeries.setColor(ContextCompat.getColor(requireContext(), R.color.soil_graph));

        LineGraphSeries<DataPoint> uvSeries = new LineGraphSeries<DataPoint>(uvData);
        uvSeries.setTitle("UV light exposure");
        uvSeries.setColor(ContextCompat.getColor(requireContext(), R.color.uv_graph));



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

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(soilSeries);

        graphView.addSeries(uvSeries);

        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background));
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }


    // Compare record numbers of DataEntry objects to sort fbData by record number
    private static class recNoCompare implements Comparator<DataEntry> {
        @Override
        public int compare(DataEntry o1, DataEntry o2) {
            return (int) (o1.getRecNo() - o2.getRecNo());
        }
    }

}