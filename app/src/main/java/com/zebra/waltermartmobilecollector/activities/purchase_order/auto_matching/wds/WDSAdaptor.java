package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.wds;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;
import com.zebra.waltermartmobilecollector.interfaces.EditPass3Listener;

import java.util.ArrayList;

public class WDSAdaptor extends RecyclerView.Adapter<WDSAdaptor.ViewHolder> {

    private ArrayList<Model> list;
    private EditPass3Listener listener;

    public WDSAdaptor(EditPass3Listener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<Model> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_wds_auto_matching_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model val = list.get(position);

        holder.barcode.setText(val.getBarcode());
        holder.sku.setText(val.getSku());
        holder.pas1.setText((val.getPas1()/val.getFactor()) + "");
        holder.pas3.setText(val.getPas3() / val.getFactor() + "");

        holder.layout.setOnClickListener(v -> listener.onEdit(val));
    }

    @Override
    public int getItemCount() {
        if (list == null) return 0;
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView pas1, sku, pas3, barcode;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            barcode = itemView.findViewById(R.id.txtBarcode);
            sku = itemView.findViewById(R.id.txtSKU);
            pas1 = itemView.findViewById(R.id.txtPas1);
            pas3 = itemView.findViewById(R.id.txtPas3);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
