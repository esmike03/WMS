package com.zebra.waltermartmobilecollector.activities.store_transfer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StoreTransferActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_transfer);

        BaseActivity.backInto = StoreTransferActivity.class;

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnUpdate).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);
    }

    public void onClear(View v) {
        new AlertDialog
                .Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to clear scanned data?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Service.clearScans();
                    showSuccess("Successfully cleared scanned items.");
                })
                .show();
    }

    public void onView(View v) {
        startActivity(new Intent(this, ViewActivity.class));
        finish();
    }

    public void onScan(View v) {
        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSend(View v) {
        new AlertDialog
                .Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to send this scanned data?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    String fileData = Service.getDataToSendToFTP();
                    if (fileData.isEmpty()) {
                        showError("No data to send!!!");
                        return;
                    }

                    uploadToFTP(
                            "TRF_" + DateTimeFormatter.ofPattern("MMddyyyyHHmmss").format(LocalDateTime.now()) + ".txt",
                            fileData
                    );
                })
                .show();
    }

    private void uploadToFTP(String filename, String fileData) {
        runThread(()->{
            try {
                FTP.upload(Folders.SCANNED_STORE_TRANSFER + filename, fileData);
            } catch (Exception e){
                downloadToLocal(filename, fileData, Folders.STORE_TRANSFER, e.getMessage());
                return;
            }

            downloadToLocal(filename, fileData, Folders.ARCHIVE + Folders.STORE_TRANSFER, null);
            showSuccessInThread("Successfully uploaded.");
        });
    }

    private void downloadToLocal(String filename, String fileData, String folder, String error) {
        try {
            FileService.download(
                    filename,
                    folder,
                    fileData
            );

            runOnUiThread(() -> {
                Helper.closeLoading();

                String mess = "archived file.";
                if (error != null) {
                    showError(error);
                    mess = "back up file.";
                }
                showSuccess("Successfully " + mess);
            });
        } catch (Exception e){
            new Handler(Looper.getMainLooper()).post(()-> {
                Helper.closeLoading();
                showError((error == null ? "" : error + " and ") + e.getMessage());
            });
        }
    }
}