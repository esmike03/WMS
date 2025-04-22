package com.zebra.waltermartmobilecollector.activities.user;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.Encryptor;
import com.zebra.waltermartmobilecollector.services.UserService;

public class ManageActivity extends BaseActivity {

    private EditText name, username, password;
    private Spinner role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        name = findViewById(R.id.edtName);
        username = findViewById(R.id.edtUsername);
        password = findViewById(R.id.edtPassword);
        if (Globals.userIsSuperAdmin()) {
            role = findViewById(R.id.spinnerRole);
            role.setAdapter(new ArrayAdapter<>(
                    getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{
                            "User",
                            "Admin",
                            "Super Admin"
                    }
            ));
        } else findViewById(R.id.spinnerRole).setVisibility(View.GONE);

        if (Globals.selectedUser == null) return;

        ((TextView) findViewById(R.id.txtTitle)).setText("Edit User");

        name.setText(Globals.selectedUser.getName());
        username.setText(Globals.selectedUser.getUsername());
        if (!Globals.userIsSuperAdmin()) return;
        if (Globals.selectedUser.getRole().equals("Admin"))
            role.setSelection(1);
        else if (Globals.selectedUser.getRole().equals("Super Admin"))
            role.setSelection(2);
    }

    public void save(View v) {
        String n = name.getText().toString().trim();
        if (n.isEmpty()) {
            name.setError("This is required");
            return;
        }
        if (!n.matches("[a-zA-Z0-9 ]+")){
            name.setError("Invalid name!!!");
            return;
        }
        String u = username.getText().toString().trim();
        if (u.isEmpty()) {
            username.setError("This is required");
            return;
        }
        if (((Globals.selectedUser != null
                && !Globals.selectedUser.getUsername().equals(u))
                || Globals.selectedUser == null)
                && UserService.isUsernameExists(u)) {
            username.setError("This username is already exists");
            return;
        }
        String p = password.getText().toString().trim();
        if (Globals.selectedUser == null && p.isEmpty()) {
            password.setError("This is required");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name", n);
        values.put("username", u);
        if (!p.isEmpty()) {
            try {
                p = Encryptor.encrypt(p);
            } catch (Exception e) {
                showError(e.getMessage());
                return;
            }
            values.put("password", p);
        }
        values.put(
                "role",
                Globals.userIsSuperAdmin()
                        ? role.getSelectedItem().toString()
                        : "User"
        );

        if (Globals.selectedUser != null) {
            Globals.db.update("users", values, "id=?", new String[]{Globals.selectedUser.getId()});

            Toast.makeText(this, "Successfully saved.", Toast.LENGTH_LONG).show();
            cancel(null);
            return;
        }

        Globals.db.insert("users", null, values);

        name.setText("");
        username.setText("");
        password.setText("");
        if (Globals.userIsSuperAdmin())
            role.setSelection(0);

        Toast.makeText(this, "Successfully saved.", Toast.LENGTH_LONG).show();
        cancel(null);
    }

    public void cancel(View v) {
        Globals.selectedUser = null;
        startActivity(new Intent(this, ViewActivity.class));
        finish();
    }
}