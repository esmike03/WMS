package com.zebra.waltermartmobilecollector.activities.pcount;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.services.FTP;
import com.zebra.waltermartmobilecollector.services.FileService;

import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public final class Service {

    public static boolean locationExists(String location) {
        Cursor c = Globals.db.rawQuery(
                "select 1 from locations where name=? limit 1",
                new String[]{location}
        );
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public static boolean hasScanned() {
        Cursor c = Globals.db.rawQuery(
                "select main_id from scanned_pcounts where user_id=" + Globals.userId + " limit 1",
                null
        );
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public static int getLocationCount() {
        Cursor c = Globals.db.rawQuery("select count(*) from locations", null);

        while (c.moveToNext())
            return c.getInt(0);
        c.close();
        return 0;
    }

    public static boolean locationIsScanned(String location){
        Cursor c = Globals.db.rawQuery(
                "select 1 from scanned_pcounts where user_id=" + Globals.userId + " and location=? and option!=? limit 1",
                new String[] { location, Globals.stockCountOption }
        );
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public static void get(int start, int limit, ArrayList<Model> data) {
        Cursor c = Globals.db.rawQuery("SELECT m.barcode,m.sku,m.description,m.vendor,s.qty,s.location from scanned_pcounts s " +
                "inner join main m on m.id=s.main_id where s.user_id=? LIMIT " + limit + " OFFSET " + start, new String[]{Globals.userId});
        while (c.moveToNext()) {
            data.add(new Model(
                    null,
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5)
            ));
        }
    }

    public static ScanModel scannedDetails(String code) {
        Cursor c = Globals.db.rawQuery(
                "select m.description,m.sku,s.qty, m.id as mainID, type, cpo from main m " +
                        "left join scanned_pcounts s on s.main_id=m.id and s.location=? and s.option=? and s.single=" +
                        (Globals.singleValidation ? 1 : 0) + " and s.user_id=" + Globals.userId +
                        " where m.barcode=? ",
                new String[]{
                        Globals.selectedLocation,
                        Globals.stockCountOption,
                        code
                });

        ScanModel p = null;
        while (c.moveToNext()) {
            p = new ScanModel(
                    c.getString(0),
                    c.getString(1),
                    code,
                    c.getString(3),
                    c.getString(2),
                    Helper.isOutright(
                            c.getString(4),
                            c.getString(5)
                    )
            );
        }
        c.close();
        return p;
    }

    public static void getCount(ArrayList<RVModel> data) {
        Cursor c = Globals.db.rawQuery(
                "SELECT s.location, " +
                        "(select count(*) from scanned_pcounts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.case_pack as INTEGER) else ss.qty end) as ct from scanned_pcounts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
                        " from scanned_pcounts s " +
                        "inner join main m on s.main_id=m.id " +
                        "where s.user_id=" + Globals.userId + " group by s.location"
                , null);
        while (c.moveToNext()) {
            data.add(new RVModel(
                    c.getString(0),
                    c.getInt(1),
                    c.getInt(2)
            ));
        }
        c.close();
    }

    public static void getCountWithSearch(ArrayList<RVModel> data, String search) {
        data.clear();
        Cursor c = Globals.db.rawQuery(
                "SELECT s.location, " +
                        "(select count(*) from scanned_pcounts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.case_pack as INTEGER) else ss.qty end) as ct from scanned_pcounts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
                        " from scanned_pcounts s " +
                        "inner join main m on s.main_id=m.id " +
                        "where (m.sku=? or s.location=?) and s.user_id=" + Globals.userId + " group by s.location",
                new String[]{
                        search,
                        search
                }
        );
        while (c.moveToNext()) {
            data.add(new RVModel(
                    c.getString(0),
                    c.getInt(1),
                    c.getInt(2)
            ));
        }
        c.close();
    }

    public static ScanModel get(int start, String location) {
        Cursor c = Globals.db.rawQuery(
                "SELECT m.sku,m.description,m.barcode,m.id,(case when s.option='3' then s.qty * CAST(m.case_pack as INTEGER) else s.qty end) as qty,s.location,s.option" +
                        " from scanned_pcounts s " +
                        "inner join main m on s.main_id=m.id " +
                        "where s.location=? and s.user_id=" + Globals.userId + " LIMIT 1 OFFSET " + start
                , new String[]{location});
        ScanModel model = null;
        while (c.moveToNext()) {
            model = new ScanModel(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6)
            );
        }
        c.close();

        return model;
    }

    public static SKUReportModel findItemR(String code, String qty) {
        Cursor c = Globals.db.rawQuery(
                "select sku,description from main where barcode=? limit 1",
                new String[]{code}
        );

        SKUReportModel p = null;
        while (c.moveToNext()) {
            p = new SKUReportModel(
                    code,
                    c.getString(0),
                    c.getString(1),
                    qty
            );
        }
        c.close();
        return p;
    }

    public static Model findItem(String code) {
        Cursor c = Globals.db.rawQuery(
                "select id,sku,description,vendor from main where barcode=? limit 1",
                new String[]{code}
        );

        Model p = null;
        while (c.moveToNext()) {
            p = new Model(
                    c.getString(0),
                    code,
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    null
            );
        }
        c.close();
        return p;
    }

    public static ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model>
    getPerLocationWithDescAndPas3() {
        Cursor c = Globals.db.rawQuery(
                "SELECT m.id,m.sku,m.barcode,m.description,s.qty from scanned_pcounts s " +
                        "inner join main m on s.main_id=m.id and s.user_id=" + Globals.userId +
                        " where s.location=? and s.option=? and s.single=?"
                , new String[]{Globals.selectedLocation, Globals.stockCountOption, Globals.singleValidation ? "1" : "0"}
        );
        ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model> data = new ArrayList<>();
        while (c.moveToNext()) {
            data.add(new com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4)
            ));
        }
        c.close();
        return data;
    }

    public static com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model
    getPas3Model(String barcode, ArrayList<com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model> allData) {
        for (com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model model : allData) {
            if (model.getBarcode().equals(barcode)) return model;
        }

        return null;
    }

    public static com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model
    getPas3Model(String barcode) {
        Cursor c = Globals.db.rawQuery(
                "SELECT id,sku,description,case_pack from main where barcode=? limit 1"
                , new String[]{barcode}
        );

        while (c.moveToNext()) {
            com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model model = new com.zebra.waltermartmobilecollector.activities.purchase_order.auto_matching.Model(
                    c.getString(0),
                    c.getString(1),
                    barcode,
                    c.getString(2)
            );
            model.setFactor(c.getString(3));
            return model;
        }

        return null;
    }

    public static boolean locationIsDone(String location, String folder) throws Exception {
        for (FTPFile file : FTP.getFiles(folder))
            if (file.isDirectory() && file.getName().equals(location)) return true;
        return false;
    }

    public static boolean locationIsOver(String location, String folder) throws Exception {
        int c = 0;
        for (FTPFile file : FTP.getFiles(folder)) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2) continue;
            String[] sp = fn[0].split("_");
            if (!sp[0].equals(location)) continue;
            if (Globals.singleValidation) {
                Log.d("TAG", "locationIsOver: " + location + "=" + file.getName());
                if (sp.length != 3) continue;
                return !sp[2].equals(Globals.name);
            }
            if (sp.length != 4) continue;
            if (sp[3].equals(Globals.name)) return false;
            try {
                int pass = Integer.parseInt(sp[2]);
                if (pass > 1)
                    c = pass;
            } catch (Exception e) {
            }
        }
        return c > 1;
    }

    public static void sendToFTP(File cacheDir) throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp", ".txt", cacheDir);
            StringBuffer stringBuffer = new StringBuffer();

            boolean successLogin = true;
            FTPFile[] matchFiles = null, unmatchFiles = null, ftpFiles = null;
            try {
                FTP.login();
                matchFiles = FTP.getFtp().listFiles(Folders.MATCHED_PCOUNT);
                unmatchFiles = FTP.getFtp().listFiles(Folders.UNMATCHED_PCOUNT);
                ftpFiles = FTP.getFiles(Folders.SCANNED_PCOUNT);
            } catch (Exception e) {
                successLogin = false;
            }

            /**  CONTINUES **/
            sendProcess(
                    tempFile,
                    matchFiles,
                    unmatchFiles,
                    ftpFiles,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location,m.id from scanned_pcounts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='1' order by s.location",
                            null
                    ),
                    stringBuffer,
                    "CO",
                    false,
                    true
            );
