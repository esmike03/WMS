package com.zebra.waltermartmobilecollector.activities.user;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.UserService;

import java.util.ArrayList;

public class DataAdaptor extends RecyclerView.Adapter<DataAdaptor.ViewHolder> {

    private ArrayList<Model> list;

    public DataAdaptor() {
        refresh();
    }

    public void refresh(){
        list = UserService.get();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_rv_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model user = list.get(position);

        holder.name.setText(user.getName());
        holder.username.setText(user.getUsername());
        holder.role.setText(user.getRole());

        holder.layout.setOnClickListener(v->{
            Globals.selectedUser = Globals.selectedUser == null || !Globals.selectedUser.getId().equals(user.getId())
                    ? user
                    : null;
            notifyDataSetChanged();
        });

        holder.layout.setCardBackgroundColor(Globals.selectedUser != null && Globals.selectedUser.getId().equals(user.getId()) ? Color.LTGRAY : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, username, role;
        CardView layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.desc);
            role = itemView.findViewById(R.id.role);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
