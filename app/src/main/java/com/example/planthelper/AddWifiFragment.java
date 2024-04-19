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
import android.widget.EditText;

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
            List<String> networks = bluetoothManager.readNetworks();
            rvWifi.setAdapter(new wifiAdapter(networks));
            rvWifi.setLayoutManager(new LinearLayoutManager(c));
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: add wifi verification
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_addWifiFragment_to_settingsFragment);
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