package com.example.planthelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.planthelper.databinding.FragmentAddDeviceBinding;

public class AddDeviceFragment extends Fragment {

    private FragmentAddDeviceBinding binding;
    private Button btnContinue;
    private Button btnScan;
    private TextView tvDevice;
    private ProgressBar spinner;
    private BluetoothManager bluetoothManager; //TODO: recently changed from static, verify is everything still works


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddDeviceBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Connect to device");

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnContinue = binding.btnAddContinue;
        btnScan = binding.btnAddScan;
        tvDevice = binding.tvDetectedDevice;
        spinner = binding.progressBar2;

        spinner.setVisibility(View.GONE);

        tvDevice.setText("No device connected.");

        btnContinue.setEnabled(false);
        btnScan.setEnabled(true);

        btnContinue.setOnClickListener(v -> {
            // Send the connected bluetooth manager to the settings fragment
            SettingsFragment.setBluetoothManager(bluetoothManager);

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_addDeviceFragment_to_settingsFragment);
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the bt scan
                Context c = getContext();
                Activity a = getActivity();

                spinner.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothManager = new BluetoothManager(c, a);
                    bluetoothManager.scanForDevice();
                    btnScan.setEnabled(false);

                    // check if the scan is done every checkDelay ms
                    Handler scanHandler = new Handler();
                    int checkDelay = 100; // ms
                    scanHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (!(bluetoothManager.checkTimeout() || bluetoothManager.checkConnected())) {
                                // Delay and check again
                                scanHandler.postDelayed(this, checkDelay);
                            } else {
                                // Stop checking
                                btnScan.setEnabled(true);

                                // Display the connected device and enable the continue btn
                                if (bluetoothManager.checkConnected()) {
                                    tvDevice.setText(bluetoothManager.readName());
                                    spinner.setVisibility(View.GONE);
                                    btnContinue.setEnabled(true);
                                }
                            }
                        }
                    }, checkDelay);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}