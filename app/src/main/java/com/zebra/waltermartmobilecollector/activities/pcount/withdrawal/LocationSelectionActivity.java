package com.zebra.waltermartmobilecollector.activities.pcount.withdrawal;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.ScanBaseActivity;
import com.zebra.waltermartmobilecollector.activities.pcount.PCountActivity;
import com.zebra.waltermartmobilecollector.activities.pcount.Service;

public class LocationSelectionActivity extends ScanBaseActivity {

    //    private FTPClient ftp;
//    private String sku;
//    private AlertDialog dialog;
//    private Model selectedModel;
    private String location = "";
    private TextView instruct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcount_withdrawal_location_selection);

        backInto = PCountActivity.class;

        instruct = findViewById(R.id.txtNoDt);

        createBarcodeHandler();
//        Intent intent = getIntent();
//        if (intent == null) return;
//        sku = intent.getStringExtra("sku");

//        RecyclerView recyclerView = findViewById(R.id.recyclerView2);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        LocationSelectionAdaptor adaptor = new LocationSelectionAdaptor(model -> {
//            selectedModel = model;
//            createDialog();
//            dialog.show();
//        });
//        recyclerView.setAdapter(adaptor);

//        runThread(()->{
//            ftp = FTP.getFtp();
//
//            ArrayList<Model> list = new ArrayList<>();
//            scanFolder(Folders.MATCHED_PCOUNT, list);
//            scanFolder(Folders.UNMATCHED_PCOUNT, list);
//
//            if (list.size() == 0) return;
//
//            adaptor.setList(list);
//            runOnUiThread(()-> {
//                recyclerView.setVisibility(View.VISIBLE);
//                findViewById(R.id.txtNoDt).setVisibility(View.GONE);
//                adaptor.notifyDataSetChanged();
//            });
//        });
    }
//
//    private void scanFolder(String folderToScan, ArrayList<Model> list) throws Exception {
//        for (FTPFile folder : ftp.listFiles(folderToScan)) {
//            if (folder.isFile()) continue;
//
//            String filepath = folderToScan + folder.getName() + "/";
//            for (FTPFile file : ftp.listFiles(filepath)) {
//                if (!file.getName().endsWith("_Final.txt")) continue;
//
//                FTP.loopThroughData(
//                        filepath + file.getName(),
//                        line -> {
//                            String[] cols = line.split(",");
//
//                            if (cols.length != 3 || !cols[1].equals(sku)) return;
//
//                            list.add(new Model(
//                                    folder.getName(),
//                                    cols[2]
//                            ));
//                        }
//                );
//                break;
//            }
//        }
//    }

//    private void createDialog(){
//        if (dialog != null) return;
//
//        View view = getLayoutInflater().inflate(R.layout.dialog_withdrawal_qty, null);
//
//        dialog = new AlertDialog
//                .Builder(this)
//                .setCancelable(false)
//                .setView(view)
//                .create();
//
//        EditText qty = view.findViewById(R.id.edtTxtQtyW);
//
//        view.findViewById(R.id.btnCancelW).setOnClickListener(v -> {
//            qty.setText("");
//            dialog.dismiss();
//        });
//        view.findViewById(R.id.btnSaveW).setOnClickListener(v -> {
//            String q = qty.getText().toString().trim();
//            if (q.isEmpty()){
//                qty.setError("This is required");
//                qty.requestFocus();
//                return;
//            }
//            int j = Integer.parseInt(q);
//            int old = Integer.parseInt(selectedModel.getQty());
//            if (j > old){
//                qty.setError("Invalid quantity");
//                qty.requestFocus();
//                return;
//            }
//
//            qty.setText("");
//            dialog.dismiss();
//        });
//    }

    @Override
    public boolean scanProcess(String data) {
        location = data.trim();
        instruct.setText(location);
        return true;
    }

    public void onOK(View _) {
        if (location.isEmpty()) {
            showError("Location is required to continue!!!");
            return;
        }
        if (!Character.isLetter(location.charAt(0)) || !location.matches("[a-zA-Z0-9]+")) {
            showError("INVALID LOCATION!!!");
            return;
        }
        if (!Service.locationExists(location)){
            showError("Unable to find location!!!");
            return;
        }

//        String folder;
//        if (Globals.stockCountOption.equals("1"))
//            folder = Folders.CONTINUES_PCOUNT;
//        else if (Globals.stockCountOption.equals("2"))
//            folder = Folders.PER_PIECE_PCOUNT;
//        else
//            folder = Folders.PER_CASE_PCOUNT;

        runThread(() -> {
            boolean existsInMatch = Service.locationIsDone(location, Folders.MATCHED_PCOUNT);
            boolean existsInUnmatch = Service.locationIsDone(location, Folders.UNMATCHED_PCOUNT);
            if (!existsInMatch && !existsInUnmatch) {
                showErrorInThread("This location is not yet processed!!!");
                return;
            }

            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(
                    "folder",
                    existsInMatch ? Folders.MATCHED_PCOUNT : Folders.UNMATCHED_PCOUNT
            );
            Globals.selectedLocation = location;
            runOnUiThread(() -> {
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    public void setManualInputInputType(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_TEXT);
    }

}