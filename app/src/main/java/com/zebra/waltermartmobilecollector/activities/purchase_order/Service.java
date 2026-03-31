package com.zebra.waltermartmobilecollector.activities.purchase_order;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;

import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public final class Service {

    public static void get(int start, int limit, ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.Model> data) {
        Cursor c = Globals.db.rawQuery(
                "SELECT p.po, p.sku, m.description, s.qty, s.si_num from pos p " +
                        "inner join scanned_pos s on p.id=s.po_id and s.user_id=" + Globals.userId +
                        " inner join main m on m.sku=p.sku " +
                        " group by p.sku LIMIT " + limit + " OFFSET " + start
                , null);
        while (c.moveToNext()) {
            com.zebra.waltermartmobilecollector.activities.purchase_order.Model model =
                    new com.zebra.waltermartmobilecollector.activities.purchase_order.Model(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3)
                    );
            model.setSi(c.getString(4));
            data.add(model);
        }
        c.close();
    }

    public static ArrayList<POCountModel> getPOCount() {
        Cursor c = Globals.db.rawQuery(
                "SELECT p.po,count(*) from scanned_pos s " +
                        "inner join pos p on p.id=s.po_id " +
                        "where s.user_id=" + Globals.userId + " group by p.po"
                , null);
        ArrayList<POCountModel> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new POCountModel(
                    c.getString(0),
                    c.getString(1)
            ));
        }
        c.close();
        return list;
    }

    public static ArrayList<Model> getPerPOWithDescWithPas3(String po) {
        Cursor c = Globals.db.rawQuery(
                "SELECT p.id,p.sku,m.barcode,m.description,p.qty,p.factor,s.qty from pos p " +
                        "inner join main m on m.sku=p.sku " +
                        "left join scanned_pos s on s.po_id=p.id and s.user_id=" + Globals.userId +
                        " where p.po=? group by p.sku"
                , new String[]{po}
        );
        ArrayList<Model> data = new ArrayList<>();
        while (c.moveToNext()) {
            data.add(new Model(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6)
            ));
        }
        c.close();
        return data;
    }

    public static boolean setPcsAndFactor(String po, String sku, Model model) {
        Cursor c = Globals.db.rawQuery(
                "SELECT qty,factor from pos where po=? and sku=? limit 1"
                , new String[]{po, sku}
        );
        boolean success = false;
        while (c.moveToNext()) {
            model.setPcsAndFactor(c.getString(0), c.getString(1));
            success = true;
        }
        c.close();

        return success;
    }

    public static Model getPas3Model(String sku, ArrayList<Model> allData) {
        for (Model model : allData) {
            if (model.getSku().equals(sku)) return model;
        }
        return null;
    }

    public static boolean isPOExists(String po) {
        boolean exists;
        Cursor c = Globals.db.rawQuery("select 1 from pos where po=? limit 1", new String[]{po});
        exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public static com.zebra.waltermartmobilecollector.activities.purchase_order.Model scannedDetails(String po, String code) {
        Cursor c = Globals.db.rawQuery(
                "select p.id,m.description,p.qty,p.sku,s.qty as uqty,p.factor, m.id as mainID from pos p " +
                        "inner join main m on m.sku=p.sku and m.barcode=? " +
                        "left join scanned_pos s on s.po_id=p.id and s.main_id=m.id and s.user_id=" + Globals.userId +
                        " where p.po=? limit 1",
                new String[]{code, po});

        com.zebra.waltermartmobilecollector.activities.purchase_order.Model p = null;
        while (c.moveToNext()) {
            p = new com.zebra.waltermartmobilecollector.activities.purchase_order.Model(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2)
            );
            p.setSku(c.getString(3));
            p.setUpdatedQty(c.getString(4));
            p.setFactor(c.getString(5));
            p.setMainID(c.getString(6));
        }
        c.close();
        return p;
    }

    public static boolean hasScanned() {
        Cursor c = Globals.db.rawQuery(
                "select p.po from pos p where exists (select 1 from scanned_pos s where s.po_id=p.id and s.user_id=? limit 1) limit 1",
                new String[]{Globals.userId}
        );
        boolean exists = c.getCount() > 0;

        c.close();
        return exists;
    }

    public static void sendToFTP(File cacheDir) throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp", ".txt", cacheDir);
            StringBuffer stringBuffer = new StringBuffer();

            FTPFile[] ftpFiles = null, matchFiles = null, unmatchFiles = null;
            boolean successLogin = true;

            try {
                FTP.login();
                matchFiles = FTP.getFtp().listFiles(Folders.MATCHED_PO);
                unmatchFiles = FTP.getFtp().listFiles(Folders.UNMATCHED_PO);
                FTP.getFtp().changeWorkingDirectory(Folders.SCANNED_PO);
                ftpFiles = FTP.getFtp().listFiles();
            } catch (Exception e) {
                successLogin = false;
            }

            Cursor c = Globals.db.rawQuery(
                    "select p.po, p.sku, s.qty, p.factor, s.si_num from scanned_pos s " +
                            "inner join pos p on p.id=s.po_id " +
                            " where s.user_id=" + Globals.userId + " order by p.po",
                    null
            );
            String currentPO = null;
            while (c.moveToNext()) {
                if (currentPO == null)
                    currentPO = c.getString(0);
                else if (!currentPO.equals(c.getString(0))) {
                    if (successLogin) {
                        if (!poIsProcessed(currentPO, matchFiles, unmatchFiles))
                            sendToFTP(
                                    tempFile,
                                    currentPO,
                                    stringBuffer.toString(),
                                    ftpFiles
                            );
                    } else downloadToLocal(currentPO, stringBuffer.toString());
                    currentPO = c.getString(0);
                    stringBuffer.setLength(0);
                }

                int qty = Helper.convertToIntAndRemoveDot(c.getString(2));
                int multiplier = Helper.convertToIntAndRemoveDot(c.getString(3));
                int totalQty = qty * multiplier;
                String siNum = c.getString(4) != null ? c.getString(4) : "";
                stringBuffer
                        .append(c.getString(0)).append(",")
                        .append(c.getString(1)).append(",")
                        .append(totalQty).append(",")
                        .append(siNum).append("\n");
            }

            if (stringBuffer.length() == 0) return;

            if (!successLogin) {
                downloadToLocal(currentPO, stringBuffer.toString());
                return;
            }
            if (poIsProcessed(currentPO, matchFiles, unmatchFiles)) return;

            sendToFTP(
                    tempFile,
                    currentPO,
                    stringBuffer.toString(),
                    ftpFiles
            );
        } finally {
            tempFile.delete();
        }
    }

    public static void updateSiNum(String po, String siNum) {
        ContentValues values = new ContentValues();
        values.put("si_num", siNum);
        Globals.db.update(
                "scanned_pos",
                values,
                "po_id IN (SELECT id FROM pos WHERE po=?) AND user_id=" + Globals.userId,
                new String[]{po}
        );
    }
    private static void downloadToLocal(String currentPO, String content) throws Exception {
        File folder = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_PO);
        if (!folder.exists())
            folder.mkdirs();
        FileService.download(
                Folders.SCANNED_PO + (!Globals.isWMS()
                        ? currentPO + "_" + Globals.name + ".txt"
                        : currentPO + "_1_" + Globals.name + ".txt"),
                content
        );
    }

    private static boolean poIsProcessed(
            String po,
            FTPFile[] matchFiles,
            FTPFile[] unmatchFiles
    ) {
        for (FTPFile file : matchFiles)
            if (!file.isFile() && file.getName().equals(po)) return true;
        for (FTPFile file : unmatchFiles)
            if (!file.isFile() && file.getName().equals(po)) return true;
        return false;
    }

    private static void sendToFTP(
            File tempFile,
            String currentPO,
            String content,
            FTPFile[] ftpFiles
    ) throws Exception {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(content);
            writer.close();
            FileInputStream is = new FileInputStream(tempFile);

            String filename = !Globals.isWMS()
                    ? currentPO + "_" + Globals.name + ".txt"
                    : currentPO + "_" + getPassNo(ftpFiles, currentPO) + "_" + Globals.name + ".txt";

            FTP.getFtp().storeFile(
                    filename,
                    is
            );
            is.close();

            File folder = new File(Environment.getExternalStorageDirectory() + Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PO);
            if (!folder.exists())
                folder.mkdirs();
            FileService.download(
                    Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PO + filename,
                    content
            );
        } catch (Exception e) {
            Log.d("TAG", "sendToFTP: " + e.getMessage());
            File folder = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_PO);
            if (!folder.exists())
                folder.mkdirs();
            FileService.download(
                    Folders.SCANNED_PO + (
                            !Globals.isWMS()
                                    ? currentPO + "_" + Globals.name + ".txt"
                                    : currentPO + "_1_" + Globals.name + ".txt"),
                    content
            );
        }
    }

    private static int getPassNo(FTPFile[] files, String po) {
        // MP0 always sends as Pass 1
        if (Globals.poMode.equals("MPO")) return 1;

        int last = 0;
        for (FTPFile file : files) {
            if (!file.isFile()) continue;
            String[] fn = file.getName().split("\\.");
            if (fn.length < 2) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 3 || !sp[0].equals(po)) continue;

            int nlast;
            try { nlast = Integer.parseInt(sp[1]); } catch (Exception e) { nlast = 0; }

            if (sp[2].equals(Globals.name)) return nlast;
            else if (nlast > last) last = nlast;
        }

        return (last + 1);
    }

    public static void updateScanned(String id, int qty, String si) {
        updateScanned(id, null, qty, si);
    }

    public static void updateScanned(String id, String mainID, int qty, String siNum) {
        ContentValues values = new ContentValues();
        values.put("qty", qty);
        values.put("si_num", siNum);

        int row = Globals.db.update("scanned_pos", values, "po_id=? and user_id=" + Globals.userId, new String[]{id});

        if (row > 0) return;

        values.put("user_id", Globals.userId);
        if (mainID != null)
            values.put("main_id", mainID);
        values.put("po_id", id);
        Globals.db.insert("scanned_pos", null, values);
    }

    public static void clearScans() {
        Globals.db.delete("scanned_pos", "user_id=" + Globals.userId, null);
    }

    public static void clearData() {
        Globals.db.execSQL("DELETE FROM pos");
        Globals.db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'pos'");

        Globals.db.execSQL("DELETE FROM scanned_pos");
    }

}