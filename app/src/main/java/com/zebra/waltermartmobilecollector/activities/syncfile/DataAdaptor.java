package com.zebra.waltermartmobilecollector.activities.syncfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;

import java.util.ArrayList;

public class DataAdaptor extends RecyclerView.Adapter<DataAdaptor.ViewHolder> {

    private ArrayList<Model> list = new ArrayList<>();
    private Context context;

    public DataAdaptor(Context context) {
        this.context = context;
    }

    public void init() throws Exception {
        fetchPO();
        fetchPCount();
        fetchRTV();
        fetchStockCount();

        new Handler(Looper.getMainLooper()).post(() -> {
            if (list.size() == 0)
                ((Activity) context).findViewById(R.id.syncFileRecyclerView).setVisibility(View.GONE);
            else
                ((Activity) context).findViewById(R.id.emptyFiles).setVisibility(View.GONE);
            ((TextView) ((Activity) context).findViewById(R.id.outdated)).setText("Outdated Files (" + list.size() + ")");
            notifyDataSetChanged();
        });
    }

    private void fetchPO() throws Exception{
        ArrayList<String> filenames = FTP.getFilenames(Folders.SCANNED_PO);
        for (Model po : FileService.getFilenames(Folders.PO)) {
            list.add(po);

            String[] spl = po.getFilename().split("_");
            if (spl.length != 3 || !spl[1].equals("0")) {
                if (filenames.contains(po.getFilename()))
                    po.setExisting();
                continue;
            }

            int[] number = getPassNumber(filenames, spl[0], spl[2]);

            if (number[0] == 1) {
                po.setNewFilename(spl[0] + "_" + number[1] + "_" + spl[2]);
                po.setExisting();
                continue;
            }

            po.setNewFilename(spl[0] + "_" + (number[1] + 1) + "_" + spl[2]);
        }
    }

    private void fetchPCount() throws Exception {
        ArrayList<String> filenames = FTP.getFilenames(Folders.SCANNED_PCOUNT);
        for (Model pcount : FileService.getFilenames(Folders.PCOUNT)) {
            list.add(pcount);

            String[] spl = pcount.getFilename().split("_");
            if (spl.length != 3 || !spl[1].equals("0")) {
                if (filenames.contains(pcount.getFilename()))
                    pcount.setExisting();
                continue;
            }

            int[] number = getPassNumber(filenames, spl[0], spl[2]);

            if (number[0] == 1) {
                pcount.setNewFilename(spl[0] + "_" + number[1] + "_" + spl[2]);
                pcount.setExisting();
                continue;
            }

            pcount.setNewFilename(spl[0] + "_" + (number[1] + 1) + "_" + spl[2]);
        }
    }

    private void fetchRTV() throws Exception {
        ArrayList<String> filenames = FTP.getFilenames(Folders.SCANNED_RTV);
        for (Model rtv : FileService.getFilenames(Folders.RTV)) {
            list.add(rtv);
            if (filenames.contains(rtv.getFilename()))
                rtv.setExisting();
        }
    }

    private void fetchStockCount() throws Exception {
//        ArrayList<String> filenames = FTP.getFilenames(Folders.Sca);
//        for (Model st : FileService.getFilenames(Folders.STORE_TRANSFER)) {
//            list.add(st);
//            if (filenames.contains(st.getFilename()))
//                st.setExisting();
//        }
    }

    public boolean hasExisting() {
        for (Model model : list)
            if (model.isExisting()) return true;

        return false;
    }

    private int[] getPassNumber(ArrayList<String> filenames, String f, String l) {
        int lastNumber = 0;
        for (String filename : filenames) {
            String[] spl = filename.split("_");
            if (spl.length != 3 || !spl[0].equals(f)) continue;

            if (spl[2].equals(l)) return new int[]{1, Integer.parseInt(spl[1])};

            if (spl[1].equals("2"))
                lastNumber = 2;
            else if (spl[1].equals("1") && lastNumber == 0)
                lastNumber = 1;
        }

        return new int[]{0, lastNumber};
    }

