package com.zebra.waltermartmobilecollector.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.SettingsService;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- FTP SETTINGS ---
        EditText store = findViewById(R.id.edtTxtStore);
        EditText ip = findViewById(R.id.edtTxtIP);
        EditText user = findViewById(R.id.edtTxtFTPUser);
        EditText password = findViewById(R.id.edtTxtFTPPassword);
        RadioButton wds = findViewById(R.id.rbWDS);

        if (!Globals.isWMS())
            wds.setChecked(true);
        store.setText(Globals.getLocalStoreCode());
        ip.setText(Globals.getIpAddress());
        user.setText(Globals.getFtpUser());
        password.setText(Globals.getFtpPassword());

        // --- MMS FTP SETTINGS ---
        EditText mmsIp = findViewById(R.id.edtTxtMMSIP);
        EditText mmsUser = findViewById(R.id.edtTxtMMSUser);
        EditText mmsPassword = findViewById(R.id.edtTxtMMSPassword);

        if (Globals.getMmsIpAddress() != null)
            mmsIp.setText(Globals.getMmsIpAddress());
        if (Globals.getMmsFtpUser() != null)
            mmsUser.setText(Globals.getMmsFtpUser());
        if (Globals.getMmsFtpPassword() != null)
            mmsPassword.setText(Globals.getMmsFtpPassword());

        // --- SAVE ALL BUTTON ---
        findViewById(R.id.btnSaveAll).setOnClickListener(v -> {
            // Validate FTP fields
            String st = store.getText().toString().trim();
            if (st.isEmpty()) {
                store.setError("This is required");
                return;
            }
            String ipadd = ip.getText().toString().trim();
            if (ipadd.isEmpty()) {
                ip.setError("This is required");
                return;
            }
            String u = user.getText().toString().trim();
            if (u.isEmpty()) {
                user.setError("This is required");
                return;
            }
            String p = password.getText().toString().trim();
            if (p.isEmpty()) {
                password.setError("This is required");
                return;
            }

            // Validate MMS fields
            String mmsIpVal = mmsIp.getText().toString().trim();
            if (mmsIpVal.isEmpty()) {
                mmsIp.setError("This is required");
                return;
            }
            String mmsUserVal = mmsUser.getText().toString().trim();
            if (mmsUserVal.isEmpty()) {
                mmsUser.setError("This is required");
                return;
            }
            String mmsPasswordVal = mmsPassword.getText().toString().trim();
            if (mmsPasswordVal.isEmpty()) {
                mmsPassword.setError("This is required");
                return;
            }

            try {
                SettingsService.update(!wds.isChecked(), st, ipadd, u, p);
                SettingsService.updateMMS(mmsIpVal, mmsUserVal, mmsPasswordVal);
                showSuccess("All settings successfully updated.");
            } catch (Exception _) {
                showError("Error updating settings!!!");
            }
        });

        // --- TEST CONNECTION BUTTON ---
        findViewById(R.id.btnCheckConnection).setOnClickListener(v -> {
            // Use currently typed values for testing even before saving
            Globals.setMmsSettings(
                    mmsIp.getText().toString().trim(),
                    mmsUser.getText().toString().trim(),
                    mmsPassword.getText().toString().trim()
            );

            runThread(() -> {
                // Test FTP1
                try {
                    FTP.checkConnection();
                    showSuccessInThread("FTP connection is ok.");
                } catch (Exception e) {
                    showErrorInThread("FTP failed: " + e.getMessage());
                } finally {
                    FTP.disconnect();
                }

                // Test MMS
                try {
                    FTP.loginMMS();
                    showSuccessInThread("MMS connection is ok.");
                } catch (Exception e) {
                    showErrorInThread("MMS failed: " + e.getMessage());
                } finally {
                    FTP.disconnectMMS();
                }
            });
        });
    }
}