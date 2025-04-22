package com.zebra.waltermartmobilecollector.activities.pcount.withdrawal;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.pcount.ClickLocationListener;

import java.util.ArrayList;

public class LocationSelectionAdaptor extends RecyclerView.Adapter<LocationSelectionAdaptor.ViewHolder> {

    private ArrayList<WModel> list;
    private ClickLocationListener listener;

    public LocationSelectionAdaptor(ClickLocationListener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<WModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_auto_matching_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//        holder.location.setText(list.get(position).getLocation());
        holder.location.setOnClickListener(v -> listener.run(list.get(position)));
    }

    @Override
    public int getItemCount() {
        if (list == null) return 0;
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView location;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            location = itemView.findViewById(R.id.txtPas3PO);
        }
    }
}
