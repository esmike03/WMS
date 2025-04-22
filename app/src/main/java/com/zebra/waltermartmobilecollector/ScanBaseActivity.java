package com.zebra.waltermartmobilecollector;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zebra.waltermartmobilecollector.services.Barcode;

public class ScanBaseActivity extends BaseActivity {

    public TextView instruction;
    private TextView promptQty;
    public ConstraintLayout detailsLayout;
    public AlertDialog dialog;
    private AlertDialog manualInputDialog;
    private int act = 0;
    public EditText qty;
    private String updatedQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void scanProcess(String data) {
    }

    public void saveProcess(int newQ) {
    }

    public void setManualInputInputType(EditText input) {
    }

    public void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @CallSuper
    public void onCancel(View _) {
        hideDetailsLayout();
        qty.setText("");
        hideKeyboard(qty);
        act = 0;
    }

    public void createBarcodeHandler() {
        Barcode.init(new Barcode.Listener() {
            @Override
            public void onScanned(String data, String labelType) {
                if ((manualInputDialog != null && manualInputDialog.isShowing()) || (dialog != null && dialog.isShowing()))
                    return;

                String formattedData = data.trim();
                while (formattedData.length() > 2 && formattedData.charAt(0) == '0')
                    formattedData = formattedData.substring(1);

                scanProcess(formattedData);
            }

            @Override
            public void onScannedError(Exception e) {
                if (instruction != null)
                    hideDetailsLayout();
                showError("Error scanning this barcode!!!");
            }
        });
    }

    public void setDefaultLayoutID() {
        detailsLayout = findViewById(R.id.detailsLayout);
        instruction = findViewById(R.id.instruction);
        qty = findViewById(R.id.edtTxtQty);
    }

    public void showDetailsLayout() {
        instruction.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.VISIBLE);
    }

    public void hideDetailsLayout() {
        instruction.setVisibility(View.VISIBLE);
        detailsLayout.setVisibility(View.GONE);
    }

    public void onAdd() {
        int newQ = validateQty(qty);
        if (newQ == -1) return;

        newQ = Integer.parseInt(updatedQty) + newQ;

        saveProcess(newQ);
    }

    public void onSub() {
        int newQ = validateQty(qty);
        if (newQ == -1) return;

        newQ = Integer.parseInt(updatedQty) - newQ;

        if (newQ < 0) {
            showError("Overall quantity must greater than or equal 0!!!");
            return;
        }

        saveProcess(newQ);
    }

    public void onSave(View _) {
        int newQ = validateQty(qty);
        if (newQ == -1) return;

        if (act == 0) saveProcess(newQ);
        else if (act == 1) onAdd();
        else onSub();
    }

    public void showDuplicateDialog(String uqty) {
        if (uqty == null) return;

        updatedQty = uqty;
        if (dialog == null) {
            View view = getLayoutInflater().inflate(R.layout.duplicate_scan_dialog, null);
            dialog = new AlertDialog.Builder(this).setCancelable(false).setView(view).create();

            promptQty = view.findViewById(R.id.txtQty);

            view.findViewById(R.id.btnDSCancel).setOnClickListener(v -> {
                onCancel(null);
                dialog.dismiss();
            });
            view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
                act = 1;
                dialog.dismiss();
            });
            view.findViewById(R.id.btnSub).setOnClickListener(v -> {
                act = 2;
                dialog.dismiss();
            });
        }

        promptQty.setText(updatedQty);
        dialog.show();
    }

    public void onManualInput(View _) {
        if (manualInputDialog == null) {
            View view = getLayoutInflater().inflate(R.layout.dialog_manual_input, null);
            manualInputDialog = new AlertDialog
                    .Builder(this)
                    .setCancelable(false)
                    .setView(view)
                    .create();

            EditText input = view.findViewById(R.id.edtTxtPas3Qty);
            setManualInputInputType(input);

            manualInputDialog.setOnShowListener(dialogInterface -> showKeyboard(input));
            input.setOnKeyListener((vv, i, keyEvent) -> {
                if (i != EditorInfo.IME_ACTION_SEARCH && i != EditorInfo.IME_ACTION_DONE && keyEvent.getKeyCode() != KeyEvent.KEYCODE_ENTER)
                    return false;
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return false;

                if (input.getText().toString().isEmpty()) {
                    input.setError("This is required");
                    input.requestFocus();
                    return true;
                }

                scanProcess(input.getText().toString());
                input.setText("");
                manualInputDialog.dismiss();
                return false;
            });

            view.findViewById(R.id.btnPas3Cancel).setOnClickListener(v -> {
                input.setText("");
                manualInputDialog.dismiss();
            });
            view.findViewById(R.id.btnPas3Save).setOnClickListener(v -> {
                if (input.getText().toString().isEmpty()) {
                    input.setError("This is required");
                    input.requestFocus();
                    return;
                }
                scanProcess(input.getText().toString());
                input.setText("");
                manualInputDialog.dismiss();
            });
        }

        manualInputDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Barcode.registerReceivers(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Barcode.unregisterReceivers(this);
    }

}
