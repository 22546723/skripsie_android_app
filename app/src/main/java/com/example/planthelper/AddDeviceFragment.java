package com.example.planthelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.FragmentAddDeviceBinding;

import java.util.ArrayList;

public class AddDeviceFragment extends Fragment {

    private FragmentAddDeviceBinding binding;
    private RecyclerView rvDevices;

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
        rvDevices = binding.rvDevices;

        testList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void testList() {
//        ArrayList<AddDeviceModel> addDeviceModelArrayList = new ArrayList<>();
//        addDeviceModelArrayList.add(new AddDeviceModel("test1"));
//        addDeviceModelArrayList.add(new AddDeviceModel("test2"));

        //AddDeviceAdapter addDeviceAdapter = new AddDeviceAdapter(this.getContext(),addDeviceModelArrayList);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
//        rvDevices.setLayoutManager(linearLayoutManager);
//        rvDevices.setAdapter(addDeviceAdapter);
    }
}