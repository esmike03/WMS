package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class DataAdaptor  extends RecyclerView.Adapter<DataAdaptor.ViewHolder> {

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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_po_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model po = list.get(position);

        holder.po.setText(po.getPo());
        holder.sku.setText(po.getSku());
        holder.desc.setText(po.getDesc());
        holder.qty.setText(po.getQty() + "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView po, sku, desc, qty;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            po = itemView.findViewById(R.id.po);
            sku = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.desc);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}
