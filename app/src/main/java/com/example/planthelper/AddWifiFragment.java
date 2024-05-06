package com.example.planthelper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.FragmentAddWifiBinding;

import java.util.List;

public class AddWifiFragment extends Fragment {
    private FragmentAddWifiBinding binding;
    private Button btnContinue;
    private Button btnCancel;
    private RecyclerView rvWifi;
    private EditText edtPassword;
    private BluetoothManager bluetoothManager;
    private wifiAdapter adapter;
    private ProgressBar spinner;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        binding = FragmentAddWifiBinding.inflate(inflater, container, false);
        requireActivity().setTitle("WiFi setup");
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnContinue = binding.btnWifiContinue;
        rvWifi = binding.rvWifi;
        edtPassword = binding.edtPassword;
        spinner = binding.progressBar;
        btnCancel = binding.btnCancelWifi;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // get the bt manager from add device fragment
//            bluetoothManager = AddDeviceFragment.getBluetoothManager();
            bluetoothManager = SettingsFragment.getBluetoothManager();

            // update bluetooth manager activity and context
            Context c = getContext();
            Activity a = getActivity();
            bluetoothManager.setActCont(a, c);

            // block user while scanning for networks
            btnContinue.setEnabled(false);
            btnContinue.setText("Scanning");
            btnCancel.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);

            // delay by 100ms
            // if bluetoothManager.readNetworks() runs without the delay, the fragment layout only
            // shows after all the networks have been read
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<String> networks = bluetoothManager.readNetworks();

                    // setup RecyclerView
                    adapter = new wifiAdapter(networks);
                    rvWifi.setAdapter(adapter);
                    rvWifi.setLayoutManager(new LinearLayoutManager(c));

                    // allow user to continue
                    spinner.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Continue");
                    btnCancel.setVisibility(View.VISIBLE);
                }
            }, 100);

        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = String.valueOf(edtPassword.getText());
                String ssid = adapter.getSelectedSSID();
                boolean connected = false;

                spinner.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    connected = bluetoothManager.connectWifi(ssid, password);
                }

                spinner.setVisibility(View.GONE);
                if (connected) {
                    Toast.makeText(getContext(), "WiFi connected", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_addWifiFragment_to_settingsFragment);
                }
                else {
                    Toast.makeText(getContext(), "WiFi connection unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_addWifiFragment_to_settingsFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            bluetoothManager.disconnect();
        }
        binding = null;
    }
}