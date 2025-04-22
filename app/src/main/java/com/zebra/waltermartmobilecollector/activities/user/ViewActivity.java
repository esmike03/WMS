package com.zebra.waltermartmobilecollector.activities.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.UserService;

public class ViewActivity extends BaseActivity {

    private DataAdaptor adaptor;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new DataAdaptor();
        recyclerView.setAdapter(adaptor);

        if (Globals.userIsNormalUser())
            findViewById(R.id.btnImport).setVisibility(View.GONE);
    }

    public void onExport(View _) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to export user masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    FTP.upload(
                            Folders.MASTERFILES + "DCUSERMST.txt",
                            UserService.getForExport()
                    );

                    showSuccessInThread("Successfully exported.");
                }))
                .show();
    }

    public void onImport(View _) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to import user masterfile?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    try {
                        first = true;
                        FTP.download(
                                Folders.MASTERFILES,
                                "DCUSERMST.txt",
                                "insert into users (name,username,password,role) select ?,?,?,? where not exists (select 1 from users where name=? or username=? limit 1)",
                                (statement, rows) -> {
                                    if (first){
                                        first = false;
                                        return false;
                                    }

                                    if (!rows.get(0).matches("[a-zA-Z0-9 ]+")) return false;

                                    String role = rows.get(3);
                                    if (!role.equals("User") && !role.equals("Admin"))
                                        role = "User";

                                    statement.bindString(1, rows.get(0));
                                    statement.bindString(2, rows.get(1));
                                    statement.bindString(3, rows.get(2));
                                    statement.bindString(4, role);
                                    statement.bindString(5, rows.get(0));
                                    statement.bindString(6, rows.get(1));
                                    return true;
                                }
                        );

                        runOnUiThread(() -> {
                            adaptor.refresh();
                            showSuccess("User Masterfile successfully updated.");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("DCUSERMST.txt File not found or the format is invalid!!!");
                    }
                }))
                .show();
    }

    public void onNew(View v) {
        Globals.selectedUser = null;
        startActivity(new Intent(this, ManageActivity.class));
        finish();
    }

    public void onEdit(View v) {
        if (Globals.selectedUser == null) {
            showError("No selected user to edit!!!");
            return;
        }

        startActivity(new Intent(this, ManageActivity.class));
        finish();
    }

    public void onDelete(View v) {
        if (Globals.selectedUser == null) {
            showError("No selected user to delete!!!");
            return;
        }

        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are your sure you want to delete this user?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Globals.db.delete("users", "id=?", new String[]{Globals.selectedUser.getId()});
                    Globals.selectedUser = null;
                    adaptor.refresh();
                    showSuccess("Successfully deleted.");
                })
                .show();
    }

}