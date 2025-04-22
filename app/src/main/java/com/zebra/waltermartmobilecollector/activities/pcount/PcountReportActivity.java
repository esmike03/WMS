package com.zebra.waltermartmobilecollector.activities.pcount;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;

public class PcountReportActivity extends BaseActivity {

    private StringBuffer strBuff = new StringBuffer();
    private ArrayList<SKUReportModel> items = new ArrayList<>();
    private ArrayList<MajorModel> mItems = new ArrayList<>();
    private ArrayList<LocationReportModel> lItems = new ArrayList<>();
    private int lastRow, rowCounter, scanned = 0;
    //            , recons, p1SKU, p2SKU, totalSKU, hashTotal, skuVariance, p1Correct, p2Correct, withdrawal, unmatchTotal;
    private boolean isPas1 = false;

    private final int START_ROW = 8, TOTAL_ROW_NUMBER = 6, REPORT_HEADER = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcount_report);

        findViewById(R.id.btnScan).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnClear).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnHome).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnSend).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btnWithdrawal).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.btn435).setBackgroundColor(Color.TRANSPARENT);
    }

    public void onConso(View _) {
        runThread(() -> {
            items.clear();
            fetchModels(Folders.MATCHED_PCOUNT);
            fetchModels(Folders.UNMATCHED_PCOUNT);
            saveReport("CONSO.csv");
        });
    }

    public void onSKU(View _) {
        runThread(() -> {
            strBuff.setLength(0);
            strBuff.append("SKU,DESC,BARCODE,QTY,LOCATION\n");

            setSKUPerLocation(Folders.MATCHED_PCOUNT);
            setSKUPerLocation(Folders.UNMATCHED_PCOUNT);

            FTP.upload(
                    Folders.REPORTS_PCOUNT + "SKU PER LOCATION.csv",
                    strBuff.toString()
            );

            showSuccessInThread("Successfully generated report.");
        });
    }

    public void onWeighted(View _) {
        onLocationCount(null);
    }

    public void onFinal(View _) {
        runThread(() -> {
            items.clear();
            Cursor c = Globals.db.rawQuery("select barcode,sku,description from main", null);
            while (c.moveToNext())
                items.add(new SKUReportModel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2)
                ));
            fetchModels(Folders.MATCHED_PCOUNT);
            fetchModels(Folders.UNMATCHED_PCOUNT);
            saveReport("COUNT FINAL.csv");
        });
    }

    public void onMajor(View _) {
        runThread(() -> {
            mItems.clear();
            strBuff.setLength(0);
            rowCounter = 0;
//            = recons = p1SKU = p2SKU = totalSKU = hashTotal = skuVariance
//                    = p1Correct = p2Correct = withdrawal = unmatchTotal = 0;

            Cursor c = Globals.db.rawQuery("select name from locations order by name", null);
            while (c.moveToNext())
                mItems.add(new MajorModel(c.getString(0)));
            if (mItems.size() == 0) return;

            getWithdrawals();

            for (FTPFile dir : FTP.getFiles(Folders.MATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName()))
                    majorProcess(dir.getName());
            }
            for (FTPFile dir : FTP.getFiles(Folders.UNMATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName())) {
                    majorProcess(dir.getName());
//                    unmatchTotal++;
                }
            }

            for (int i = 0; i < mItems.size(); i++) {
                MajorModel model = mItems.get(i);
                int rowNum = START_ROW + i;
                strBuff
                        .append(model.getLocation()).append(",")
                        .append(model.getP1SKU()).append(",")
                        .append(model.getP2SKU()).append(",")
                        .append(model.getRecon()).append(",")
                        .append(model.getTotalSKU()).append(",")
                        .append(model.getHashTotal()).append(",")
                        .append(model.getSkuVariance()).append(",")
                        .append(model.getP1Correct()).append(",")
                        .append(model.getP2Correct()).append(",")
                        .append(model.getWithdrawal()).append(",")
                        .append("=F" + rowNum + "-J" + rowNum).append("\n");
//                        .append(model.getFinalHash()).append("\n");
            }

            lastRow = mItems.size() + START_ROW - 1;

            StringBuffer bf = new StringBuffer()
//                    .append("ACCURACY OF PASS 1,").append(String.format("%.0f", (double) p1Correct / skuVariance * 100)).append("%\n")
//                    .append("ACCURACY OF PASS 2,").append(String.format("%.0f", (double) p2Correct / skuVariance * 100)).append("%\n\n")
//                    .append(mItems.size()).append(",")
//                    .append(p1SKU).append(",")
//                    .append(p2SKU).append(",")
//                    .append(recons).append(",")
//                    .append(totalSKU).append(",")
//                    .append(hashTotal).append(",")
//                    .append(skuVariance).append(",")
//                    .append(p1Correct).append(",")
//                    .append(p2Correct).append(",")
//                    .append(withdrawal).append(",")
//                    .append(hashTotal - withdrawal)
                    .append("Running Update,").append("=INT((").append(scanned).append("/A").append(TOTAL_ROW_NUMBER).append(")*100)&\"%\"\n\n")
                    .append("ACCURACY OF PASS 1,").append("=INT((H").append(TOTAL_ROW_NUMBER).append("/G").append(TOTAL_ROW_NUMBER).append(")*100)&\"%\"\n")
                    .append("ACCURACY OF PASS 2,").append("=INT((I").append(TOTAL_ROW_NUMBER).append("/G").append(TOTAL_ROW_NUMBER).append(")*100)&\"%\"\n\n")
                    .append("=COUNTA(A").append(START_ROW).append(":A").append(lastRow).append("),")
                    .append("=SUM(B").append(START_ROW).append(":B").append(lastRow).append("),")
                    .append("=SUM(C").append(START_ROW).append(":C").append(lastRow).append("),")
                    .append("=SUM(D").append(START_ROW).append(":D").append(lastRow).append("),")
                    .append("=SUM(E").append(START_ROW).append(":E").append(lastRow).append("),")
                    .append("=SUM(F").append(START_ROW).append(":F").append(lastRow).append("),")
                    .append("=SUM(G").append(START_ROW).append(":G").append(lastRow).append("),")
                    .append("=SUM(H").append(START_ROW).append(":H").append(lastRow).append("),")
                    .append("=SUM(I").append(START_ROW).append(":I").append(lastRow).append("),")
                    .append("=SUM(J").append(START_ROW).append(":J").append(lastRow).append("),")
                    .append("=SUM(K").append(START_ROW).append(":K").append(lastRow).append("),")
//                    .append("=F" + TOTAL_ROW_NUMBER + "-J" + TOTAL_ROW_NUMBER)
                    .append("\nLOCATION,P1,P2,RECON,TOTAL SKU,HASH TOTAL,SKU with VAR, P1 CORRECT SKU,P2 CORRECT SKU, WITHDRAWAL,FINAL HASH TOTAL\n")
                    .append(strBuff);

            FTP.upload(
                    Folders.REPORTS_PCOUNT + "RGMS.csv",
                    bf.toString()
            );
        });
    }

    public void onSKUCount(View _) {
        runThread(() -> {
            items.clear();
            Cursor c = Globals.db.rawQuery("select barcode,sku from main", null);
            while (c.moveToNext())
                items.add(new SKUReportModel(
                        c.getString(0),
                        c.getString(1),
                        null
                ));
            fetchModels(Folders.MATCHED_PCOUNT);
            fetchModels(Folders.UNMATCHED_PCOUNT);

            String store = Globals.getStoreCode();
            if (store.length() < 5) store = "0".repeat(5 - store.length()) + store;
            strBuff.setLength(0);
            for (SKUReportModel model : items) {
                String sku = model.getSku();
                if (sku.length() < 9) sku = "0".repeat(9 - sku.length()) + sku;
                String qty = model.getQty() + "00";
                if (qty.length() < 10) qty = "0".repeat(10 - qty.length()) + qty;
                strBuff
                        .append(store)
                        .append(sku)
                        .append(qty).append("\n");
            }

            FTP.upload(
                    Folders.REPORTS_PCOUNT + "SKUCOUNT.txt",
                    strBuff.toString()
            );

            showSuccessInThread("Successfully generated report.");
        });
    }

    public void onLocationCount(View _) {
        runThread(() -> {
            mItems.clear();
            strBuff.setLength(0);
            rowCounter = 0;

            getWithdrawals();

            for (FTPFile dir : FTP.getFiles(Folders.MATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName()))
                    majorProcess1(dir.getName());
            }
            for (FTPFile dir : FTP.getFiles(Folders.UNMATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName()))
                    majorProcess1(dir.getName());
            }

            for (int i = 0; i < mItems.size(); i++) {
                MajorModel model = mItems.get(i);
                strBuff
                        .append(model.getLocation()).append(",")
                        .append(model.getTotalSKU()).append(",")
                        .append(model.getFinalHash()).append("\n");
            }

            StringBuffer bf = new StringBuffer()
                    .append("LOCATION,SKU,TOTAL\n")
                    .append(strBuff);

            FTP.upload(
                    Folders.REPORTS_PCOUNT + "LOCATION COUNT.csv",
                    bf.toString()
            );
        });
    }

    private void getWithdrawals() throws Exception {
        lItems.clear();
        FTP.loopThroughData(
                Folders.WITHDRAWAL_PCOUNT + "Logs.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < 8) return;

                    String[] sp = line.split(",");
                    if (sp.length < 11) return;

                    LocationReportModel model = getModel(sp[2]);
                    if (model != null) {
                        model.add(sp[6]);
                        return;
                    }
                    model = new LocationReportModel(sp[2], sp[6]);
                    lItems.add(model);
                }
        );
    }

