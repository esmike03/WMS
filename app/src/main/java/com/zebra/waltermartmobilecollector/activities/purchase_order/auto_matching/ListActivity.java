package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.purchase_order.POActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.wds.WDSAutoMatchingActivity;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.HashMap;

public class ListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matching_list);

        backInto = POActivity.class;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdaptor adaptor = new ListAdaptor(model -> {
            Intent intent = new Intent(
                    ListActivity.this,
                    Globals.isWMS()
                            ? AutoMatchingActivity.class
                            : WDSAutoMatchingActivity.class
            );
            intent.putExtra("pas1_filename", model.getPas1());
            if (Globals.isWMS())
                intent.putExtra("pas2_filename", model.getPas2());
            intent.putExtra("po_number", model.getPo());
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(adaptor);

        if (Globals.isWMS())
            runThread(() -> {
                ArrayList<POFilename> list = new ArrayList<>();
                HashMap<String, POFilename> pos = new HashMap<>();
                for (FTPFile file : FTP.getFiles(Folders.SCANNED_PO)) {
                    if (!file.isFile()) continue;

                    String[] fn = file.getName().split("\\.");
                    if (fn.length < 2 || !fn[1].equals("txt")) continue;
                    String[] sp = fn[0].split("_");
                    if (sp.length != 3) continue;

                    POFilename val = pos.get(sp[0]);
                    if (val == null) {
                        if (sp[1].equals("1")) {
                            POFilename model = new POFilename(sp[0]);
                            model.setPas1(file.getName());
                            pos.put(sp[0], model);
                        } else if (sp[1].equals("2")) {
                            POFilename model = new POFilename(sp[0]);
                            model.setPas2(file.getName());
                            pos.put(sp[0], model);
                        }
                    } else {
                        if (sp[1].equals("1"))
                            val.setPas1(file.getName());
                        else if (sp[1].equals("2"))
                            val.setPas2(file.getName());
                    }
                }

                for (POFilename model : pos.values()) {
                    if (model.getPas1() != null && model.getPas2() != null)
                        list.add(model);
                }

                if (list.size() == 0) {
                    runOnUiThread(() -> {
                        findViewById(R.id.txtPas3NoData).setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    });
                    return;
                }
                adaptor.setList(list);
                runOnUiThread(() -> adaptor.notifyDataSetChanged());
            });
        else
            runThread(() -> {
                ArrayList<POFilename> list = new ArrayList<>();
                for (FTPFile file : FTP.getFiles(Folders.SCANNED_PO)) {
                    if (!file.isFile()) continue;

                    String[] fn = file.getName().split("\\.");
                    if (fn.length < 2 || !fn[1].equals("txt")) continue;
                    String[] sp = fn[0].split("_");
                    if (sp.length != 2) continue;

                    list.add(new POFilename(sp[0], file.getName()));
                }

                if (list.size() == 0) {
                    runOnUiThread(() -> {
                        findViewById(R.id.txtPas3NoData).setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    });
                    return;
                }
                adaptor.setList(list);
                runOnUiThread(() -> adaptor.notifyDataSetChanged());
            });
    }

}