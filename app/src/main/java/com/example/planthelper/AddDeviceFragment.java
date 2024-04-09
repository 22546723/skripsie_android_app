package com.example.planthelper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private BluetoothManager bluetoothManager;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddDeviceBinding.inflate(inflater, container, false);

        requireActivity().setTitle("Add new device");

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnContinue = binding.btnAddContinue;
        btnScan = binding.btnAddScan;
        tvDevice = binding.tvDetectedDevice;

        btnContinue.setEnabled(false);
        btnScan.setEnabled(true);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_addDeviceFragment_to_addWifiFragment);
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the bt scan
                Context c = getContext();
                Activity a = getActivity();

                if (a == null)
                    Log.i("MINE", "empty activity in frag");
                if (c == null)
                    Log.i("MINE", "empty context in frag");
                // TODO: remove this ^

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothManager = new BluetoothManager(c, a);
                    bluetoothManager.scanForDevice();
                }
            }
        });

        // TODO: display scan results?
        // TODO: link the scan to a btn so that the user can re-scan
        // TODO: toggle buttons
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}