package com.zebra.waltermartmobilecollector.activities.purchase_order.reports;

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
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Adaptor;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

public class AutoMatchingActivity extends BaseActivity {

    private Adaptor adaptor;
    private String poNo;
    private ArrayList<Model> allData = new ArrayList<>();
    private AlertDialog dialog;
    private Model selectedModel;
    private TextView txtSKU, txtBarcode, txtDesc;
    private EditText edtTxtQty;
    private String reportFolder;
    private boolean isMatched = true, submitted = false, matched = false, hasReportMatched = false;
    private int loopCounter = 0, totalBox = 0, totalPcs = 0, totalSKU=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matching);

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
        matched = intent.getBooleanExtra("matched", false);
        poNo = intent.getStringExtra("po_number");

        reportFolder = (matched
                ? Folders.MATCHED_PO
                : Folders.UNMATCHED_PO)
                + poNo + "/";

        ((TextView) findViewById(R.id.txtPO)).setText(poNo);

        runThread(() -> {
            if (Globals.ftpStoreCode == null) {
                try {
                    FTP.fetchStoreCode();
                } catch (Exception e) {
                }
            }

            checkIfHasReportMatched();
            getAllData();

            adaptor.setList(allData);

            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.txtTotalSKU)).setText(totalSKU + "");
                ((TextView) findViewById(R.id.txtTotalBox)).setText(totalBox + "");
                ((TextView) findViewById(R.id.txtTotalPcs)).setText(totalPcs + "");

                TextView title = findViewById(R.id.txtAMTitle);
                if (matched) {
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

    private void checkIfHasReportMatched() throws Exception {
        for (FTPFile file : FTP.getFiles(reportFolder)) {
            if (!file.isFile()) continue;

            if (file.getName().equals(poNo + "_Report_Matched.csv")) {
                hasReportMatched = true;
                break;
            }
        }
    }

    private void getAllData() throws Exception {
        FTP.loopThroughData(
                reportFolder + poNo + "_Report_" + (
                        hasReportMatched ? "M" : "Unm"
                ) + "atched.csv",
                line -> {
                    if (loopCounter < 8) {
                        loopCounter++;
                        return;
                    }

                    String[] cols = line.split(",");
                    if (cols.length < 9) return;

                    String sku = cols[1];
                    Model model = new Model(
                            sku,
                            cols[2].substring(1),
                            cols[3],
                            cols[4],
                            cols[5],
                            cols[6],
                            false
                    );
                    Service.setPcsAndFactor(poNo, sku, model);
                    int pc = model.getPas3() > 0 ? model.getPas3() : model.getPas1();
                    totalBox += (pc / model.getFactor());
                    totalPcs += pc;
                    if (model.getPas1() >0 || model.getPas2() > 0 || model.getPas3() > 0)
                        totalSKU++;
                    allData.add(model);
                }
        );
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
        AMModel amModel = ReportService.get(allData, poNo);

        FTP.upload(reportFolder + poNo + "_Receipt.csv", amModel.getReceipt());
        FTP.upload(reportFolder + poNo + "_Final.txt", amModel.getFinalTxt());
        FTP.upload(reportFolder + poNo + "_Report_Matched.csv", amModel.getReport());
        FTP.upload(reportFolder + poNo + "_Report_SKU.csv", amModel.getSkuReport());
    }

}