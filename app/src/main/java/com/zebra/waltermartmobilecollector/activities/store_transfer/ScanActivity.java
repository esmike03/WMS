package com.zebra.waltermartmobilecollector.activities.store_transfer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.R;

public class ScanActivity extends BaseActivity {

    private Model scannedST;
    private TextView instruction,desc,upc,sku, promptQty;
    private EditText qty, deci;
    private ConstraintLayout detailsLayout;
    private AlertDialog typeDialog, dialog;
    private int isPerPiece = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_st_scan);

        detailsLayout = findViewById(R.id.detailsLayout);
        instruction = findViewById(R.id.instruction);
        desc = findViewById(R.id.desc);
        upc = findViewById(R.id.upc);
        sku = findViewById(R.id.sku);
        qty = findViewById(R.id.edtTxtQty);
        deci = findViewById(R.id.edtTxtDec);

        setBarcodeHandler();

        showTypeDialog();
    }

    private void setBarcodeHandler(){

    }

    private void setVisibilityDesc(boolean vis){
        instruction.setVisibility(vis ? View.GONE : View.VISIBLE);
        detailsLayout.setVisibility(vis ? View.VISIBLE : View.GONE);
    }

    public void onReset(View v){
        scannedST = null;
        onCancel(null);
    }

    public void onDone(View v) {
        startActivity(new Intent(this, StoreTransferActivity.class));
        finish();
    }

    public void onSave(View v) {
        int newQ = validateQty();
        if (newQ != -1)
            saveProcess(newQ);
    }

    private void showDialog(){
        if (dialog == null){
            View view = getLayoutInflater().inflate(R.layout.duplicate_scan_dialog, null);
            dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setView(view)
                    .create();

            promptQty = view.findViewById(R.id.txtQty);

            view.findViewById(R.id.btnDSCancel).setOnClickListener(v -> {
                onCancel(null);
                dialog.dismiss();
            });
            view.findViewById(R.id.btnAdd).setOnClickListener(v -> dialog.dismiss());
            view.findViewById(R.id.btnSub).setOnClickListener(v -> dialog.dismiss());
        }

        promptQty.setText(scannedST.getQty());
        dialog.show();
    }

    private void showTypeDialog(){
        if (typeDialog == null){
            View view = getLayoutInflater().inflate(R.layout.type_dialog, null);
            typeDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setView(view)
                    .create();

            view.findViewById(R.id.perCase).setOnClickListener(v -> {
                isPerPiece = 0;
                typeDialog.dismiss();
            });
            view.findViewById(R.id.perPiece).setOnClickListener(v -> typeDialog.dismiss());
        }

        typeDialog.show();
    }

    private void saveProcess(int newQ){
        Service.updateScanned(scannedST.getId(), newQ, isPerPiece);
        showSuccess("Successfully saved.");
        onCancel(null);
    }

    private int validateQty(){
        String qtyStr = qty.getText().toString().trim();
        if (qtyStr.isEmpty()) {
            qty.setError("This is required");
            return -1;
        }

        int newQ = Integer.parseInt(qtyStr);
        if (newQ < 1) {
            showError("Entered quantity must be greater than 0!!!");
            return -1;
        }

        return newQ;
    }

    public void onCancel(View v){
        setVisibilityDesc(false);
        desc.setText("");
        upc.setText("");
        sku.setText("");
        qty.setText("");
        hideKeyboard(qty);
    }
}