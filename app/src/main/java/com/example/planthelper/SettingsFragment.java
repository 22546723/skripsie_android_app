package com.example.planthelper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.planthelper.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private Button btnSave, btnDelete;
    private ImageButton ibtnName, ibtnWifi;
    private EditText edtName;
    private TextView tvWifi;

//    public SettingsFragment() {
//
//    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        btnSave = binding.btnSaveSettings;
        btnDelete = binding.btnDeleteDevice;
        ibtnName = binding.ibtnEditName;
        ibtnWifi = binding.ibtnEditWifi;
        edtName = binding.edtDeviceName;
        tvWifi = binding.tvWifiName;

        ibtnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_settingsFragment_to_addWifiFragment);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: Add code to store data
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_settingsFragment_to_controlPanel);
                //MainActivity.setMenuItemVis(true);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: Add code to delete device data
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_settingsFragment_to_addDeviceFragment);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}