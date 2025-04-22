package com.zebra.waltermartmobilecollector.activities.price_check;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;

public class PriceCheckActivity extends ScanBaseActivity {

    private TextView barcode, desc, sku, rPrice, pPrice, vendor, itemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_check);

        detailsLayout = findViewById(R.id.layout);
        instruction = findViewById(R.id.txtInstruction);

        barcode = findViewById(R.id.txtBarcode);
        desc = findViewById(R.id.txtDesc);
        sku = findViewById(R.id.txtSKU);
        rPrice = findViewById(R.id.txtRPrice);
        pPrice = findViewById(R.id.txtPPrice);
        vendor = findViewById(R.id.txtVendorCode);
        itemType = findViewById(R.id.txtItemType);

        createBarcodeHandler();
    }

    @Override
    public void scanProcess(String data) {
        if (data.equals("")) return;

        Model model = Service.find(data);
        if (model == null) {
            hideDetailsLayout();
            Helper.showError("Barcode not found!!!");
            return;
        }

        barcode.setText(model.getBarcode());
        desc.setText(model.getDesc());
        sku.setText(model.getSku());
        rPrice.setText(model.getrPrice());
        pPrice.setText(model.getpPrice());
        vendor.setText(model.getVendorCode());

        if (model.isOutright()) {
            itemType.setText("Outright");
            itemType.setTextColor(Color.parseColor("#006400"));
            barcode.setTextColor(Color.parseColor("#006400"));
        } else {
            itemType.setText("Concess");
            itemType.setTextColor(Color.RED);
            barcode.setTextColor(Color.RED);
        }

        showDetailsLayout();
    }

}