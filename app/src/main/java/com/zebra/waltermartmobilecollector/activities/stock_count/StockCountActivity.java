package com.zebra.waltermartmobilecollector.activities.stock_count;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.UserService;

public class StockCountActivity extends BaseActivity {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count);

        BaseActivity.backInto = StockCountActivity.class;

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);
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

    public void onView(View v) {
        startActivity(new Intent(this, ViewActivity.class));
        finish();
    }

    public void onScan(View _) {
        View view = getLayoutInflater().inflate(R.layout.dialog_stock_count_type, null);
        AlertDialog dg = new AlertDialog
                .Builder(this)
                .setView(view)
                .create();
        navigateScan(view.findViewById(R.id.btnContinues), dg, "1");
        navigateScan(view.findViewById(R.id.btnPerPiece), dg, "2");
        navigateScan(view.findViewById(R.id.btnPerCase), dg, "3");
        dg.show();
    }

    private void navigateScan(Button btn, AlertDialog dg, String option) {
        btn.setOnClickListener(v -> {
            dg.dismiss();
            Globals.stockCountOption = option;
            startActivity(new Intent(this, ScanLocationActivity.class));
            finish();
        });
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
}