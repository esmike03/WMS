package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;
import com.zebra.waltermartmobilecollector.services.ReasonService;

import java.util.ArrayList;

public class ScanActivity extends ScanBaseActivity {

    private Model scannedRTV;
    private TextView txtStoreCode, desc, upc, sku, vendor;
    private Spinner spinner;
    private ArrayList<String> codes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtv_scan);

        setDefaultLayoutID();

        txtStoreCode = findViewById(R.id.po);
        desc = findViewById(R.id.desc);
        upc = findViewById(R.id.upc);
        sku = findViewById(R.id.sku);
        vendor = findViewById(R.id.vendor);
        spinner = findViewById(R.id.spinnerReason);

        spinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, ReasonService.getDescs()));

        txtStoreCode.setText("Store Code: " + Globals.getStoreCode());

        createBarcodeHandler();

        codes = ReasonService.getCodes();
    }

    @Override
    public void saveProcess(int newQ) {
        Service.updateScanned(scannedRTV.getId(), codes.get(spinner.getSelectedItemPosition()), newQ);
        showSuccess("Successfully saved.");
        onCancel(null);
    }

    @Override
    public void scanProcess(String data) {
        if (spinner.getSelectedItemPosition() < 0)
            return;

        scannedRTV = Service.scannedDetails(data, codes.get(spinner.getSelectedItemPosition()));

        if (scannedRTV == null) {
            onCancel(null);
            hideKeyboard(qty);
            showError("Barcode not found!!!");
            return;
        }

        if (!scannedRTV.isAllowed()) {
            onCancel(null);
            hideKeyboard(qty);
            showError("This item is not allowed in RTV!!!");
            return;
        }

        qty.setText("");
        showDetailsLayout();
        desc.setText(scannedRTV.getDesc());
        upc.setText(scannedRTV.getUpc());
        sku.setText(scannedRTV.getSku());
        vendor.setText(scannedRTV.getVendor());

        showDuplicateDialog(scannedRTV.getUpdatedQty());

        showKeyboard(qty);
    }

}