package com.example.planthelper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuHost;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.planthelper.databinding.FragmentAddDeviceBinding;
import com.example.planthelper.databinding.FragmentControlPanelBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class ControlPanelFragment extends Fragment {

    private FragmentControlPanelBinding binding;
    private GraphView graphView;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentControlPanelBinding.inflate(inflater, container, false);


        Log.i("MYDEBUG", "1");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("MYDEBUG", "2");
//        MenuHelper menuHelper = new MenuHelper();
//        menuHelper.setMenuVis(true);


        graphView = binding.idGraphView;
//        Log.i("MYDEBUG", "cp 1");
//        toolbar = view.findViewById(R.id.toolbar);
//
//        int temp = toolbar.getId();
//        if (temp == -1)
//            Log.i("MYDEBUG", "no toolbar id");
//        else
//            Log.i("MYDEBUG", String.valueOf(temp));


       // Log.i("MYDEBUG", String.valueOf(toolbar.getId()));
//        Log.i("MYDEBUG", "cp 2");
//        toolbar.inflateMenu(R.menu.menu_main);
//        Log.i("MYDEBUG", "cp 3");


        testGraph();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        Log.i("MYDEBUG", "cp 4");
//        toolbar.getMenu().clear();
//        Log.i("MYDEBUG", "cp 5");

        binding = null;
    }




    private void testGraph() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                // on below line we are adding
                // each point on our x and y axis.
                new DataPoint(0, 1),
                new DataPoint(1, 3),
                new DataPoint(2, 4),
                new DataPoint(3, 9),
                new DataPoint(4, 6),
                new DataPoint(5, 3),
                new DataPoint(6, 6),
                new DataPoint(7, 1),
                new DataPoint(8, 2)
        });

        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        graphView.setTitle("My Graph View");

        // on below line we are setting
        // text color to our graph view.
//        graphView.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.
        graphView.setTitleTextSize(18);

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(series);
    }



//
////    @Override
////    public boolean onPrepareOptionsMenu(Menu menu) {
////        // Inflate the menu; this adds items to the action bar if it is present.
////
////        getMenuInflater().inflate(R.menu.menu_main, menu);
////        menuItem = menu.findItem(R.id.action_settings);
////        String test = String.valueOf(menuItem.getItemId());
////        Log.i("MYDEBUG", test);
////        return true;
////    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//            navController.navigate(R.id.action_global_settingsFragment);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}