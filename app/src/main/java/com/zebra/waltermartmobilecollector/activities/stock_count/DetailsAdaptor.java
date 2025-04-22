package com.zebra.waltermartmobilecollector.activities.stock_count;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class DetailsAdaptor extends RecyclerView.Adapter<DetailsAdaptor.ViewHolder> {

    private ArrayList<Model> list;

    public DetailsAdaptor(ArrayList<Model> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_stock_count, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model model = list.get(position);
        Log.d("TAG", "onBindViewHolder: " + model.getLocation() + " " + model.getQty() + " " + model.getSku());

            holder.sku.setText(model.getSku());
            holder.desc.setText(model.getDesc());
            holder.location.setText(model.getLocation());
            holder.qty.setText(model.getQty() + "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView sku, desc, location, option, qty;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sku = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.desc);
            location = itemView.findViewById(R.id.po);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}
