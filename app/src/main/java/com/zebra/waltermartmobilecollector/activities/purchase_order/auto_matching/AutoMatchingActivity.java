package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.purchase_order.AMModel;
import com.zebra.waltermartmobilecollector.activities.purchase_order.ReportService;
import com.zebra.waltermartmobilecollector.activities.purchase_order.Service;
import com.zebra.waltermartmobilecollector.services.FTP;

import java.util.ArrayList;

public class AutoMatchingActivity extends BaseActivity {

    private Adaptor adaptor;
    private String poNo;
    private ArrayList<Model> allData;
    private AlertDialog dialog;
    private Model selectedModel;
    private TextView txtSKU, txtBarcode, txtDesc;
    private EditText edtTxtQty,edtSI;
    private String pas1Filename, pas2Filename, reportFolder;
    private boolean isMatched = true, submitted = false;
    private int totalBoxExpected = 0, totalPcsExpected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matching);


        TextView pas2Header = findViewById(R.id.txtPas2);
        pas2Header.setText(Globals.poMode.equals("MPO") ? "P0" : "P2");

        backInto = ListActivity.class;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adaptor = new Adaptor(model -> {
            selectedModel = model;
            createDialog();
            txtSKU.setText(selectedModel.getSku());
            txtBarcode.setText(selectedModel.getBarcode());
            txtDesc.setText(selectedModel.getDesc());
            edtTxtQty.setText((selectedModel.getPas3() / selectedModel.getFactor()) + "");
            dialog.show();
        });
        recyclerView.setAdapter(adaptor);

        Intent intent = getIntent();
        if (intent == null) return;
        pas1Filename = intent.getStringExtra("pas1_filename");
        pas2Filename = intent.getStringExtra("pas2_filename");
        poNo = intent.getStringExtra("po_number");
        edtSI = findViewById(R.id.edtSI);

        edtSI.setFilters(new android.text.InputFilter[]{
                // Length limit
                new android.text.InputFilter.LengthFilter(30),

                // Allowed characters filter
                (source, start, end, dest, dstart, dend) -> {
                    StringBuilder filtered = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char c = source.charAt(i);

                        if (Character.isLetterOrDigit(c) || c == '/') {
                            filtered.append(c);
                        }
                    }
                    return filtered.length() == end - start ? null : filtered.toString();
                }
        });

        allData = Service.getPerPOWithDescWithPas3(poNo);

        ((TextView) findViewById(R.id.txtPO)).setText(poNo);
        ((TextView) findViewById(R.id.txtTotalSKU)).setText(allData.size() + "");

        runThread(() -> {
            if (Globals.ftpStoreCode == null) {
                try {
                    FTP.fetchStoreCode();
                } catch (Exception e) {
                }
            }

// Replace getPas2() call with this branch in onCreate's runThread:
            getPas1();
            if (Globals.poMode.equals("MPO")) {
                getMasterfileQty(); // reads from pos table instead
            } else {
                getPas2();
            }

            saveAutoMatchingReport();

            adaptor.setList(allData);
            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.txtTotalBox)).setText(totalBoxExpected + "");
                ((TextView) findViewById(R.id.txtTotalPcs)).setText(totalPcsExpected + "");
                TextView title = findViewById(R.id.txtAMTitle);
                if (isMatched) {
                    title.setText("MATCHED");
                    title.setTextColor(Color.GREEN);
                } else {
                    title.setText("UNMATCHED");
                    title.setTextColor(Color.RED);
                }
                adaptor.notifyDataSetChanged();
            });
        });
    }

    private void getMasterfileQty() {
        for (Model model : allData) {
            if (!Service.setPcsAndFactor(poNo, model.getSku(), model))
                continue;
            model.setPas2(String.valueOf(model.getPcs())); // ✅ convert int to String
        }
    }

    private void saveAutoMatchingReport() throws Exception {
        AMModel amModel = ReportService.getWithoutPas3(allData, poNo, edtSI.getText().toString().trim());
        totalBoxExpected = amModel.getTotalBoxExpected();
        totalPcsExpected = amModel.getTotalPcsExpected();
        isMatched = amModel.isMatched();

        reportFolder = (isMatched ? Folders.MATCHED_PO : Folders.UNMATCHED_PO) + poNo + "/";

        FTP.getFtp().makeDirectory(reportFolder);
        moveFiles();
        if (isMatched) {
//            FTP.copy(
//                    reportFolder + pas1Filename,
//                    reportFolder + poNo + "_Final.txt"
//            );

            FTP.upload(reportFolder + "RCR_" + poNo + "_Final.txt", amModel.getFinalTxt()); // ← overwrites with get()
            FTP.upload(reportFolder + "RCR_" + poNo + "_Receipt.csv", amModel.getReceipt());
            FTP.upload(reportFolder + "RCR_" + poNo + "_Report_Matched.csv", amModel.getReport());
        } else {
            FTP.upload(reportFolder + "RCR_" + poNo + "_Report_Unmatched.csv", amModel.getReport());
        }
        FTP.upload(reportFolder + "RCR_" + poNo + "_Report_SKU.csv", amModel.getSkuReport());

        if (isMatched) {
            sendToMMS(amModel);
        }
    }

    private void moveFiles() throws Exception {
        FTP.move(Folders.SCANNED_PO + pas1Filename, reportFolder + pas1Filename);
        if (Globals.poMode.equals("MP2") && pas2Filename != null) {
            FTP.move(Folders.SCANNED_PO + pas2Filename, reportFolder + pas2Filename);
        }
    }

    private void createDialog() {
        if (dialog != null) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_auto_matching_edit, null);

        txtSKU = view.findViewById(R.id.txtPas3SKU);
        txtBarcode = view.findViewById(R.id.txtPas3Barcode);
        txtDesc = view.findViewById(R.id.txtPas3Desc);
        edtTxtQty = view.findViewById(R.id.edtTxtPas3Qty);

        dialog = new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setView(view)
                .create();

        view.findViewById(R.id.btnPas3Cancel).setOnClickListener(v -> {
            selectedModel = null;
            dialog.dismiss();
        });
        view.findViewById(R.id.btnPas3Save).setOnClickListener(v -> {
            int newQ = validateQty(edtTxtQty);
            if (newQ == -1) return;

            int val = newQ * selectedModel.getFactor();

            if (val > selectedModel.getPcs()) {
                showErrorInThread("Invalid Quantity!!!");
                return;
            }

            String siNum = edtSI.getText().toString().trim();
            Service.updateScanned(selectedModel.getId(), newQ, siNum);
            Service.updateSiNum(poNo, siNum); // ← save SI to all rows of this PO

            selectedModel.setPas3(val);
            adaptor.notifyDataSetChanged();

            selectedModel = null;
            dialog.dismiss();

            showSuccess("Successfully saved.");
        });
    }

    public void onSubmit(View v) {
        new AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit recount?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (d, k) -> runThread(() -> {
                    saveReport();
                    showSuccessInThread("Successfully submitted.");
                    submitted = true;
                }))
                .show();
    }

    private void getPas1() throws Exception {
        FTP.downloadAsArraylist(
                Folders.SCANNED_PO,
                pas1Filename,
                (statement, rows) -> {
                    if (rows.size() > 2 && edtSI.getText().toString().isEmpty()) {
                        runOnUiThread(() -> edtSI.setText(rows.get(2))); // siNum moved to index 2
                    }

                    Model model = Service.getPas3Model(rows.get(3), allData); // SKU moved to index 3
                    if (model != null) {
                        model.setPas1(rows.get(4));                                    // totalQty at index 4
                        if (rows.size() > 2) model.setSiNum(rows.get(2));             // siNum at index 2
                        if (rows.size() > 5) model.setPas1Username(rows.get(5));      // username at index 5
                        if (rows.size() > 1) model.setPas1Date(rows.get(1));          // lastScannedDate at index 1
                    }
                    return true;
                }
        );
    }

    private void getPas2() throws Exception {
        FTP.downloadAsArraylist(
                Folders.SCANNED_PO,
                pas2Filename,
                (statement, rows) -> {
                    android.util.Log.d("PAS2", rows.toString());
                    Model model = Service.getPas3Model(rows.get(3), allData); // SKU moved to index 3
                    if (model != null) {
                        model.setPas2(rows.get(4));                                    // totalQty at index 4
                        if (rows.size() > 5) model.setPas2Username(rows.get(5));      // username at index 5
                        if (rows.size() > 1) model.setPas2Date(rows.get(1));          // lastScannedDate at index 1
                    }
                    return true;
                }
        );
    }

    public void onClickMainMenu(View _) {
        if (isMatched || submitted) {
            onClickHome(null);
            return;
        }

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Recount not submitted yet, do you want to continue?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialogInterface, i) -> onClickHome(null))
                .show();
    }

    private void saveReport() throws Exception {
        AMModel amModel = ReportService.get(allData, poNo, edtSI.getText().toString().trim());

        FTP.upload(reportFolder + "RCR_" + poNo + "_Receipt.csv", amModel.getReceipt());
        FTP.upload(reportFolder + "RCR_" + poNo + "_Final.txt", amModel.getFinalTxt());
        FTP.upload(reportFolder + "RCR_" + poNo + "_Report_Matched.csv", amModel.getReport());
        FTP.upload(reportFolder + "RCR_" + poNo + "_Report_SKU.csv", amModel.getSkuReport());

        sendToMMS(amModel);

    }

    private void sendToMMS(AMModel amModel) {
        try {
            FTP.disconnect();
            FTP.loginMMS();

            String basePath = Globals.getMmsFtpPath();
            if (basePath == null) basePath = "";
            if (!basePath.isEmpty() && !basePath.endsWith("/")) basePath += "/";

            String ftpFolder = basePath + Folders.MMS_FTP_FOLDER;
            String rcrFolder = basePath + Folders.MMS_RCR;
            String mmsFolder = basePath + Folders.MMS_RCR + poNo + "/";

            FTP.makeMmsDirectory(ftpFolder);
            FTP.makeMmsDirectory(rcrFolder);
            FTP.makeMmsDirectory(mmsFolder);

            // Upload all data files first
            FTP.uploadToMMS(mmsFolder + "RCR_" + poNo + "_Final.txt", amModel.getFinalTxt());
            FTP.uploadToMMS(mmsFolder + "RCR_" + poNo + "_Receipt.csv", amModel.getReceipt());
            FTP.uploadToMMS(mmsFolder + "RCR_" + poNo + "_Report_Matched.csv", amModel.getReport());
            FTP.uploadToMMS(mmsFolder + "RCR_" + poNo + "_Report_SKU.csv", amModel.getSkuReport());

            // Wait 5 seconds to ensure all files are fully written
            Thread.sleep(5000);

            // Send blank trigger file to signal completion
            FTP.uploadToMMS(mmsFolder + "RCR" + poNo + ".trg", "");

        } catch (Exception e) {
            showErrorInThread("Matched but failed to send to MMS: " + e.getMessage());
        } finally {
            FTP.disconnectMMS();
        }
    }
}