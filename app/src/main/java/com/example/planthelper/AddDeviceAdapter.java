package com.example.planthelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddDeviceAdapter extends RecyclerView.Adapter<AddDeviceAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<AddDeviceModel> addDeviceModelArrayList;

    public AddDeviceAdapter(Context context, ArrayList<AddDeviceModel> addDeviceModelArrayList) {
        this.context = context;
        this.addDeviceModelArrayList = addDeviceModelArrayList;
    }


    @NonNull
    @Override
    public AddDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_device_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddDeviceModel model = addDeviceModelArrayList.get(position);
        holder.edtDeviceName.setText(model.getDevice_name());
        holder.btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.getCanEdit()) {
                    model.setDevice_name(String.valueOf(holder.edtDeviceName.getText()));
                    model.setCan_edit(false);
                    holder.btnEditName.setImageResource(android.R.drawable.ic_menu_edit);
                }
                else {
                    model.setCan_edit(true);
                    holder.btnEditName.setImageResource(android.R.drawable.ic_menu_save);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return addDeviceModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final EditText edtDeviceName;
        private final ImageButton btnEditName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            edtDeviceName = itemView.findViewById(R.id.edtDeviceName);
            btnEditName = itemView.findViewById(R.id.btnEditName);
        }
    }



}
