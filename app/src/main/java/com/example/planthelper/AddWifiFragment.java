package com.example.planthelper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.planthelper.databinding.FragmentAddWifiBinding;
import com.example.planthelper.databinding.FragmentSettingsBinding;

public class AddWifiFragment extends Fragment {
    private FragmentAddWifiBinding binding;
    private Button btnContinue;
    private RecyclerView rvWifi;
    private EditText edtPassword;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddWifiBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnContinue = binding.btnWifiContinue;
        rvWifi = binding.rvWifi;
        edtPassword = binding.edtPassword;

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
        binding = null;
    }
}