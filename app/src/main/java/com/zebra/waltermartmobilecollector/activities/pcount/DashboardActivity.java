package com.zebra.waltermartmobilecollector.activities.pcount;

import android.os.Bundle;
import android.widget.TextView;

import com.zebra.waltermartmobilecollector.BaseActivity;
import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.R;
import com.zebra.waltermartmobilecollector.services.FTP;

import org.apache.commons.net.ftp.FTPFile;

public class DashboardActivity extends BaseActivity {

    private boolean isPas1 = false, recon = false;
    private int rowCounter, scanned = 0, skuVariance, p1Correct, p2Correct,
            recons, p1SKU, p2SKU, totalSKU, hashTotal, withdrawal;
    private final int REPORT_HEADER = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView txtRunningUpdate = findViewById(R.id.txtRunningUpdate);
        TextView txtLocation = findViewById(R.id.txtTotalLocation);
        TextView txtScannedLocation = findViewById(R.id.txtTotalScannedLocation);
        TextView txtPas1Acc = findViewById(R.id.txtPass1Acc);
        TextView txtPas2Acc = findViewById(R.id.txtPass2Acc);

        runThread(() -> {
            rowCounter = scanned = recons = p1SKU = p2SKU = totalSKU = hashTotal = skuVariance
                    = p1Correct = p2Correct = withdrawal = 0;

            if (
                    !Globals.db
                            .rawQuery("select 1 from locations limit 1", null)
                            .moveToNext()
            ) return;

            getWithdrawals();

            for (FTPFile dir : FTP.getFiles(Folders.MATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName()))
                    majorProcess(dir.getName());
            }
            for (FTPFile dir : FTP.getFiles(Folders.UNMATCHED_PCOUNT)) {
                if (dir.isDirectory() && getPas(dir.getName()))
                    majorProcess(dir.getName());
            }

            int totalLocations = Service.getLocationCount();
            runOnUiThread(() -> {
                txtRunningUpdate.setText("RUNNING UPDATE\n" + String.format("%.0f", (double) scanned / totalLocations * 100) + "%");
                txtLocation.setText("TOTAL NO. OF LOCATION\n" + totalLocations);
                txtScannedLocation.setText("TOTAL NO. OF SCANNED LOCATION\n" + scanned);
                txtPas1Acc.setText("ACCURACY OF PASS 1\n" + String.format("%.0f", (double) p1Correct / skuVariance * 100) + "%");
                txtPas2Acc.setText("ACCURACY OF PASS 2\n" + String.format("%.0f", (double) p2Correct / skuVariance * 100) + "%");
            });
        });
    }

    private void getWithdrawals() throws Exception {
        FTP.loopThroughData(
                Folders.WITHDRAWAL_PCOUNT + "Logs.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < 8) return;

                    String[] sp = line.split(",");
                    if (sp.length < 11 || !Service.locationExists(sp[2])) return;

                    addWithdrawal(sp[6]);
                }
        );
    }

    private void majorProcess(String folder) throws Exception {
        if (!Service.locationExists(folder)) return;

        scanned++;
        rowCounter = 0;
        if (isPas1)
            pas1Process(folder);
        else
            pas2Process(folder);
    }

    private void pas1Process(String folder) throws Exception {
        FTP.loopThroughData(
                folder + "/" + folder + "_Report_Matched.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < REPORT_HEADER) return;

                    String[] sp = line.split(",");
                    if (sp.length < 8) return;

                    addHashTotal(sp[7]);
                    p1SKU++;
                    totalSKU++;
                }
        );
    }

    private void pas2Process(String folder) throws Exception {
        recon = false;
        FTP.loopThroughData(
                folder + "/" + folder + "_Report_Matched.csv",
                line -> {
                    rowCounter++;
                    if (rowCounter < REPORT_HEADER) return;

                    String[] sp = line.split(",");
                    if (sp.length < 8) return;

                    addHashTotal(sp[8]);
                    if (!sp[4].equals("0"))
                        p1SKU++;
                    if (!sp[5].equals("0"))
                        p2SKU++;
                    totalSKU++;
                    if (sp[8].equals("0")) return;

                    boolean p1C = sp[4].equals(sp[8]);
                    boolean p2C = sp[5].equals(sp[8]);
                    if (p1C && p2C) return;

                    if (!recon)
                        recons++;

                    recon = true;
                    skuVariance++;
                    if (p1C)
                        p1Correct++;
                    if (p2C)
                        p2Correct++;
                }
        );
    }

    private void addHashTotal(String qty) {
        try {
            int q = Integer.parseInt(qty);
            hashTotal += q;
        } catch (Exception e) {
        }
    }

    private void addWithdrawal(String qty) {
        try {
            int q = Integer.parseInt(qty);
            withdrawal += q;
        } catch (Exception e) {
        }
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

}