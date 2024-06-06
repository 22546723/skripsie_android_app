package com.example.planthelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planthelper.databinding.WifiCardBinding;

import java.util.List;

/**
 * RecyclerView adapter for AddWifiFragment
 */
public class wifiAdapter extends RecyclerView.Adapter<wifiAdapter.viewHolder> {

    private final List<String> networks;
    private int selected = -1;
    private int prevSelected = -1;


    /**
     * View holder linked to wifi_card.xml
     */
    public static class viewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final CardView cvWifi;


        /**
         * Initialise a view holder linked to wifi_card.xml
         *
         * @param itemView View to use.
         * @param binding WifiCardBinding of the viewHolder.
         */
        public viewHolder(@NonNull View itemView, WifiCardBinding binding) {
            super(itemView);
            tvName = binding.tvWifiCard;
            cvWifi = binding.cvWifiCard;
        }


        /**
         * Returns the name of the text view in the card
         */
        public TextView getTvName() {
            return tvName;
        }


        /**
         * Returns the name of the card view
         */
        public CardView getCvWifi() {
            return cvWifi;
        }

    }


    /**
     * Initialise the RecyclerView adapter for AddWifiFragment
     *
     * @param n List<String> of detected network names
     */
    public wifiAdapter(List<String> n) {
        networks = n;
    }

    /**
     * Returns the SSID of the selected network
     */
    public String getSelectedSSID() {
        return networks.get(selected);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        com.example.planthelper.databinding.WifiCardBinding binding = WifiCardBinding.inflate(inflater, container, false);

        return new viewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.getTvName().setText(networks.get(position));

        holder.itemView.setOnClickListener(v -> {
            // update selected network
            prevSelected = selected;
            selected = holder.getAdapterPosition();

            // call onBindViewHolder to update the cards
            notifyItemChanged(selected);
            notifyItemChanged(prevSelected);
        });

        // set card appearance to indicate selection
        holder.getCvWifi().setSelected(selected == holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return networks.size();
    }
}
