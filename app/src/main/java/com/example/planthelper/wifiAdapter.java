package com.example.planthelper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.WifiCardBinding;

import java.util.List;

public class wifiAdapter extends RecyclerView.Adapter<wifiAdapter.viewHolder> {

    private WifiCardBinding binding;
    private List<String> networks;
    private int selected = -1;
    private int prevSelected = -1;
    private Context context;


    public static class viewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private CardView cvWifi;

        public viewHolder(@NonNull View itemView, WifiCardBinding binding) {
            super(itemView);
            tvName = binding.tvWifiCard;
            cvWifi = binding.cvWifiCard;
        }

        public TextView getTvName() {
            return tvName;
        }

        public CardView getCvWifi() {
            return cvWifi;
        }

        public void setBackground(boolean flag, Context c) {
            if (flag) {
                cvWifi.setCardBackgroundColor(c.getResources().getColor(R.color.background, null));
            }
            else {
                cvWifi.setCardBackgroundColor(c.getResources().getColor(R.color.white, null));
            }
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MINE", "clicked at "+holder.getAdapterPosition());
                prevSelected = selected;
                selected = holder.getAdapterPosition();
                notifyItemChanged(selected);
                notifyItemChanged(prevSelected);
            }
        });

        holder.getCvWifi().setSelected(selected == holder.getAdapterPosition());

    }

    @Override
    public int getItemCount() {
        return networks.size();
    }

    public int getSelected() {
        return selected;
    }

    public String getSelectedSSID() {
        return networks.get(selected);
    }
}
