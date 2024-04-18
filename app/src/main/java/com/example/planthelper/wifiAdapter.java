package com.example.planthelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.WifiCardBinding;

import java.util.List;

public class wifiAdapter extends RecyclerView.Adapter<wifiAdapter.viewHolder> {

    private WifiCardBinding binding;
    private List<String> networks;


    public static class viewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;

        public viewHolder(@NonNull View itemView, WifiCardBinding binding) {
            super(itemView);
            tvName = binding.tvWifiCard;
        }

        public TextView getTvName() {
            return tvName;
        }
    }

    public wifiAdapter(List<String> n) {
        networks = n;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        binding = WifiCardBinding.inflate(inflater, container, false);

        return new viewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.getTvName().setText(networks.get(position));
    }


    @Override
    public int getItemCount() {
        return networks.size();
    }
}