//            sendProcess(
//                    tempFile,
//                    successLogin,
//                    Globals.db.rawQuery(
//                            "SELECT m.barcode,s.qty,s.location from scanned_pcounts s " +
//                                    "inner join main m on s.main_id=m.id " +
//                                    "where s.user_id=" + Globals.userId + " and s.option='1' order by s.location",
//                            null
//                    ),
//                    stringBuffer,
//                    Folders.CONTINUES_PCOUNT,
//                    false,
//                    true
//            );

            /**         PER PIECE        **/
            /**    SINGLE VALIDATION   **/
            sendProcess(
                    tempFile,
                    matchFiles,
                    unmatchFiles,
                    ftpFiles,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location,m.id from scanned_pcounts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='2' and s.single=1 order by s.location",
                            null
                    ),
                    stringBuffer,
                    "PC",
                    false,
                    true
            );
            /**    DOUBLE VALIDATION   **/
            sendProcess(
                    tempFile,
                    matchFiles,
                    unmatchFiles,
                    ftpFiles,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location,m.id from scanned_pcounts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='2' and s.single=0 order by s.location",
                            null
                    ),
                    stringBuffer,
                    "PC",
                    false,
                    false
            );

            /**         PER CASE        **/
            sendProcess(
                    tempFile,
                    matchFiles,
                    unmatchFiles,
                    ftpFiles,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location,m.id,m.case_pack from scanned_pcounts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='3' order by s.location",
                            null
                    ),
                    stringBuffer,
                    "CA",
                    true,
                    false
            );
        } finally {
            tempFile.delete();
        }
    }

    private static void sendProcess(
            File tempFile,
            FTPFile[] matchFiles,
            FTPFile[] unmatchFiles,
            FTPFile[] ftpFiles,
            boolean successLogin,
            Cursor c,
            StringBuffer stringBuffer,
            String identifier,
            boolean perCase,
            boolean single
    ) throws Exception {
        String currentLocation = null;
        stringBuffer.setLength(0);
        while (c.moveToNext()) {
            if (currentLocation == null) {
                currentLocation = c.getString(2);
            } else if (!currentLocation.equals(c.getString(2))) {
                if (successLogin) {
                    if (!locationIsProcessed(currentLocation, matchFiles, unmatchFiles))
                        sendToFTP1(
                                tempFile,
                                currentLocation,
                                stringBuffer.toString(),
                                ftpFiles,
                                identifier,
                                single
                        );
                } else downloadToLocal(
                        currentLocation,
                        stringBuffer.toString(),
                        single,
                        identifier
                );
                currentLocation = c.getString(2);
                stringBuffer.setLength(0);
            }

            int qty = (perCase
                    ? c.getInt(1) * Helper.convertToIntAndRemoveDot(c.getString(4))
                    : c.getInt(1)) * getChildCount(c.getString(3));
            stringBuffer
                    .append(Globals.getStoreCode()).append(",")
                    .append(currentLocation).append(",")
                    .append(c.getString(0)).append(",")
                    .append(qty).append("\n");
        }

        if (stringBuffer.length() == 0) return;

        if (!successLogin) {
            downloadToLocal(
                    currentLocation,
                    stringBuffer.toString(),
                    single,
                    identifier
            );
            return;
        }

        if (!locationIsProcessed(currentLocation, matchFiles, unmatchFiles))
            sendToFTP1(
                    tempFile,
                    currentLocation,
                    stringBuffer.toString(),
                    ftpFiles,
                    identifier,
                    single
            );
    }

    private static int getChildCount(String parentID){
        Cursor c = Globals.db.rawQuery("select qty from pcount_parent_childs where parent_id=?", new String[]{parentID});
        if (!c.moveToNext()) return 1;

        return c.getInt(0);
    }

    private static void sendProcess(File tempFile, boolean successLogin, Cursor c, StringBuffer stringBuffer, String folder, boolean perCase, boolean single) throws Exception {
        FTPFile[] matchFiles = FTP.getFtp().listFiles(folder + "Matched");
        FTPFile[] unmatchFiles = FTP.getFtp().listFiles(folder + "Unmatched");
        FTPFile[] ftpFiles = FTP.getFiles(folder);

        String currentLocation = null;
        stringBuffer.setLength(0);
        while (c.moveToNext()) {
            if (currentLocation == null) {
                currentLocation = c.getString(2);
            } else if (!currentLocation.equals(c.getString(2))) {
                if (successLogin) {
                    if (!locationIsProcessed(currentLocation, matchFiles, unmatchFiles))
                        sendToFTP(
                                tempFile,
                                currentLocation,
                                stringBuffer.toString(),
                                ftpFiles,
                                single
                        );
//                        sendToFTP(
//                                tempFile,
//                                stringBuffer.toString(),
//                                currentLocation
//                                        + (c.getString(3).equals("1") ? "" : "_" + getPassNo(ftpFiles, currentLocation)) +
//                                        "_" + Globals.name + ".txt",
//                                folder + "/"
//                        );
                } else downloadToLocal(
                        currentLocation,
                        folder,
                        stringBuffer.toString(),
                        single
                );
                currentLocation = c.getString(2);
                stringBuffer.setLength(0);
            }

            stringBuffer
                    .append(Globals.getStoreCode()).append(",")
                    .append(currentLocation).append(",")
                    .append(c.getString(0)).append(",")
                    .append(
                            perCase
                                    ? c.getInt(1) * Helper.convertToIntAndRemoveDot(c.getString(3))
                                    : c.getInt(1)
                    ).append("\n");
        }

        if (stringBuffer.length() == 0) return;

        if (!successLogin) {
            downloadToLocal(
                    currentLocation,
                    folder,
                    stringBuffer.toString(),
                    single
            );
            return;
        }

        if (!locationIsProcessed(currentLocation, matchFiles, unmatchFiles))
            sendToFTP(
                    tempFile,
                    currentLocation,
                    stringBuffer.toString(),
                    ftpFiles,
                    single
            );
//            sendToFTP(
//                    tempFile,
//                    stringBuffer.toString(),
//                    currentLocation
//                            + (c.getString(3).equals("1") ? "" : "_" + getPassNo(ftpFiles, currentLocation)) +
//                            "_" + Globals.name + ".txt",
//                    folder + "/"
//            );
    }

    private static void sendToFTP1(
            File tempFile,
            String currentLocation,
            String content,
            FTPFile[] ftpFiles,
            String identifier,
            boolean single
    ) throws Exception {
        try {
            int pass = 0;
            if (single) {
                if (hasScanned(ftpFiles, currentLocation)) return;
            } else {
                pass = getPassNo(ftpFiles, currentLocation);
                if (pass > 2) return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(content);
            writer.close();
            FileInputStream is = new FileInputStream(tempFile);

            String filename = single
                    ? currentLocation + "_" + identifier + "_" + Globals.name + ".txt"
                    : currentLocation + "_" + identifier + "_" + pass + "_" + Globals.name + ".txt";

            FTP.getFtp().storeFile(
                    filename,
                    is
            );
            is.close();

            FileService.download(
                    Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PCOUNT + filename,
                    content
            );
        } catch (Exception e) {
            Log.d("TAG", "sendToFTP: " + e.getMessage());
            FileService.download(
                    Folders.SCANNED_PCOUNT + (
                            !single
                                    ? currentLocation + "_" + identifier + "_" + Globals.name + ".txt"
                                    : currentLocation + "_" + identifier + "_0_" + Globals.name + ".txt"),
                    content
            );
        }
    }

    private static void sendToFTP(
            File tempFile,
            String currentLocation,
            String content,
            FTPFile[] ftpFiles,
            boolean single
    ) throws Exception {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(content);
            writer.close();
            FileInputStream is = new FileInputStream(tempFile);

            String filename = single
                    ? currentLocation + "_" + Globals.name + ".txt"
                    : currentLocation + "_" + getPassNo(ftpFiles, currentLocation) + "_" + Globals.name + ".txt";

            FTP.getFtp().storeFile(
                    filename,
                    is
            );
            is.close();

            FileService.download(
                    Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PCOUNT + filename,
                    content
            );
        } catch (Exception e) {
            Log.d("TAG", "sendToFTP: " + e.getMessage());
            FileService.download(
                    Folders.SCANNED_PCOUNT + (
                            !single
                                    ? currentLocation + "_" + Globals.name + ".txt"
                                    : currentLocation + "_0_" + Globals.name + ".txt"),
                    content
            );
        }
    }

    private static void downloadToLocal(String currentLocation, String content, boolean single, String identifier) throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_PCOUNT);
        if (!dir.exists())
            dir.mkdirs();
        FileService.download(
                Folders.SCANNED_PCOUNT + "/" + (single
                        ? currentLocation + "_" + identifier + "_" + Globals.name + ".txt"
                        : currentLocation + "_" + identifier + "_0_" + Globals.name + ".txt"),
                content
        );
    }

    private static void downloadToLocal(String currentLocation, String folder, String content, boolean single) throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory() + folder);
        if (!dir.exists())
            dir.mkdirs();
        FileService.download(
                folder + "/" + (single
                        ? currentLocation + "_" + Globals.name + ".txt"
                        : currentLocation + "_0_" + Globals.name + ".txt"),
                content
        );
    }

    private static void sendToFTP(
            File tempFile,
            String content,
            String filename,
            String folder
    ) throws Exception {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(content);
            writer.close();
            FileInputStream is = new FileInputStream(tempFile);

            FTP.getFtp().storeFile(
                    Folders.SCANNED_PCOUNT + folder + filename,
                    is
            );
            is.close();

            File afolder = new File(Environment.getExternalStorageDirectory() + Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PCOUNT + folder);
            if (!afolder.exists())
                afolder.mkdirs();
            FileService.download(
                    Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.PCOUNT + folder + filename,
                    content
            );
        } catch (Exception e) {
            Log.d("TAG", "sendToFTP: " + e.getMessage());
            File afolder = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_PCOUNT);
            if (!afolder.exists())
                afolder.mkdirs();
            FileService.download(
                    Folders.SCANNED_PCOUNT + folder + filename,
                    content
            );
        }
    }

    public static int getPassNo(FTPFile[] files, String location) {
        if (files == null) return 1;

        int last = 0;
        for (FTPFile file : files) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 4 || !sp[0].equals(location)) continue;

            int nlast;
            try {
                nlast = Integer.parseInt(sp[2]);
            } catch (Exception e) {
                nlast = 0;
            }
            if (sp[3].equals(Globals.name)) {
                return nlast;
            } else if (nlast > last) {
                last = nlast;
            }
        }

        return (last + 1);
    }

    public static boolean hasScanned(FTPFile[] files, String location) {
        if (files == null) return false;

        for (FTPFile file : files) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 3 || !sp[0].equals(location)) continue;

            return !sp[2].equals(Globals.name);
        }

        return false;
    }

    private static void downloadToLocal(String currentLocation, String content) throws Exception {
        FileService.download(
                Folders.SCANNED_PCOUNT + (!Globals.isWMS()
                        ? currentLocation + "_" + Globals.name + ".txt"
                        : currentLocation + "_0_" + Globals.name + ".txt"),
                content
        );
    }

    public static boolean locationIsProcessed(
            String location,
            FTPFile[] matchFiles,
            FTPFile[] unmatchFiles
    ) {
        if (matchFiles != null) {
            for (FTPFile file : matchFiles)
                if (!file.isFile() && file.getName().equals(location)) return true;
        }
        if (unmatchFiles != null) {
            for (FTPFile file : unmatchFiles)
                if (!file.isFile() && file.getName().equals(location)) return true;
        }
        return false;
    }

    public static void updateScanned(String id, int qty) {
        ContentValues values = new ContentValues();
        values.put("qty", qty);

        int rows = Globals.db.update(
                "scanned_pcounts",
                values,
                "location=? and main_id=? and option=? and single=" + (Globals.singleValidation ? 1 : 0) + " and user_id=" + Globals.userId,
                new String[]{Globals.selectedLocation, id, Globals.stockCountOption}
        );

        if (rows > 0) return;

        values.put("location", Globals.selectedLocation);
        values.put("user_id", Globals.userId);
        values.put("main_id", id);
        values.put("option", Globals.stockCountOption);
        values.put("single", Globals.singleValidation);
        Globals.db.insert("scanned_pcounts", null, values);
    }

    public static void clearScans() {
        Globals.db.delete("scanned_pcounts", "user_id=?", new String[]{Globals.userId});
    }

    public static void clearLocations() {
        Globals.db.execSQL("DELETE FROM locations");
    }

    public static void clearParentChilds() {
        Globals.db.execSQL("DELETE FROM pcount_parent_childs");
    }

}