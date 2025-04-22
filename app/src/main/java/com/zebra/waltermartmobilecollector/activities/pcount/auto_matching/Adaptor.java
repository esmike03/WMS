package com.zebra.waltermartmobilecollector.activities.pcount.auto_matching;

import android.annotation.SuppressLint;
import android.graphics.Color;
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

public class Adaptor extends RecyclerView.Adapter<Adaptor.ViewHolder> {

    private ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model> list;
    private EditPass3Listener listener;

    public Adaptor(EditPass3Listener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_auto_matching_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model val = list.get(position);

        holder.barcode.setText(val.getBarcode());
        holder.sku.setText(val.getSku());
        holder.pas1.setText((val.getPas1()) + "");
        holder.pas2.setText((val.getPas2()) + "");
        holder.pas3.setText(val.getPas3() + "");

        holder.layout.setOnClickListener(v -> listener.onEdit(val));

        int pas3Color;
        if (val.getPas3() == 0) pas3Color = Color.BLACK;
        else if (val.getPas1() == val.getPas3() || val.getPas2() == val.getPas3())
            pas3Color = Color.parseColor("#fec00d");
        else pas3Color = Color.parseColor("#FFA500");

        holder.pas3.setTextColor(pas3Color);

        int color;
        if (val.getPas1() == 0 && val.getPas2() == 0)
            color = Color.BLACK;
        else if (val.getPas1() == val.getPas2())
            color = Color.parseColor("#006400");
        else color = Color.RED;

        holder.pas1.setTextColor(color);
        holder.pas2.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        if (list == null) return 0;
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView pas1, sku, pas2, pas3, barcode;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            barcode = itemView.findViewById(R.id.txtBarcode);
            sku = itemView.findViewById(R.id.txtSKU);
            pas1 = itemView.findViewById(R.id.txtPas1);
            pas2 = itemView.findViewById(R.id.txtPas2);
            pas3 = itemView.findViewById(R.id.txtPas3);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
