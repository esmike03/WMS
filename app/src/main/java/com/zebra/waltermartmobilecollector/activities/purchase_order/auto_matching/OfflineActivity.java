package com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.activities.MainActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.AMModel;
import com.zebra.waltermartmobilecollector.activities.purchase_order.POActivity;
import com.zebra.waltermartmobilecollector.activities.purchase_order.ReportService;
import com.zebra.waltermartmobilecollector.activities.purchase_order.Service;
import com.zebra.waltermartmobilecollector.services.FileService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class OfflineActivity extends BaseActivity {

    private Adaptor adaptor;
    private String poNo;
    private ArrayList<Model> allData;
    private AlertDialog dialog;
    private Model selectedModel;
    private TextView txtSKU, txtBarcode, txtDesc, fFile, sFile;
    private EditText edtTxtQty;
    private String pas1Filename, pas2Filename, reportFolder, pas1Filepath, pas2Filepath;
    private boolean isMatched = true, submitted = false;
    private int totalBoxExpected = 0, totalPcsExpected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_matching);

        backInto = POActivity.class;

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

        showSelectFileDialog();
    }

    private void showSelectFileDialog() {
        View view = getLayoutInflater().inflate(R.layout.auto_matching_dialog, null);
        fFile = view.findViewById(R.id.firstFile);
        sFile = view.findViewById(R.id.secondFile);

        AlertDialog selectFileDialog = new AlertDialog
                .Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");

//        view.findViewById(R.id.btnBack).setOnClickListener(vv -> {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        });

        view.findViewById(R.id.btnFirstFile).setOnClickListener(vv -> {
            startActivityForResult(intent, 101);
        });

        view.findViewById(R.id.btnSecondFile).setOnClickListener(vv -> {
            startActivityForResult(intent, 102);
        });

        view.findViewById(R.id.btnSub).setOnClickListener(vv -> {
            if (pas1Filename == null || pas2Filename == null) {
                showError("Choose 2 files to match in order to continue!!!");
                return;
            }

            adaptor.setList(allData);
            adaptor.notifyDataSetChanged();

            ((TextView) findViewById(R.id.txtPO)).setText(poNo);
            ((TextView) findViewById(R.id.txtTotalSKU)).setText(allData.size() + "");

            saveAutoMatchingReport();

            ((TextView) findViewById(R.id.txtTotalBox)).setText(totalBoxExpected + "");
            ((TextView) findViewById(R.id.txtTotalPcs)).setText(totalPcsExpected + "");

            selectFileDialog.dismiss();
        });

        selectFileDialog.show();
    }

    private void saveAutoMatchingReport() {
        AMModel amModel = ReportService.getWithoutPas3(allData, poNo);
        totalBoxExpected = amModel.getTotalBoxExpected();
        totalPcsExpected = amModel.getTotalPcsExpected();
        isMatched = amModel.isMatched();

        reportFolder = (isMatched ? Folders.MATCHED_PO : Folders.UNMATCHED_PO) + poNo + "/";

        File folder = new File(Environment.getExternalStorageDirectory() + reportFolder);
        if (!folder.exists())
            folder.mkdirs();

        moveFiles();

        TextView title = findViewById(R.id.txtAMTitle);
        if (isMatched) {
            title.setText("MATCHED");
            title.setTextColor(Color.GREEN);
            try {
                FileService.copy(
                        reportFolder + pas1Filename,
                        reportFolder + poNo + "_Final.txt"
                );
                FileService.download(
                        reportFolder + poNo + "_Report_Matched.csv",
                        amModel.getReport()
                );
            } catch (Exception e){
                showError(e.getMessage());
            }
        } else {
            title.setText("UNMATCHED");
            title.setTextColor(Color.RED);
            try {
                FileService.download(
                        reportFolder + poNo + "_Report_Unmatched.csv",
                        amModel.getReport()
                );
            } catch (Exception e){
                showError(e.getMessage());
            }
        }
        try {
            FileService.download(
                    reportFolder + poNo + "_Report_SKU.csv",
                    amModel.getSkuReport()
            );
        } catch (Exception _){
            showError(_.getMessage());
        }
    }

    public void onClickMainMenu(View _){
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

    private void moveFiles() {
        String error = FileService.move(
                pas1Filepath,
                reportFolder + pas1Filename
        );
        if (error != null)
            showError(error);
        error = FileService.move(
                pas2Filepath,
                reportFolder + pas2Filename
        );
        if (error != null)
            showError(error);
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

            Service.updateScanned(selectedModel.getId(), newQ);

            selectedModel.setPas3(val);
            adaptor.notifyDataSetChanged();

            selectedModel = null;
            dialog.dismiss();

            showSuccess("Successfully saved.");
        });
    }

    public void onSubmit(View v) {
        AMModel amModel = ReportService.get(allData, poNo);

        try {
            FileService.download(reportFolder + poNo + "_Final.txt", amModel.getFinalTxt());
            FileService.download(reportFolder + poNo + "_Report_Matched.csv", amModel.getReport());
            FileService.download(reportFolder + poNo + "_Report_SKU.csv", amModel.getSkuReport());
            showSuccess("Successfully submitted.");
            submitted = true;
        } catch (Exception e){
            showError(e.getMessage());
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String filename = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (displayNameIndex != -1) {
                filename = cursor.getString(displayNameIndex);
            }
            cursor.close();
        }
        return filename;
    }

    private void loopThroughFile(Intent data, boolean isPas1) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = getContentResolver().openInputStream(data.getData());
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] val = line.split(",");

                if (val.length != 3) continue;

                Model model = Service.getPas3Model(val[1], allData);
                if (model == null) continue;

                if (isPas1)
                    model.setPas1(val[2]);
                else
                    model.setPas2(val[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                inputStream.close();
            } catch (Exception e) {
            }
        }
    }

    private String getPONo(String filename) {
        String[] fn = filename.split("\\.");
        if (fn.length != 2) return null;
        if (!fn[1].equals("txt")) return null;
        String[] sp = fn[0].split("_");
        if (sp.length != 3) return null;

        return sp[0];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == 101) {
            pas1Filename = getFileNameFromUri(data.getData());
            if (poNo == null) {
                poNo = getPONo(pas1Filename);
                allData = Service.getPerPOWithDescWithPas3(poNo);
            } else {
                if (pas2Filename != null && !poNo.equals(getPONo(pas1Filename))) {
                    pas1Filename = null;
                    showError("File selected is different PO!!!");
                    return;
                }
            }
            pas1Filepath = getFilePathFromUri(data.getData());
            fFile.setText(pas1Filename);
            loopThroughFile(data, true);
        } else if (requestCode == 102) {
            pas2Filename = getFileNameFromUri(data.getData());
            if (poNo == null) {
                poNo = getPONo(pas2Filename);
                allData = Service.getPerPOWithDescWithPas3(poNo);
            } else {
                if (pas1Filename != null && !poNo.equals(getPONo(pas2Filename))) {
                    pas2Filename = null;
                    showError("File selected is different PO!!!");
                    return;
                }
            }
            pas2Filepath = getFilePathFromUri(data.getData());
            sFile.setText(pas2Filename);
            loopThroughFile(data, false);
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;

        if (DocumentsContract.isDocumentUri(this, uri)) {
            // Document URI
            String documentId = DocumentsContract.getDocumentId(uri);

            if (isExternalStorageDocument(uri)) {
                // External Storage Provider
                String[] split = documentId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                // Downloads Provider
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(this, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                // Media Provider
                String[] split = documentId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[] {
                        split[1]
                };
                filePath = getDataColumn(this, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            filePath = getDataColumn(this, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            filePath = uri.getPath();
        }

        return filePath;
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}