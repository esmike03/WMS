package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.text.InputFilter;
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
    private TextView txtPO, txtSI, desc, sku, barcode;
    private EditText po, si;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po_scan);

        setDefaultLayoutID();

        txtPO = findViewById(R.id.po);
        txtSI = findViewById(R.id.si);
        desc = findViewById(R.id.desc);
        sku = findViewById(R.id.txtSKU);
        barcode = findViewById(R.id.txtBarcode);
        po = findViewById(R.id.edtTxtPO);
        si = findViewById(R.id.edtTxtSI);

        createBarcodeHandler();
        setKeyListener();

        po.postDelayed(() -> showKeyboard(po), 150);
    }

    private void setKeyListener() {
        si.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(30),
                (source, start, end, dest, dstart, dend) -> {
                    String filtered = source.toString().replaceAll("[^a-zA-Z0-9/]", "");
                    return filtered;
                }
        });
        si.setOnKeyListener((view, i, keyEvent) -> {
            if (i != EditorInfo.IME_ACTION_SEARCH && i != EditorInfo.IME_ACTION_DONE && keyEvent.getKeyCode() != KeyEvent.KEYCODE_ENTER)
                return false;
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return false;

            String s = si.getText().toString().trim();
            if (s.isEmpty()) {
                si.setError("This is required");
                return true;
            }

            // ✅ If PO hasn't been confirmed yet, confirm it now
            if (poNo == null) {
                String p = po.getText().toString().trim();
                if (p.isEmpty()) {
                    po.setError("PO Number is required");
                    po.requestFocus();
                    showKeyboard(po);
                    return true;
                }

                if (!Service.isPOExists(p)) {
                    showError("P.O. not found!!!");
                    po.requestFocus();
                    showKeyboard(po);
                    return true;
                }

                // ✅ Do the same FTP check logic, then confirm both PO and SI together
                if (Globals.isWMS()) {
                    Helper.showLoading();
                    String finalS = s;
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
                            try { FTP.disconnect(); } catch (Exception e) {}
                            boolean finalAllowed = allowed;
                            runOnUiThread(() -> {
                                Helper.closeLoading();
                                if (finalAllowed) {
                                    processAfterCheckingPO(p);
                                    confirmSI(finalS); // ✅ confirm SI after PO
                                }
                            });
                        }
                    }).start();
                } else {
                    Helper.showLoading();
                    String finalS = s;
                    new Thread(() -> {
                        boolean allowed = true;
                        try {
                            FTP.login();
                            if (!allowToScanPOWDS(p)) {
                                allowed = false;
                                showErrorInThread("PO already been processed!!!");
                            }
                            if (poIsProccessed(Folders.MATCHED_PO, p)) {
                                allowed = false;
                                showErrorInThread("PO already been processed!!!");
                            }
                        } catch (Exception e) {
                        } finally {
                            try { FTP.disconnect(); } catch (Exception e) {}
                            boolean finalAllowed = allowed;
                            runOnUiThread(() -> {
                                Helper.closeLoading();
                                if (finalAllowed) {
                                    processAfterCheckingPO(p);
                                    confirmSI(finalS); // ✅ confirm SI after PO
                                }
                            });
                        }
                    }).start();
                }
                return true;
            }

            // poNo already set, just confirm SI normally
            confirmSI(s);
            return true;
        });
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

        // ✅ Auto-focus SI field after PO is confirmed
        si.setVisibility(View.VISIBLE);
        txtSI.setVisibility(View.GONE);
        si.setText("");
        si.requestFocus();
        si.postDelayed(() -> showKeyboard(si), 150);
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
    private void confirmSI(String s) {
        si.setVisibility(View.GONE);
        txtSI.setVisibility(View.VISIBLE);
        txtSI.setText("SI Number:  " + s);
        hideKeyboard(si);
        si.clearFocus();
        po.clearFocus();
        instruction.setVisibility(View.VISIBLE);
        instruction.setText("Start scanning");
    }

    private boolean allowToScanPO(String p) throws Exception {
        int c = 0;
        for (FTPFile file : FTP.getFiles(Folders.SCANNED_PO)) {
            if (!file.isFile()) continue;
            String[] fn = file.getName().split("\\.");
            if (fn.length < 2 || !fn[1].equals("txt")) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 3 || !sp[0].equals(p)) continue;

            try { Integer.parseInt(sp[1]); } catch (Exception e) { continue; }

            if (sp[2].equals(Globals.name)) return true;
            c++;
        }

        // MP2 = max 2 scanners, MP0 = max 1 scanner
        return Globals.poMode.equals("MP2") ? c < 2 : c < 1;
    }

    @Override
    public boolean scanProcess(String data) {
        if (poNo == null) return true;

        // Get SI value from whichever view is visible
        String siValue = (si.getVisibility() == View.VISIBLE)
                ? si.getText().toString().trim()
                : txtSI.getText().toString().replace("SI Number:  ", "").trim();

        if (siValue.isEmpty()) {
            showError("Please enter SI Number first!");
            si.setVisibility(View.VISIBLE);
            txtSI.setVisibility(View.GONE);
            si.requestFocus();
            si.postDelayed(() -> showKeyboard(si), 150);
            return false;
        }

        // ✅ Remove the duplicate call — was called twice before
        scannedPO = Service.scannedDetails(poNo, data);

        if (scannedPO == null) {
            onCancel(null);
            showError("Barcode not Found!!!");
            return false;
        }

        qty.setText("");
        instruction.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.VISIBLE);
        desc.setText(scannedPO.getDesc());
        sku.setText(scannedPO.getSku());
        barcode.setText(data);

        showDuplicateDialog(scannedPO.getUpdatedQty());
        showKeyboard(qty);
        return true;
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

        Service.updateScanned(scannedPO.getId(), scannedPO.getMainID(), newQ, txtSI.getText().toString().replace("SI Number: ", "").trim());

        showSuccess("Successfully saved.");
        onCancel(null);
    }

}