package com.zebra.waltermartmobilecollector.activities.pcount;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;

public class ScanActivity extends ScanBaseActivity {
    private ScanModel scanned;
    private TextView desc, sku, barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_count_scan);

        ((TextView) findViewById(R.id.textView9)).setText("PCOUNT");
        ((TextView) findViewById(R.id.txtLocationValue)).setText(Globals.selectedLocation);

        setDefaultLayoutID();

        if (Globals.stockCountOption.equals("2"))
            ((TextView) findViewById(R.id.txtType)).setText("PER PIECE");
        else if (Globals.stockCountOption.equals("3"))
            ((TextView) findViewById(R.id.txtType)).setText("PER CASE");
        else {
            qty.setEnabled(false);
            findViewById(R.id.btnSave).setVisibility(View.GONE);
            ((Button)findViewById(R.id.btnCancel)).setText("close");
        }

        desc = findViewById(R.id.desc);
        sku = findViewById(R.id.txtSKU);
        barcode = findViewById(R.id.txtBarcode);

        createBarcodeHandler();
    }

    public void onBackToScanLocation(View _){
        startActivity(new Intent(this, ScanLocationActivity.class));
        finish();
    }

    @Override
    public boolean scanProcess(String data) {
        scanned = Service.scannedDetails(data);

        if (scanned == null) {
            onCancel(null);
            showError("Barcode not Found!!!");
            return false;
        }

        if (!scanned.isOutright()) {
            onCancel(null);
            showError("CONCESS ITEM DO NOT COUNT!!!");
            scanned = null;
            return false;
        }

        instruction.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.VISIBLE);
        desc.setText(scanned.getDesc());
        sku.setText(scanned.getSku());
        barcode.setText(data);

        if (Globals.stockCountOption.equals("1")) {
            int newQ = scanned.getQty() + 1;
            qty.setText(newQ + "");
            Service.updateScanned(scanned.getMainID(), newQ);
            return true;
        }

        qty.setText("");
        showDuplicateDialog(scanned.getQty() == 0 ? null : (scanned.getQty() + ""));
        showKeyboard(qty);
        return true;
    }

    @Override
    public void saveProcess(int newQ) {
        Service.updateScanned(scanned.getMainID(), newQ);

        showSuccess("Successfully saved.");
        onCancel(null);
    }

}