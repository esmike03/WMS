package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;

import java.util.ArrayList;

public class ListAdaptor extends RecyclerView.Adapter<ListAdaptor.ViewHolder> {

    private ArrayList<POFilename> list;
    private ClickPOListener listener;

    public ListAdaptor(ClickPOListener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<POFilename> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_auto_matching_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        POFilename model = list.get(position);
        holder.po.setText(model.getPo());
        holder.po.setOnClickListener(v -> listener.run(model));
    }

    @Override
    public int getItemCount() {
        if (list == null) return 0;
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView po;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            po = itemView.findViewById(R.id.txtPas3PO);
        }
    }
}
