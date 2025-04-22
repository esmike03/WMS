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
    }
}