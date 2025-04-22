package com.zebra.waltermartmobilecollector.activities.user;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.MainActivity;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.UserService;

public class UpdateProfileActivity extends BaseActivity {

    private EditText name, username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        name = findViewById(R.id.edtName);
        username = findViewById(R.id.edtUsername);
        password = findViewById(R.id.edtPassword);

        name.setText(Globals.name);
        username.setText(Globals.username);
    }

    public void save(View v) {
        String n = name.getText().toString().trim();
        if (n.isEmpty()) {
            name.setError("This is required");
            return;
        }
        String u = username.getText().toString().trim();
        if (u.isEmpty()) {
            username.setError("This is required");
            return;
        }
        if (!Globals.username.equals(u) && UserService.isUsernameExists(u)) {
            username.setError("This username is already exists");
            return;
        }
        String p = password.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put("name", n);
        values.put("username", u);
        if (!p.isEmpty()) {
            try{
                p = Encryptor.encrypt(p);
            } catch (Exception e){
                showError(e.getMessage());
                return;
            }
            values.put("password", p);
        }

        Globals.db.update("users", values, "id=?", new String[]{Globals.userId});

        Globals.name = n;
        Globals.username = u;
        Toast.makeText(this, "Successfully updated your profile.", Toast.LENGTH_LONG).show();
        cancel(null);
    }

    public void cancel(View v) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}