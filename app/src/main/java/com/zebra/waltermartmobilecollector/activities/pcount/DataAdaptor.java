package com.zebra.waltermartmobilecollector.activities.pcount;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class DataAdaptor extends RecyclerView.Adapter<DataAdaptor.ViewHolder> {

    private ArrayList<Model> list = new ArrayList<>();
    private int cursor = 0;
    public static final int TOTAL_PER_LOAD = 10;

    public DataAdaptor() {
        Service.get(0, TOTAL_PER_LOAD, list);
    }

    public void loadMore(){
        if((cursor + TOTAL_PER_LOAD) > list.size()) return;

        cursor += TOTAL_PER_LOAD;
        Service.get(cursor, TOTAL_PER_LOAD, list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_pcount_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model rtv = list.get(position);

        holder.location.setText(rtv.getLocation());
        holder.vendor.setText(rtv.getVendor());
        holder.upc.setText(rtv.getUpc());
        holder.desc.setText(rtv.getDesc());
        holder.qty.setText(rtv.getUpdatedQty());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView location, vendor, desc, qty, upc;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.po);
            vendor = itemView.findViewById(R.id.name);
            upc = itemView.findViewById(R.id.upc);
            desc = itemView.findViewById(R.id.desc);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}
