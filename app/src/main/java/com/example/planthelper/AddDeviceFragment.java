package com.example.planthelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.FragmentAddDeviceBinding;
import com.example.planthelper.MainActivity;

import java.util.ArrayList;

public class AddDeviceFragment extends Fragment {

    private FragmentAddDeviceBinding binding;
    private Button btnContinue;
    private TextView tvDevice;

    public AddDeviceFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddDeviceBinding.inflate(inflater, container, false);



        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Menu menu = view.findViewById(R.id.main_menu);
//        Log.i("MYDEBUG", "add device check");
//        String test = String.valueOf(menu.size());
//        Log.i("MYDEBUG", test);


        btnContinue = binding.btnAddContinue;
        tvDevice = binding.tvDetectedDevice;

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_addDeviceFragment_to_addWifiFragment);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

//    @Override
//    public void onPrepareOptionsMenu(@NonNull Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        MenuItem item = menu.findItem(R.id.action_settings);
//        Log.i("MYDEBUG", "on prepare");
//    }

}