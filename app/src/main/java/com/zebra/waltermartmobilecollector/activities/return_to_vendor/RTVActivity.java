package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;
import com.zebra.waltermartmobilecollector.services.ReasonService;
import com.zebra.waltermartmobilecollector.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RTVActivity extends BaseActivity {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtv);

        BaseActivity.backInto = RTVActivity.class;

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnUpdate).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);

        if (Globals.ftpStoreCode != null) return;

        runThread(() -> FTP.fetchStoreCode());
    }

    public void onClear(View v) {
        new AlertDialog
                .Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to clear scanned data?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    if (!Globals.userIsNormalUser()) {
                        Service.clearScans();
                        showSuccess("Successfully cleared scanned items.");
                        return;
                    }
                    createDialog();
                    dialog.show();
                })
                .show();
    }

    public void onView(View v) {
        startActivity(new Intent(this, ViewActivity.class));
        finish();
    }

    public void onScan(View v) {
        if (!ReasonService.hasCodes()) {
            showError("No imported reasons yet, Please import first!!!");
            return;
        }

        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    private void createDialog() {
        if (dialog != null) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_password_confirmation, null);
        dialog = new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setView(view)
                .create();
        EditText username = view.findViewById(R.id.edtTxtConfirmationUsername);
        EditText password = view.findViewById(R.id.edtTxtConfirmationPassword);
        dialog.setOnShowListener(dialogInterface -> username.requestFocus());
        view.findViewById(R.id.btnConfirmationCancel).setOnClickListener(v -> {
            username.setText("");
            password.setText("");
            dialog.dismiss();
        });
        view.findViewById(R.id.btnConfirmationOk).setOnClickListener(v -> {
            String u = username.getText().toString();
            if (u.isEmpty()) {
                username.setError("This is required");
                username.requestFocus();
                return;
            }
            String p = password.getText().toString();
            if (p.isEmpty()) {
                password.setError("This is required");
                password.requestFocus();
                return;
            }

            try {
                p = Encryptor.encrypt(p);
            } catch (Exception e) {
                showError(e.getMessage());
                return;
            }

            if (!UserService.confirm(u, p)) {
                showError("Incorrect credentials!!!");
                return;
            }

            username.setText("");
            password.setText("");

            Service.clearScans();
            dialog.dismiss();
            showSuccess("Successfully cleared scanned items.");
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSend(View v) {
        new AlertDialog
                .Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to send this scanned data?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Cursor fileData = Service.getDataToSendToFTP();
                    if (fileData.getCount() < 1) {
                        showError("No data to send!!!");
                        return;
                    }

                    uploadToFTP(
                            fileData
                    );
                })
                .show();
    }

    private void uploadToFTP(Cursor c) {
        Helper.showLoading();
        String datetime;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            datetime = DateTimeFormatter.ofPattern("MMddyyyyHHmmss").format(LocalDateTime.now());
        else {
            datetime = "";
        }
        String filename = "RTV_" + datetime + ".txt";
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer strBuffer = new StringBuffer();
        StringBuffer mainBuffer = new StringBuffer();
        new Thread(() -> {
            try {
                int totalPcs = 0, totalSku = 0, i = 0;
                while (c.moveToNext()) {
                    totalPcs += c.getInt(2);
                    i++;
                    totalSku++;

                    strBuffer
                            .append(Globals.getStoreCode() + ",")
                            .append(c.getString(0) + ",")
                            .append(c.getString(1) + ",")
                            .append(c.getString(2) + ",")
                            .append(c.getString(3) + "\n");

                    stringBuffer
                            .append(i).append(",")
                            .append(c.getString(1)).append(",")
                            .append(c.getString(5)).append(",")
                            .append(c.getString(3)).append(",")
                            .append(c.getString(6)).append(",")
                            .append(c.getString(2)).append("\n")
                            .append(",'").append(c.getString(4)).append("\n");
                }
                mainBuffer
                        .append("TOTAL SKU with COUNT : ").append(totalSku).append("\n")
                        .append("TOTAL PCS : ").append(totalPcs).append("\n")
                        .append("Item No,SKU,Description,Code,Reason,Qty\n")
                        .append(stringBuffer)
                        .append(",,,,SUB TOTAL :,")
                        .append(totalPcs);

                FTP.login();

                String folder = Folders.SCANNED_RTV;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    folder += DateTimeFormatter.ofPattern("MMddyyyy").format(LocalDateTime.now()) + "/";
                    FTP.getFtp().makeDirectory(folder);
                }

                FTP.upload(folder + "RTV_Report_" + datetime + ".csv", mainBuffer.toString());
                FTP.upload(folder + filename, strBuffer.toString());

//                FTP.upload(Folders.SCANNED_RTV + filename, fileData);
            } catch (Exception e) {
                downloadToLocal(filename, strBuffer.toString(), Folders.RTV, e.getMessage());
                return;
            } finally {
                try {
                    FTP.disconnect();
                } catch (Exception e) {
                }
                runOnUiThread(() -> Helper.closeLoading());
            }

            downloadToLocal(filename, strBuffer.toString(), Folders.ARCHIVE + Folders.RTV, null);
            Service.clearScans();
            showSuccessInThread("Successfully uploaded.");
        }).start();
    }

    public void onUpdate(View v) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to update masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    try {
                        ReasonService.clear();

                        FTP.download(
                                Folders.MASTERFILES,
                                "DCRTVRSN.txt",
                                "insert into reasons (code,description) select ?,? where not exists (select 1 from reasons where code=?)",
                                (statement, rows) -> {
                                    for (int i = 0; i < 2; i++)
                                        statement.bindString(i + 1, rows.get(i));

                                    statement.bindString(3, rows.get(0));

                                    return true;
                                }
                        );

                        showSuccessInThread("RTV Reason Code Masterfile successfully updated.");
                    } catch (Exception _) {
                        throw new Exception("DCRTVRSN.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
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
        } catch (Exception e) {
            runOnUiThread(() -> {
                Helper.closeLoading();
                showError((error == null ? "" : error + " and ") + e.getMessage());
            });
        }
    }

}