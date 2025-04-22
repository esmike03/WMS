package com.zebra.waltermartmobilecollector.activities.purchase_order.reports;

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
import com.zebra.waltermartmobilecollector.activities.purchase_order.Service;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

public class ListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matching_list);

        backInto = POActivity.class;

        boolean matched = Globals.reportMatch;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdaptor adaptor = new ListAdaptor(po -> {
            if (!Service.isPOExists(po)) {
                showError("P.O. not found in current P.O. Masterfile!!!");
                return;
            }

            Intent intt = new Intent(
                    ListActivity.this,
                    Globals.isWMS()
                            ? AutoMatchingActivity.class
                            : WDSAutoMatchingActivity.class
            );
            if (Globals.isWMS())
                intt.putExtra("matched", matched);
            intt.putExtra("po_number", po);
            startActivity(intt);
            finish();
        });
        recyclerView.setAdapter(adaptor);

        runThread(() -> {
            ArrayList<String> list = new ArrayList<>();
            for (FTPFile folder : FTP.getFiles(matched ? Folders.MATCHED_PO : Folders.UNMATCHED_PO)) {
                if (folder.isFile()) continue;

                list.add(folder.getName());
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