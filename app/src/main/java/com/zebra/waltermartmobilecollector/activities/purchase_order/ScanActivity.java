package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

public class ScanActivity extends ScanBaseActivity {

    private String poNo;
    private Model scannedPO;
    private TextView txtPO, desc, sku, barcode;
    private EditText po;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po_scan);

        setDefaultLayoutID();

        txtPO = findViewById(R.id.po);
        desc = findViewById(R.id.desc);
        sku = findViewById(R.id.txtSKU);
        barcode = findViewById(R.id.txtBarcode);
        po = findViewById(R.id.edtTxtPO);

        createBarcodeHandler();
        setKeyListener();

        po.postDelayed(() -> showKeyboard(po), 150);
    }

    private void setKeyListener() {
        po.setOnKeyListener((view, i, keyEvent) -> {
            if (i != EditorInfo.IME_ACTION_SEARCH && i != EditorInfo.IME_ACTION_DONE && keyEvent.getKeyCode() != KeyEvent.KEYCODE_ENTER)
                return false;
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return false;

            String p = po.getText().toString().trim();
            if (p.isEmpty()) {
                po.setError("This is required");
                return true;
            }

            if (!Service.isPOExists(p)) {
                showError("P.O. not found!!!");
                return true;
            }

            if (Globals.isWMS()) {
                Helper.showLoading();
                new Thread(() -> {
                    boolean allowed = true;
                    try {
                        FTP.login();
                        if (!allowToScanPO(p)) {
                            allowed = false;
                            showErrorInThread("PO already been processed!!!");
                            return;
                        }
                        if (poIsProccessed(Folders.MATCHED_PO, p)) {
                            allowed = false;
                            showErrorInThread("PO already been processed!!!");
                            return;
                        }
                        if (poIsProccessed(Folders.UNMATCHED_PO, p)) {
                            allowed = false;
                            showErrorInThread("PO already been processed!!!");
                        }
                    } catch (Exception e) {
                    } finally {
                        try {
                            FTP.disconnect();
                        } catch (Exception e) {
                        }
                        boolean finalAllowed = allowed;
                        runOnUiThread(() -> {
                            Helper.closeLoading();
                            if (finalAllowed)
                                processAfterCheckingPO(p);
                        });
                    }
                }).start();
//            } else processAfterCheckingPO(p);
            } else {
                Helper.showLoading();
                new Thread(() -> {
                    boolean allowed = true;
                    try {
                        FTP.login();
                        if (!allowToScanPOWDS(p)) {
                            allowed = false;
                            showErrorInThread("PO already been processed!!!");
                            return;
                        }
                        if (poIsProccessed(Folders.MATCHED_PO, p)) {
                            allowed = false;
                            showErrorInThread("PO already been processed!!!");
                        }
                    } catch (Exception e) {
                    } finally {
                        try {
                            FTP.disconnect();
                        } catch (Exception e) {
                        }
                        boolean finalAllowed = allowed;
                        runOnUiThread(() -> {
                            Helper.closeLoading();
                            if (finalAllowed)
                                processAfterCheckingPO(p);
                        });
                    }
                }).start();
            };

            return false;
        });
    }

    public void onBack(View _){
        if (poNo == null){
            onClickBack(null);
            return;
        }

        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    private void processAfterCheckingPO(String p) {
        po.setVisibility(View.GONE);
        txtPO.setVisibility(View.VISIBLE);
        txtPO.setText("PO Number:  " + p);
        poNo = p;
        instruction.setText("Start scanning");
        hideKeyboard(po);
    }

    private boolean poIsProccessed(String folder, String p) throws Exception {
        for (FTPFile file : FTP.getFiles(folder)) {
            if (!file.isFile() && file.getName().equals(p)) return true;
        }

        return false;
    }

    private boolean allowToScanPOWDS(String p) throws Exception {
        for (FTPFile file : FTP.getFiles(Folders.SCANNED_PO)) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2 || !fn[1].equals("txt")) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 2 || !sp[0].equals(p)) continue;

            return sp[1].equals(Globals.name);
        }

        return true;
    }

    private boolean allowToScanPO(String p) throws Exception {
        int c = 0;
        for (FTPFile file : FTP.getFiles(Folders.SCANNED_PO)) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2 || !fn[1].equals("txt")) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 3 || !sp[0].equals(p)) continue;

            try {
                Integer.parseInt(sp[1]);
            } catch (Exception e) {
                continue;
            }
            if (sp[2].equals(Globals.name)) return true;
            c++;
        }

        return c < 2;
    }

    @Override
    public void scanProcess(String data) {
        if (poNo == null) return;

        scannedPO = Service.scannedDetails(poNo, data);

        if (scannedPO == null) {
            onCancel(null);
            showError("Barcode not Found!!!");
            return;
        }

        qty.setText("");
        instruction.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.VISIBLE);
        desc.setText(scannedPO.getDesc());
        sku.setText(scannedPO.getSku());
        barcode.setText(data);

        showDuplicateDialog(scannedPO.getUpdatedQty());

        showKeyboard(qty);
    }

    @Override
    public void onManualInput(View _) {
        if (poNo == null) {
            showError("There are no PO yet!!!");
            showKeyboard(po);
            return;
        }

        super.onManualInput(null);
    }

    @Override
    public void saveProcess(int newQ) {
        if ((newQ * scannedPO.getFactor()) > scannedPO.getQty()) {
            showError("Invalid Quantity!!!");
            return;
        }

        Service.updateScanned(scannedPO.getId(), scannedPO.getMainID(), newQ);

        showSuccess("Successfully saved.");
        onCancel(null);
    }

}