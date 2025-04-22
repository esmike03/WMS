package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_rtv_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model rtv = list.get(position);

        holder.store.setText(rtv.getStore());
        holder.vendor.setText(rtv.getVendor());
        holder.upc.setText(rtv.getUpc());
        holder.desc.setText(rtv.getDesc());
        holder.qty.setText(rtv.getInputed_qty());
        holder.reason.setText(rtv.getReason());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView store, vendor, desc, qty, upc,reason;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            store = itemView.findViewById(R.id.po);
            vendor = itemView.findViewById(R.id.name);
            upc = itemView.findViewById(R.id.upc);
            desc = itemView.findViewById(R.id.desc);
            qty = itemView.findViewById(R.id.qty);
            reason = itemView.findViewById(R.id.reason);
        }
    }
}
