package com.zebra.waltermartmobilecollector.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zebra.waltermartmobilecollector.BuildConfig;
import com.zebra.waltermartmobilecollector.DB;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.SerialHelper;
import com.zebra.waltermartmobilecollector.SerialNumbers;
import com.zebra.waltermartmobilecollector.services.Barcode;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.SettingsService;
import com.zebra.waltermartmobilecollector.services.UserService;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Globals.db == null) {
            String serial = SerialHelper.get(getContentResolver());
            Log.d("TAG", "onCreate: " + serial);
            if (serial == null || !SerialNumbers.has(serial)) {
                finish();
                return;
            }

            Globals.db = new DB(this).getWritableDatabase();

            Barcode.createProfile(this, getResources().getString(R.string.app_name));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

//        go();

        EditText username = findViewById(R.id.edtTxtUsername);
        EditText password = findViewById(R.id.edtTxtPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String uname = username.getText().toString().trim();
            if (uname.isEmpty()) {
                username.setError("This is required");
                return;
            }
            String pass = password.getText().toString().trim();
            if (pass.isEmpty()) {
                password.setError("This is required");
                return;
            }

            try {
                pass = Encryptor.encrypt(pass);
            } catch (Exception e) {
                Helper.showError(e.getMessage());
                return;
            }

            if (!UserService.login(uname, pass)) {
                Helper.setErrorDialog(this);
                Helper.showError("Incorrect credentials!!!");
                return;
            }

            SettingsService.fetchMasterfileUpdatedAt();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void go() {
        Globals.userId = "1";
        Globals.name = "Super Admin";
        Globals.username = "superadmin";
        Globals.setUserRole("Super Admin");

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}