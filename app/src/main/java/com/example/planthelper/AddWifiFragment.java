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
    private RecyclerView rvWifi;
    private EditText edtPassword;
    private BluetoothManager bluetoothManager;
    private wifiAdapter adapter;

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



        // get the bt manager used by the add device fragment

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothManager = AddDeviceFragment.getBluetoothManager();
            Context c = getContext();
            Activity a = getActivity();
            bluetoothManager.setActCont(a, c);
            btnContinue.setEnabled(false);
            btnContinue.setText("Scanning");

            // delay by 100ms
            // if bluetoothManager.readNetworks() runs without the delay, the fragment layout only
            // shows after all the networks have been read
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<String> networks = bluetoothManager.readNetworks();
                    adapter = new wifiAdapter(networks);
                    rvWifi.setAdapter(adapter);
                    rvWifi.setLayoutManager(new LinearLayoutManager(c));
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Continue");
                }
            }, 100);

        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: add wifi verification
                String password = String.valueOf(edtPassword.getText());
                String ssid = adapter.getSelectedSSID();
                boolean connected = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    connected = bluetoothManager.connectWifi(ssid, password);
                }

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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothManager.disconnect();
        }
        binding = null;
    }
}