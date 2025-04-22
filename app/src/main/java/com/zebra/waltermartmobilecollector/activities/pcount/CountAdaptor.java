package com.zebra.waltermartmobilecollector.activities.pcount;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class CountAdaptor extends RecyclerView.Adapter<CountAdaptor.ViewHolder> {

    private ArrayList<RVModel> list;

    public CountAdaptor(ArrayList<RVModel> d) {
        list = d;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_stock_count, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        RVModel model = list.get(position);

        holder.location.setText(model.getLocation());
        holder.skuCount.setText(model.getSkuCount() + "");
        holder.total.setText(model.getTotal() + "");

        holder.layout.setOnClickListener(v -> {
            model.toggleShow();
            if (model.isShow()) {
                model.resetPosition();
                model.setModel(Service.get(0, model.getLocation()));
            }
            notifyDataSetChanged();
        });

        if (!model.isShow()) {
            holder.details.setVisibility(View.GONE);
            return;
        }

        holder.details.setVisibility(View.VISIBLE);

        ScanModel m = model.getModel();
        holder.sku.setText(m.getSku());
        holder.desc.setText(m.getDesc());
        holder.dlocation.setText(m.getLocation());
        holder.qty.setText(m.getQty() + "");

        holder.close.setOnClickListener(v -> {
            model.closeShow();
            notifyDataSetChanged();
        });

        if (model.allowedPrev()){
            holder.prev.setEnabled(true);
            holder.prev.setOnClickListener(v -> {
                model.minusPosition();
                model.setModel(Service.get(model.getPosition(), model.getLocation()));
                notifyDataSetChanged();
            });
        }
        else
            holder.prev.setEnabled(false);

        if (!model.allowedNext()) {
            holder.next.setEnabled(false);
            return;
        }

        holder.next.setEnabled(true);
        holder.next.setOnClickListener(v -> {
            model.addPosition();
            model.setModel(Service.get(model.getPosition(), model.getLocation()));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView location, skuCount, total;
        TextView sku, desc, dlocation, qty;
        ConstraintLayout layout;
        CardView details;
        Button next, close, prev;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            location = itemView.findViewById(R.id.txtLocationRV);
            skuCount = itemView.findViewById(R.id.txtSKUCountRV);
            total = itemView.findViewById(R.id.txtTotalRV);
            layout = itemView.findViewById(R.id.mainLayout);
            details = itemView.findViewById(R.id.details);
            next = itemView.findViewById(R.id.btnNext);
            close = itemView.findViewById(R.id.btnClose);
            prev = itemView.findViewById(R.id.btnPrev);

            sku = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.desc);
            dlocation = itemView.findViewById(R.id.po);
            qty = itemView.findViewById(R.id.qty);
        }
    }
}