    public ArrayList<Model> getList() {
        return list;
    }

    public void clearList() {
        list.clear();
        ((TextView) ((Activity) context).findViewById(R.id.outdated)).setText("Outdated Files (" + list.size() + ")");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_syncfile_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Model model = list.get(position);

        holder.filepath.setText(model.getFolder() + model.getNewFilename());

        holder.sync.setOnClickListener(v -> {
            if (model.isExisting())
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Confirmation")
                        .setMessage("Are your sure you want to override this file in FTP server?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", (dialogInterface, i) -> uploadToFTP(model, position))
                        .show();
            else
                uploadToFTP(model, position);
        });
        holder.archive.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to archive this?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", (dialogInterface, i) -> archive(model, position))
                        .show()
        );
    }

    private void archive(Model model, int position) {
        String err = FileService.archive(model);
        if (err != null) {
            Helper.showError(err);
            return;
        }

        list.remove(position);
        ((TextView) ((Activity) context).findViewById(R.id.outdated)).setText("Outdated Files (" + list.size() + ")");

        if (list.size() == 0) {
            ((Activity) context).findViewById(R.id.syncFileRecyclerView).setVisibility(View.GONE);
            ((Activity) context).findViewById(R.id.emptyFiles).setVisibility(View.VISIBLE);
        }

        Helper.showSuccess("Successfully archived file.");

        notifyDataSetChanged();
    }

    private void uploadToFTP(Model model, int position) {
        Helper.showLoading();
        new Thread(() -> {
            try {
                FTP.login();
                FTP.uploadFile(
                        model.getFolder() + model.getFilename(),
                        Folders.FTP_MASTER_FOLDER + model.getFolder() + model.getNewFilename()
                );

                list.remove(position);

                new Handler(Looper.getMainLooper()).post(() -> {
                    ((TextView) ((Activity) context).findViewById(R.id.outdated)).setText("Outdated Files (" + list.size() + ")");
                    Helper.closeLoading();

                    String err = FileService.archive(model);
                    if (err != null)
                        Helper.showError(err);

                    if (list.size() == 0) {
                        ((Activity) context).findViewById(R.id.syncFileRecyclerView).setVisibility(View.GONE);
                        ((Activity) context).findViewById(R.id.emptyFiles).setVisibility(View.VISIBLE);
                    }

                    Helper.showSuccess("Successfully synced file.");

                    notifyDataSetChanged();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Helper.showError(e.getMessage()));
            } finally {
                try {
                    FTP.disconnect();
                } catch (Exception e) {
                }
                new Handler(Looper.getMainLooper()).post(() -> Helper.closeLoading());
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView filepath;
        Button sync, archive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            filepath = itemView.findViewById(R.id.filepath);
            sync = itemView.findViewById(R.id.btnSync);
            archive = itemView.findViewById(R.id.btnArchive);
        }
    }
}


//            for (Model po : FileService.getFilenames(Folders.PO)) {
//                String[] spl = po.getFilename().split("_");
//                if (spl.length != 3 || !spl[1].equals("0")) {
//                    if (!filenames.contains(po.getFilename()))
//                        list.add(po);
//                    else
//                        po.setExisting();
////                        FileService.archive(po);
//                    continue;
//                }
//
//                int[] number = getPassNumber(filenames, spl[0], spl[2]);
//
//                if (number[0] == 1) {
//                    po.setNewFilename(spl[0] + "_" + number[1] + "_" + spl[2]);
//                    FileService.archive(po);
//                    continue;
//                }
//
//                po.setNewFilename(spl[0] + "_" + (number[1] + 1) + "_" + spl[2]);
//                list.add(po);


////////////RTV////////////

//                if (!filenames.contains(rtv.getFilename()))
//                        list.add(rtv);
//                        else
//                        FileService.archive(rtv);