//    private void sort() {
//        int matchTotal = mItems.size() - unmatchTotal;
//        if (matchTotal == 0 || unmatchTotal == 0) {
//            for (MajorModel model: mItems)
//                fillRow(model);
//            Log.d(TAG, "sort: inilipat lang ");
//            return;
//        }
//
//            int uC = matchTotal;
//            int mC = 0;
//            while (uC < mItems.size() && mC < matchTotal){
//                MajorModel m = mItems.get(mC);
//                MajorModel u = mItems.get(uC);
//                if (m.getLocation().compareTo(u.getLocation()) < 0){
//                    fillRow(m);
//                    mC++;
//                    Log.d(TAG, "sort: unang sorting match");
//                } else{
//                    fillRow(u);
//                    uC++;
//                    Log.d(TAG, "sort: unang sorting unmatch");
//                }
//            }
//
//            while (mC < matchTotal) {
//                fillRow(mItems.get(mC));
//                mC++;
//                Log.d(TAG, "sort: match na tira " + mC);
//            }
//            while (uC < mItems.size()) {
//                fillRow(mItems.get(uC));
//                uC++;
//                Log.d(TAG, "sort: unmatch na tira " + uC);
//            }
//    }

    private void setSKUPerLocation(String folder) throws Exception {
        for (FTPFile dir : FTP.getFiles(folder)) {
            if (!dir.isDirectory()) continue;

            FTP.loopThroughData(
                    dir.getName() + "/Final.txt",
                    line -> {
                        String[] sp = line.split(",");

                        Cursor c = Globals.db.rawQuery(
                                "select sku,description from main where barcode=? limit 1",
                                new String[]{sp[2]}
                        );
                        if (!c.moveToNext()) return;

                        strBuff
                                .append(c.getString(0)).append(",")
                                .append(c.getString(1)).append(",'")
                                .append(sp[2]).append(",")
                                .append(sp[3]).append(",")
                                .append(dir.getName()).append("\n");
                    }
            );
        }
    }

    private LocationReportModel getModel(String location) {
        for (LocationReportModel model : lItems) {
            if (model.getLocation().equals(location)) return model;
        }
        return null;
    }

    private boolean getPas(String folder) throws Exception {
        isPas1 = false;
        boolean done = false;
        for (FTPFile f : FTP.getFtp().listFiles(folder)) {
            if (f.getName().endsWith("_Report_Matched.csv"))
                done = true;
            if (!f.isFile() || f.getName().endsWith(".csv") || f.getName().equals("Final.txt"))
                continue;

            String[] sp = f.getName().split("_");
            isPas1 = (sp[1].equals("CO") || (sp.length == 3 && sp[1].equals("PC")));
            if (done) return true;
        }
        return done;
    }

    private MajorModel getMajorModel(String location) {
        for (MajorModel model : mItems)
            if (model.getLocation().equals(location)) return model;
        return null;
    }

    private void majorProcess(String folder) throws Exception {
        MajorModel model = getMajorModel(folder);
        if (model == null) return;

        scanned++;
        LocationReportModel lModel = getModel(folder);
        if (lModel != null) {
            model.setWithdrawal(lModel.getQty());
//            withdrawal += lModel.getQty();
        }

        rowCounter = 0;
        if (isPas1)
            pas1Process(folder, model);
        else
            pas2Process(folder, model);
    }

    private void majorProcess1(String folder) throws Exception {
        MajorModel model = getMajorModel(folder);
        if (model == null) {
            model = new MajorModel(folder);
            mItems.add(model);
        }

        LocationReportModel lModel = getModel(folder);
        if (lModel != null)
            model.setWithdrawal(lModel.getQty());

        rowCounter = 0;
        if (isPas1)
            pas1Process(folder, model);
        else
            pas2Process1(folder, model);
    }

    private void pas1Process(String folder, MajorModel model) throws Exception {
        FTP.loopThroughData(
                folder + "/" + folder + "_Report_Matched.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < REPORT_HEADER) return;

                    String[] sp = line.split(",");
                    if (sp.length < 8) return;

                    model.addHashTotal(sp[7]);
//                    addHashTotal(sp[7]);
                    model.addP1SKU();
//                    p1SKU++;
                    model.addTotalSKU();
//                    totalSKU++;
                }
        );
    }

    private void pas2Process(String folder, MajorModel model) throws Exception {
        FTP.loopThroughData(
                folder + "/" + folder + "_Report_Matched.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < REPORT_HEADER) return;

                    String[] sp = line.split(",");
                    if (sp.length < 8) return;

                    model.addHashTotal(sp[8]);
//                    addHashTotal(sp[8]);
                    if (!sp[4].equals("0")) {
                        model.addP1SKU();
//                        p1SKU++;
                    }
                    if (!sp[5].equals("0")) {
                        model.addP2SKU();
//                        p2SKU++;
                    }
                    model.addTotalSKU();
//                    totalSKU++;
                    if (sp[8].equals("0")) return;

                    boolean p1C = sp[4].equals(sp[8]);
                    boolean p2C = sp[5].equals(sp[8]);
                    if (p1C && p2C) return;

//                    if (!model.isRecon())
//                        recons++;
                    model.recon();
                    model.addSKUVariance();
//                    skuVariance++;
                    if (p1C) {
                        model.addP1Correct();
//                        p1Correct++;
                    }
                    if (p2C) {
                        model.addP2Correct();
//                        p2Correct++;
                    }
                }
        );
    }

    private void pas2Process1(String folder, MajorModel model) throws Exception {
        FTP.loopThroughData(
                folder + "/" + folder + "_Report_Matched.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < REPORT_HEADER) return;

                    String[] sp = line.split(",");
                    if (sp.length < 8) return;

                    model.addHashTotal(sp[8]);
                    model.addTotalSKU();
                }
        );
    }

    private void saveReport(String filename) throws Exception {
        strBuff.setLength(0);
        strBuff.append("SKU,DESC,QTY\n");
        int total = 0;
        for (SKUReportModel model : items) {
            strBuff
                    .append(model.getSku()).append(",")
                    .append(model.getDesc()).append(",")
                    .append(model.getQty()).append("\n");
            total += model.getQty();
        }
        strBuff.append(",TOTAL,").append(total);

        FTP.upload(
                Folders.REPORTS_PCOUNT + filename,
                strBuff.toString()
        );

        showSuccessInThread("Successfully generated report.");
    }

    private void fetchModels(String folder) throws Exception {
        for (FTPFile file : FTP.getFiles(folder)) {
            if (!file.isDirectory()) continue;

            FTP.loopThroughData(
                    file.getName() + "/Final.txt",
                    line -> {
                        String[] sp = line.split(",");

                        for (SKUReportModel model : items) {
                            if (model.getBarcode().equals(sp[2])) {
                                model.add(sp[3]);
                                return;
                            }
                        }

                        SKUReportModel model = Service.findItemR(sp[2], sp[3]);
                        if (model == null) return;
                        items.add(model);
                    }
            );
        }
    }

//    private void addHashTotal(String qty) {
//        try {
//            int q = Integer.parseInt(qty);
//            hashTotal += q;
//        } catch (Exception e) {
//        }
//    }

}