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

public class POCountAdaptor extends RecyclerView.Adapter<POCountAdaptor.ViewHolder> {

    private ArrayList<POCountModel> list;

    public POCountAdaptor(ArrayList<POCountModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_po_po_count, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        POCountModel model = list.get(position);

        holder.po.setText(model.getPo());
        holder.count.setText(model.getCount());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView po, count;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            po = itemView.findViewById(R.id.txtPONo);
            count = itemView.findViewById(R.id.txtSKUCount);
        }
    }
}
