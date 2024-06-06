package com.example.planthelper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.planthelper.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private ImageButton ibtnName;
    private EditText edtName;
    @SuppressLint("StaticFieldLeak")
    private static BluetoothManager bluetoothManager;
    private boolean editingName;



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Settings");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSave = binding.btnSaveSettings;
        Button btnDelete = binding.btnDeleteDevice;
        ibtnName = binding.ibtnEditName;
        ImageButton ibtnWifi = binding.ibtnEditWifi;
        edtName = binding.edtDeviceName;
        TextView tvWifi = binding.tvWifiName;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tvWifi.setText(bluetoothManager.getConnectedSsid());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            edtName.setText(bluetoothManager.readName());
        }

        edtName.setEnabled(false);
        editingName = false;
        ibtnName.setOnClickListener(v -> {
            if (editingName) {
                String name = String.valueOf(edtName.getText());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothManager.setName(name);
                }
                ControlPanelFragment.setDeviceName(name);
                edtName.setEnabled(false);
                ibtnName.setImageResource(R.drawable.ic_edit);
                editingName = false;
            }
            else {
                edtName.setEnabled(true);
                ibtnName.setImageResource(R.drawable.ic_save);
                editingName = true;
            }

        });

        ibtnWifi.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_addWifiFragment);
        });

        btnSave.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothManager.disconnect();
            }
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_controlPanel);
        });

        btnDelete.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_addDeviceFragment);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void setBluetoothManager(BluetoothManager manager) {
        bluetoothManager = manager;
    }

    public static BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

}