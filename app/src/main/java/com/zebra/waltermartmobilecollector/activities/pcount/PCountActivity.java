package com.zebra.waltermartmobilecollector.activities.pcount;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.pcount.auto_matching.ListActivity;
import com.zebra.waltermartmobilecollector.activities.pcount.withdrawal.LocationSelectionActivity;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.UserService;

public class PCountActivity extends BaseActivity {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcount);

        BaseActivity.backInto = PCountActivity.class;

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnDashboard).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnWithdrawal).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnAM).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnReport).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnUpdateLocation).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnUpdateParentChild).setBackgroundColor(Color.TRANSPARENT);
    }

    public void onUpdateLocation(View v) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to update location masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    try {
                        Service.clearLocations();

                        FTP.download(
                                Folders.MASTERFILES,
                                "LOCMST.txt",
                                "insert into locations (name) values (?)",
                                (statement, rows) -> {
                                    String location = rows.get(0);
                                    if (Character.isLetter(location.charAt(0)) && location.matches("[a-zA-Z0-9]+"))
                                        statement.bindString(1, location);
                                    return true;
                                }
                        );

                        showSuccessInThread("Location Masterfile successfully updated.");
                    } catch (Exception _) {
                        throw new Exception("LOCMST.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
    }

    public void onUpdateParentChild(View v) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to update parent child masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    try {
                        Service.clearParentChilds();

                        FTP.download(
                                Folders.MASTERFILES,
                                "DCINVSET.txt",
                                "insert into pcount_parent_childs (parent_id,child_id,qty) values (?,?,?)",
                                (statement, rows) -> {
                                    statement.bindString(1, rows.get(0));
                                    statement.bindString(2, rows.get(1));
                                    statement.bindString(3, Helper.convertToIntAndRemoveDot(rows.get(2)) + "");
                                    return true;
                                }
                        );

                        showSuccessInThread("Parent Child Masterfile successfully updated.");
                    } catch (Exception _) {
                        throw new Exception("DCINVSET.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
    }

    public void onReport(View _) {
        startActivity(new Intent(this, PcountReportActivity.class));
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

    public void onWithdrawal(View _) {
        startActivity(new Intent(this, LocationSelectionActivity.class));
        finish();
    }

    public void onDashboard(View _) {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    public void onAM(View _) {
        chooseScanningKindAndNavigate(ListActivity.class);
    }

    public void onView(View v) {
        startActivity(new Intent(this, ViewActivity.class));
        finish();
    }

    private void chooseScanningKindAndNavigate(Class<?> activityToNavigate) {
        View view = getLayoutInflater().inflate(R.layout.dialog_stock_count_type, null);
        AlertDialog dg = new AlertDialog
                .Builder(this)
                .setView(view)
                .create();
        ((TextView) view.findViewById(R.id.textView19)).setText("PCount Type");
        navigate(view.findViewById(R.id.btnContinues), dg, "1", activityToNavigate);
        navigate(view.findViewById(R.id.btnPerPiece), dg, "2", activityToNavigate);
        navigate(view.findViewById(R.id.btnPerCase), dg, "3", activityToNavigate);
        dg.show();
    }

    private void navigate(Button btn, AlertDialog dg, String option, Class<?> act) {
        btn.setOnClickListener(vv -> {
            dg.dismiss();
            Globals.stockCountOption = option;
            if (!option.equals("2")) {
                Globals.singleValidation = option.equals("1");
                startActivity(new Intent(this, act));
                finish();
                return;
            }

            View view = getLayoutInflater().inflate(R.layout.pass_selection_dialog, null);
            AlertDialog dgt = new AlertDialog
                    .Builder(this)
                    .setView(view)
                    .create();
            view.findViewById(R.id.perPiece).setOnClickListener(v -> {
                Globals.singleValidation = true;
                dgt.dismiss();
                startActivity(new Intent(this, act));
                finish();
            });
            view.findViewById(R.id.perCase).setOnClickListener(v -> {
                Globals.singleValidation = false;
                dgt.dismiss();
                startActivity(new Intent(this, act));
                finish();
            });
            dgt.show();
        });
    }

    public void onScan(View _) {
        chooseScanningKindAndNavigate(ScanLocationActivity.class);
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
                e.printStackTrace();
                Log.d(TAG, "onSend: " + e.getMessage());
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