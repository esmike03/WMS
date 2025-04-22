package com.zebra.waltermartmobilecollector.activities.pcount.auto_matching.single;

import android.content.Intent;
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
import com.zebra.waltermartmobilecollector.activities.pcount.AMModel;
import com.zebra.waltermartmobilecollector.activities.pcount.ReportService;
import com.zebra.waltermartmobilecollector.activities.pcount.Service;
import com.zebra.waltermartmobilecollector.activities.pcount.auto_matching.ListActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;
import com.zebra.waltermartmobilecollector.services.FTP;

import java.util.ArrayList;

public class SingleAutoMatchingActivity extends BaseActivity {

    private SingleAdaptor adaptor;
    private ArrayList<Model> allData = new ArrayList<>();
    private AlertDialog dialog;
    private Model selectedModel;
    private TextView txtSKU, txtBarcode, txtDesc;
    private EditText edtTxtQty;
    private String pas1Filename, reportFolder;
    private boolean submitted = false;
    private int totalPcs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcount_single_auto_matching);

        backInto = ListActivity.class;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adaptor = new SingleAdaptor(model -> {
            selectedModel = model;
            createDialog();
            txtSKU.setText(selectedModel.getSku());
            txtBarcode.setText(selectedModel.getBarcode());
            txtDesc.setText(selectedModel.getDesc());
            edtTxtQty.setText((selectedModel.getPas3()) + "");
            dialog.show();
        });
        recyclerView.setAdapter(adaptor);

        Intent intent = getIntent();
        if (intent == null) return;
        pas1Filename = intent.getStringExtra("pas1_filename");

        ((TextView) findViewById(R.id.txtPO)).setText(Globals.selectedLocation);

        runThread(() -> {
            if (Globals.ftpStoreCode == null) {
                try {
                    FTP.fetchStoreCode();
                } catch (Exception e) {
                }
            }

            getPas1();

            saveAutoMatchingReport();

            adaptor.setList(allData);
            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.txtTotalSKU)).setText(allData.size() + "");
                ((TextView) findViewById(R.id.txtTotalPcs)).setText(totalPcs + "");
                adaptor.notifyDataSetChanged();
            });
        });
    }

    private void saveAutoMatchingReport() throws Exception {
        AMModel amModel = ReportService.getSingleWithoutPas3(allData);
        totalPcs = amModel.getTotalPcsExpected();

        reportFolder = Folders.MATCHED_PCOUNT + Globals.selectedLocation + "/";

        FTP.getFtp().makeDirectory(reportFolder);
        moveFiles();
        FTP.copy(
                reportFolder + pas1Filename,
                reportFolder + "Final.txt"
        );
        FTP.upload(reportFolder + Globals.selectedLocation + "_Report_Matched.csv", amModel.getReport());
        FTP.upload(reportFolder + Globals.selectedLocation + "_Report_SKU.csv", amModel.getSkuReport());
    }

    private void moveFiles() throws Exception {
        FTP.move(
                Folders.SCANNED_PCOUNT + pas1Filename,
                reportFolder + pas1Filename
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

//            Service.updateScanned(selectedModel.getId(), newQ);

            selectedModel.setPas3(newQ);
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
                Folders.SCANNED_PCOUNT,
                pas1Filename,
                (statement, rows) -> {
                    Model model = Service.getPas3Model(rows.get(2));
                    if (model == null) return true;

                    allData.add(model);
                    model.setPas1(rows.get(3));

                    return true;
                }
        );
    }

    public void onClickMainMenu(View _) {
        if (submitted) {
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
        AMModel amModel = ReportService.getSingle(allData);

        FTP.upload(reportFolder + "Final.txt", amModel.getFinalTxt());
        FTP.upload(reportFolder + Globals.selectedLocation + "_Report_Matched.csv", amModel.getReport());
        FTP.upload(reportFolder + Globals.selectedLocation + "_Report_SKU.csv", amModel.getSkuReport());
    }

}