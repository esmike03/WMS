package com.zebra.waltermartmobilecollector.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.SettingsService;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- DEFAULT FTP SETTINGS ---
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

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
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

            try {
                SettingsService.update(
                        !wds.isChecked(),
                        st,
                        ipadd,
                        u,
                        p
                );
                showSuccess("Settings successfully updated.");
            } catch (Exception _) {
                showError("Error updating settings!!!");
            }
        });

        // --- MMS FTP SETTINGS ---
        EditText mmsIp = findViewById(R.id.edtTxtMMSIP);
        EditText mmsUser = findViewById(R.id.edtTxtMMSUser);
        EditText mmsPassword = findViewById(R.id.edtTxtMMSPassword);

        // Pre-fill existing MMS values if already set
        if (Globals.getMmsIpAddress() != null)
            mmsIp.setText(Globals.getMmsIpAddress());
        if (Globals.getMmsFtpUser() != null)
            mmsUser.setText(Globals.getMmsFtpUser());
        if (Globals.getMmsFtpPassword() != null)
            mmsPassword.setText(Globals.getMmsFtpPassword());

        findViewById(R.id.btnUpdateMMS).setOnClickListener(v -> {
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
                SettingsService.updateMMS(
                        mmsIpVal,
                        mmsUserVal,
                        mmsPasswordVal
                );
                showSuccess("MMS Settings successfully updated.");
            } catch (Exception _) {
                showError("Error updating MMS settings!!!");
            }
        });
    }
}