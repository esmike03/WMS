package com.zebra.waltermartmobilecollector.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.BuildConfig;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.pcount.PCountActivity;
import com.zebra.waltermartmobilecollector.activities.price_check.PriceCheckActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.POActivity;
import com.zebra.waltermartmobilecollector.activities.return_to_vendor.RTVActivity;
import com.zebra.waltermartmobilecollector.activities.stock_count.StockCountActivity;
import com.zebra.waltermartmobilecollector.activities.user.UpdateProfileActivity;
import com.zebra.waltermartmobilecollector.activities.user.ViewActivity;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.MainService;
import com.zebra.waltermartmobilecollector.services.SettingsService;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);

        setMasterfileUpdatedAt();

        setButton(findViewById(R.id.btnPO));
        setButton(findViewById(R.id.btnCheck));
        setButton(findViewById(R.id.btnDownload));
        setButton(findViewById(R.id.btnRTV));
        setButton(findViewById(R.id.btnSyncFile));
        setButton(findViewById(R.id.btnPC));
        setButton(findViewById(R.id.btnST));
        setButton(findViewById(R.id.btnPCK));
//        setButton(findViewById(R.id.btnReport));
        setButton(findViewById(R.id.btnStockCount));

        findViewById(R.id.btnPO).setOnClickListener(v -> {
            startActivity(new Intent(this, POActivity.class));
            finish();
        });
        findViewById(R.id.btnRTV).setOnClickListener(v -> {
            startActivity(new Intent(this, RTVActivity.class));
            finish();
        });

        if (!Globals.userIsNormalUser()) return;

        findViewById(R.id.btnDownload).setVisibility(View.GONE);
//        findViewById(R.id.btnReport).setVisibility(View.GONE);
    }

    private void setMasterfileUpdatedAt() {
        if (!Globals.masterfileUpdatedAt.isEmpty())
            ((TextView) findViewById(R.id.txtMasterfileUpdatedAt))
                    .setText("MF updated at " + Globals.masterfileUpdatedAt);
    }

    private void setButton(Button btn) {
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextColor(Color.BLACK);
        btn.setTextSize(13);
        btn.setElevation(0);
        btn.setBackground(null);
    }

    public void onPCount(View _) {
        startActivity(new Intent(this, PCountActivity.class));
        finish();
    }

    public void stockCount(View vv) {
        startActivity(new Intent(this, StockCountActivity.class));
        finish();
    }

    public void onST(View v) {
//        startActivity(new Intent(this, StoreTransferActivity.class));
//        finish();
    }

    public void onPriceCheck(View v) {
        startActivity(new Intent(this, PriceCheckActivity.class));
        finish();
    }

    public void onCheck(View v) {
        runThread(() -> {
            // Test FTP1
            try {
                FTP.checkConnection();
                if (FTP.isConnected()) {
                    showSuccessInThread("FTP connection is ok.");
                } else {
                    showErrorInThread("Unable to connect to FTP!!!");
                }
            } catch (Exception e) {
                showErrorInThread("FTP1 failed: " + e.getMessage());
            } finally {
                FTP.disconnect();
            }

            // Test MMS FTP2
            try {
                FTP.loginMMS();
                showSuccessInThread("MMS connection is ok.");
            } catch (Exception e) {
                showErrorInThread("MMS connection failed: " + e.getMessage());
            } finally {
                FTP.disconnectMMS();
            }
        });
    }

    public void syncFiles(View v) {
        startActivity(new Intent(this, com.zebra.waltermartmobilecollector.activities.syncfile.ViewActivity.class));
        finish();
    }

    public void onDownload(View v) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to update masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    try {
                        MainService.clear();

                        FTP.download(
                                Folders.MASTERFILES,
                                "DCINVMST.txt",
                                "insert into main " +
                                        "(sku,description,long_desc,short_desc,type,barcode,upc_type,vendor,dept,subdept,class,subclass,buyer,selling,buy,sell_buy,inner_pack,case_pack,manuf_list,cube,ucost,set_code,rep_code,event_no,event_type,regular_price,promo_price,start_date,end_date,deal_price_method,deal_price_qty,deal_price_amount,ols,cpo,dti) " +
                                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                (statement, rows) -> {
                                    for (int i = 0; i < 35; i++)
                                        statement.bindString(i + 1, rows.get(i));

                                    return true;
                                }
                        );

                        SettingsService.updateMasterfileUpdatedAt();

                        runOnUiThread(() -> {
                            setMasterfileUpdatedAt();
                            showSuccess("MC Masterfile successfully updated.");
                        });
                    } catch (Exception _) {
                        throw new Exception("DCINVMST.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bar, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!Globals.userIsNormalUser()) return super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_users).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

//    private void downloadProfile(){
//        try {
//            FileService.downloadDWProfile(getAssets().open(Globals.DW_PROFILE_FILENAME));
//            showSuccess("Successfully downloaded at /Download/" + Globals.DW_PROFILE_FILENAME);
//        } catch (Exception e) {
//            e.printStackTrace();
//            showError(e.getMessage());
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_update_profile:
                startActivity(new Intent(this, UpdateProfileActivity.class));
                finish();
                return true;
            case R.id.nav_users:
                startActivity(new Intent(this, ViewActivity.class));
                finish();
                return true;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            case R.id.nav_logout:
                Globals.name = null;
                Globals.userId = null;
                Globals.setUserRole(null);

                startActivity(new Intent(this, LoginActivity.class));
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}