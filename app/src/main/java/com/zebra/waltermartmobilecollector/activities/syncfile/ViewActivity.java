package com.zebra.waltermartmobilecollector.activities.syncfile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.MainActivity;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;

public class ViewActivity extends BaseActivity {

    private DataAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syncfile_view);

        RecyclerView recyclerView = findViewById(R.id.syncFileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new DataAdaptor(this);
        recyclerView.setAdapter(adaptor);

        runThread(() -> {
            adaptor.init();
        });
    }

    public void onArchiveAll(View v) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to archive all of this?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> archiveAll())
                .show();
    }

    private void archiveAll() {
        if (adaptor.getItemCount() == 0) {
            showError("No files to archive!!!");
            return;
        }

        String error = null;
        while (adaptor.getItemCount() > 0){
            error = FileService.archive(adaptor.getList().get(0));
            if (error != null) break;
            else adaptor.getList().remove(0);
        }

        ((TextView) findViewById(R.id.outdated)).setText("Outdated Files (" + adaptor.getItemCount() + ")");

        if (adaptor.getItemCount() == 0) {
            findViewById(R.id.syncFileRecyclerView).setVisibility(View.GONE);
            findViewById(R.id.emptyFiles).setVisibility(View.VISIBLE);
        }

        if (error == null)
            showSuccess("Successfully archived files.");
        else showError(error);
    }

    public void onSyncAll(View v) {
        if (adaptor.getItemCount() == 0) {
            showError("No files to sync!!!");
            return;
        }

        if (!adaptor.hasExisting()) {
            runThread(() -> sync(0));
            return;
        }

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are your sure you want to override this files in FTP server?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> runThread(() -> sync(0)))
                .show();
    }

    private void sync(int pos) throws Exception {
        Model model = adaptor.getList().get(pos);
        FTP.uploadFile(
                model.getFolder() + model.getFilename(),
                Folders.FTP_MASTER_FOLDER + model.getFolder() + model.getNewFilename()
        );

        String err = FileService.archive(model);
        if (err != null)
            runOnUiThread(() -> Helper.showError(err));

        if (pos < adaptor.getItemCount() - 1) {
            sync(pos + 1);
            return;
        }

        runOnUiThread(() -> {
            findViewById(R.id.syncFileRecyclerView).setVisibility(View.GONE);
            findViewById(R.id.emptyFiles).setVisibility(View.VISIBLE);
            adaptor.clearList();

            if (err == null)
                showSuccess("Successfully synced files.");
        });
    }

    public void onBack(View v) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}