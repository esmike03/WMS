package com.zebra.waltermartmobilecollector.activities.pcount.withdrawal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;
import com.zebra.waltermartmobilecollector.activities.pcount.Model;
import com.zebra.waltermartmobilecollector.activities.pcount.Service;
import com.zebra.waltermartmobilecollector.services.FTP;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ScanActivity extends ScanBaseActivity {

    private TextView desc, upc, sku, vendor;
    private Model scannedItem;
    private ArrayList<WModel> items = new ArrayList<>();
    private ArrayList<String> skus = new ArrayList<>();
    private String folder;
    private int rowCount = 0;
    private StringBuilder stringBuilder = new StringBuilder();
    private StringBuilder str = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcount_withdrawal_scan);

        backInto = LocationSelectionActivity.class;

        Intent intent = getIntent();
        if (intent == null) return;
        folder = intent.getStringExtra("folder");

        desc = findViewById(R.id.desc);
        upc = findViewById(R.id.upc);
        sku = findViewById(R.id.sku);
        vendor = findViewById(R.id.vendor);

        setDefaultLayoutID();

        createBarcodeHandler();

        runThread(() -> {
            FTP.loopThroughData(
                    folder + Globals.selectedLocation + "/Final.txt",
                    line -> {
                        String[] sp = line.split(",");
                        items.add(new WModel(
                                sp[2],
                                sp[3]
                        ));
                    }
            );
        });
    }

    @Override
    public boolean scanProcess(String data) {
        scannedItem = Service.findItem(data);
        if (scannedItem == null) {
            hideDetailsLayout();
            showError("Barcode not found!!!");
            return false;
        }
        WModel model = findBarcode(data);
        if (model == null) {
            hideDetailsLayout();
            showError("This item is not in this location!!!");
            return false;
        }

        scannedItem.setBarcode(data);
        scannedItem.setUpdatedQty(model.getQty() + "");
        desc.setText(scannedItem.getDesc());
        upc.setText(scannedItem.getUpc());
        sku.setText(scannedItem.getSku());
        vendor.setText(scannedItem.getVendor());
        showDetailsLayout();

        return true;
    }

    @Override
    public void saveProcess(int newQ) {
        if (newQ > Integer.parseInt(scannedItem.getUpdatedQty())) {
            showError("Entered quantity is greater than the quantity in this location!!!");
            return;
        }
        rowCount = 0;
        runThread(() -> {
            str.setLength(0);
            stringBuilder.setLength(0);
            skus.clear();

            String date = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());
            FTP.loopThroughData(
                    Folders.WITHDRAWAL_PCOUNT + "Logs.csv",
                    line -> {
                        rowCount++;
                        if (rowCount < 8) return;

                        String[] sp = line.split(",");
                        if (sp.length < 11) return;

                        str.append(line).append("\n");
                        if (!skus.contains(sp[3]))
                            skus.add(sp[3]);
                    }
            );
            if (!skus.contains(scannedItem.getSku()))
                skus.add(scannedItem.getSku());
            str
                    .append(rowCount - 6).append(",")
                    .append(date).append(",")
                    .append(Globals.selectedLocation).append(",")
                    .append(scannedItem.getSku()).append(",'")
                    .append(scannedItem.getBarcode()).append(",,")
                    .append(newQ).append(",")
                    .append(scannedItem.getUpdatedQty()).append(",");
            String storeCode = Globals.getStoreCode();
            for (WModel model : items) {
                if (model.getBarcode().equals(scannedItem.getBarcode())) {
                    model.minus(newQ);
                    str
                            .append(model.getQty()).append(",")
                            .append(model.getQty()).append(",")
                            .append(Globals.name);
                }

                stringBuilder
                        .append(storeCode).append(",")
                        .append(Globals.selectedLocation).append(",")
                        .append(model.getBarcode()).append(",")
                        .append(model.getQty()).append("\n");
            }
            StringBuffer bf = new StringBuffer()
                    .append(",WITHDRAWAL\n,Store Code:,").append(Globals.getStoreCode())
                    .append("\n,TOTAL SKU with COUNT:,").append(skus.size())
                    .append("\n,TOTAL PCS deducted:,=SUM(G8:G").append(rowCount+1).append(")\n\n\n")
                    .append("Item No.,Date of withdrawal,Location,SKU,Barcode,Description,QTY in PCS (deduct),QTY before withdrawal,QTY after withdrawal,Final QTY,Username\n")
                    .append(str);
            FTP.upload(
                    Folders.WITHDRAWAL_PCOUNT + "Logs.csv",
                    bf.toString()
            );
            FTP.upload(
                    folder + Globals.selectedLocation + "/Final.txt",
                    stringBuilder.toString()
            );

            scannedItem = null;
            runOnUiThread(() -> {
                showSuccess("Successfully saved.");
                onCancel(null);
            });
        });
    }

    private WModel findBarcode(String code) {
        for (WModel model : items) {
            if (model.getBarcode().equals(code)) return model;
        }
        return null;
    }

}