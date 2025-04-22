package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.OfflineActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.reports.ListActivity;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.UserService;

public class POActivity extends BaseActivity {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po);

        BaseActivity.backInto = POActivity.class;

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnUpdate).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);

        if (Globals.userIsNormalUser()) {
            findViewById(R.id.btnAM).setVisibility(View.GONE);
            findViewById(R.id.btnReport).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.btnAM).setBackgroundColor(Color.TRANSPARENT);
            findViewById(R.id.btnReport).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void onReport(View _){
        View view = getLayoutInflater().inflate(R.layout.dialog_report_type, null);
        AlertDialog dialog = new AlertDialog
                .Builder(this)
                .setView(view)
                .create();
        view.findViewById(R.id.btnMatched).setOnClickListener(v -> onNavigateReport(dialog, true));
        view.findViewById(R.id.btnUnmatched).setOnClickListener(v -> onNavigateReport(dialog, false));

        dialog.show();
    }

    private void onNavigateReport(AlertDialog dialog, boolean matched){
        if (!Globals.isWMS() && !matched){
            showError("No Unmatched in WDS!!!");
            return;
        }

        Globals.reportMatch = matched;
        dialog.dismiss();
        startActivity(new Intent(
                POActivity.this,
                ListActivity.class
        ));
        finish();
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
        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    public void onAM(View vv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_automatching_type, null);
        AlertDialog amDialog = new AlertDialog
                .Builder(this)
                .setView(view)
                .create();
        view.findViewById(R.id.btnOffline).setOnClickListener(v -> {
            amDialog.dismiss();
            startActivity(new Intent(POActivity.this, OfflineActivity.class));
            finish();
        });
        view.findViewById(R.id.btnOnline).setOnClickListener(v -> {
            amDialog.dismiss();
            startActivity(new Intent(POActivity.this, com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.ListActivity.class));
            finish();
        });

        amDialog.show();
    }

    public void onSend(View v) {
        if (!Service.hasScanned()) {
            showError("No data to send!!!");
            return;
        }

        Helper.showLoading();
        new Thread(() -> {
            try {
                Service.sendToFTP(getCacheDir());
                showSuccessInThread("Successfully uploaded.");
            } catch (Exception e) {
                showSuccessInThread("Saved as pending for ftp.");
            } finally {
                try {
                    FTP.disconnect();
                } catch (Exception e) {
                }
                runOnUiThread(() -> Helper.closeLoading());
            }
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
                        Service.clearData();

                        FTP.download(
                                Folders.MASTERFILES,
                                "DCPOMST.txt",
                                "insert into pos (po,sku,qty,factor) values (?,?,?,?)",
                                (statement, rows) -> {
                                    for (int i = 0; i < 4; i++)
                                        statement.bindString(i + 1, rows.get(i));
                                    return true;
                                }
                        );

                        showSuccessInThread("PO Masterfile successfully updated.");
                    } catch (Exception _) {
                        throw new Exception("DCPOMST.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
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

}