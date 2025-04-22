package com.zebra.waltermartmobilecollector.activities.stock_count;

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

    public static void getCount(ArrayList<RVModel> data) {
        Cursor c = Globals.db.rawQuery(
                "SELECT s.location, " +
                        "(select count(*) from scanned_stock_counts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.case_pack as INTEGER) else ss.qty end) as ct from scanned_stock_counts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
                        " from scanned_stock_counts s " +
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

//    public static void getCount(ArrayList<RVModel> data, String search, String code) {
//        data.clear();
//        Cursor c = Globals.db.rawQuery(
//                "SELECT s.location, " +
//                        "(select count(*) from scanned_stock_count ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
//                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.buy as INTEGER) else ss.qty end) as ct from scanned_stock_count ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
//                        " from scanned_stock_count s " +
//                        "inner join main m on s.main_id=m.id " +
//                        "where (m.sku=? or s.location=?) and m.barcode=? and s.user_id=" + Globals.userId + " group by s.location",
//                new String[]{
//                        search,
//                        search,
//                        code
//                }
//        );
//        while (c.moveToNext()) {
//            data.add(new RVModel(
//                    c.getString(0),
//                    c.getInt(1),
//                    c.getInt(2)
//            ));
//        }
//        c.close();
//    }

    public static void getCountWithSearch(ArrayList<RVModel> data, String search) {
        data.clear();
        Cursor c = Globals.db.rawQuery(
                "SELECT s.location, " +
                        "(select count(*) from scanned_stock_counts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.case_pack as INTEGER) else ss.qty end) as ct from scanned_stock_counts ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
                        " from scanned_stock_counts s " +
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

//    public static void getCountWithBarcode(ArrayList<RVModel> data, String code) {
//        data.clear();
//        Cursor c = Globals.db.rawQuery(
//                "SELECT s.location, " +
//                        "(select count(*) from scanned_stock_count ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as skucount," +
//                        "(select sum(case when ss.option='3' then ss.qty * CAST(m.buy as INTEGER) else ss.qty end) as ct from scanned_stock_count ss where ss.location=s.location and ss.user_id=" + Globals.userId + ") as total" +
//                        " from scanned_stock_count s " +
//                        "inner join main m on s.main_id=m.id " +
//                        "where m.barcode=? and s.user_id=" + Globals.userId + " group by s.location",
//                new String[]{
//                        code
//                }
//        );
//        while (c.moveToNext()) {
//            data.add(new RVModel(
//                    c.getString(0),
//                    c.getInt(1),
//                    c.getInt(2)
//            ));
//        }
//        c.close();
//    }

    public static Model scannedDetails(String code) {
        Cursor c = Globals.db.rawQuery(
                "select m.description,m.sku,s.qty, m.id as mainID, type, cpo from main m " +
                        "left join scanned_stock_counts s on s.main_id=m.id and s.location=? and s.option=? and s.user_id=" + Globals.userId +
                        " where m.barcode=? ",
                new String[]{Globals.selectedLocation, Globals.stockCountOption, code});

        Model p = null;
        while (c.moveToNext()) {
            p = new Model(
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

//    public static void get(int start, int limit, ArrayList<Model> data) {
//        Cursor c = Globals.db.rawQuery(
//                "SELECT m.sku,m.description,m.barcode,m.id,s.qty,s.location,s.option" +
//                        " from scanned_stock_count s " +
//                        "inner join main m on s.main_id=m.id " +
//                        "where s.user_id="+Globals.userId+" LIMIT " + limit + " OFFSET " + start
//                , null);
//        while (c.moveToNext()) {
//            data.add(new Model(
//                    c.getString(0),
//                    c.getString(1),
//                    c.getString(2),
//                    c.getString(3),
//                    c.getString(4),
//                    c.getString(5),
//                    c.getString(6)
//            ));
//        }
//        c.close();
//    }

    public static Model get(int start, String location) {
        Cursor c = Globals.db.rawQuery(
                "SELECT m.sku,m.description,m.barcode,m.id,(case when s.option='3' then s.qty * CAST(m.case_pack as INTEGER) else s.qty end) as qty,s.location,s.option" +
                        " from scanned_stock_counts s " +
                        "inner join main m on s.main_id=m.id " +
                        "where s.location=? and s.user_id=" + Globals.userId + " LIMIT 1 OFFSET " + start
                , new String[]{location});
        Model model = null;
        while (c.moveToNext()) {
            model = new Model(
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

//    public static ArrayList<Model> get(String location) {
//        ArrayList<Model> data = new ArrayList<>();
//        Cursor c = Globals.db.rawQuery(
//                "SELECT m.sku,m.description,m.barcode,m.id,s.qty,s.location,s.option" +
//                        " from scanned_stock_count s " +
//                        "inner join main m on s.main_id=m.id " +
//                        "where s.location=? and s.user_id="+Globals.userId
//                , new String[]{location});
//        while (c.moveToNext()) {
//            data.add(new Model(
//                    c.getString(0),
//                    c.getString(1),
//                    c.getString(2),
//                    c.getString(3),
//                    c.getString(4),
//                    c.getString(5),
//                    c.getString(6)
//            ));
//        }
//        c.close();
//        return data;
//    }

    public static boolean hasScanned() {
        Cursor c = Globals.db.rawQuery(
                "select 1 from scanned_stock_counts where user_id=" + Globals.userId + " limit 1",
                null
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

            boolean successLogin = true;

            try {
                FTP.login();
            } catch (Exception e) {
                successLogin = false;
            }

            /**   **/
            sendProcess(
                    tempFile,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location from scanned_stock_counts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='1' order by s.location",
                            null
                    ),
                    stringBuffer,
                    "CONTINUOUS",
                    false
            );
//            Cursor c = Globals.db.rawQuery(
//                    "SELECT m.sku,m.barcode,s.qty,s.location from scanned_stock_count s " +
//                            "inner join main m on s.main_id=m.id " +
//                            "where s.user_id=" + Globals.userId + " and s.option='1' order by s.location",
//                    null
//            );
//            String currentLocation = null;
//            while (c.moveToNext()) {
//                if (currentLocation == null)
//                    currentLocation = c.getString(3);
//                else if (!currentLocation.equals(c.getString(3))) {
//                    if (successLogin) {
//                        sendToFTP(
//                                tempFile,
//                                stringBuffer.toString(),
//                                currentLocation + "_" + Globals.name + ".txt",
//                                "Continues/"
//                        );
//                    } else downloadToLocal(
//                            currentLocation,
//                            "Continues",
//                            stringBuffer.toString(),
//                            true
//                    );
//                    currentLocation = c.getString(3);
//                    stringBuffer.setLength(0);
//                }
//
//                stringBuffer
//                        .append(c.getString(0)).append(",")
//                        .append(c.getString(1)).append(",")
//                        .append(c.getInt(2)).append("\n");
//            }
//
//            if (stringBuffer.length() > 0) {
//                if (!successLogin) {
//                    downloadToLocal(
//                            currentLocation,
//                            "Continues",
//                            stringBuffer.toString(),
//                            true
//                    );
//                } else
//
//                    sendToFTP(
//                            tempFile,
//                            stringBuffer.toString(),
//                            currentLocation + "_" + Globals.name + ".txt",
//                            "Continues/"
//                    );
//            }

            /**         PER PIECE        **/
            sendProcess(
                    tempFile,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location from scanned_stock_counts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='2' order by s.location",
                            null
                    ),
                    stringBuffer,
                    "PIECE",
                    false
            );
//            c = Globals.db.rawQuery(
//                    "SELECT m.sku,m.barcode,s.qty,s.location from scanned_stock_count s " +
//                            "inner join main m on s.main_id=m.id " +
//                            "where s.user_id=" + Globals.userId + " and s.option='2' order by s.location",
//                    null
//            );
//            currentLocation = null;
//            stringBuffer.setLength(0);
//            while (c.moveToNext()) {
//                if (currentLocation == null)
//                    currentLocation = c.getString(3);
//                else if (!currentLocation.equals(c.getString(3))) {
//                    if (successLogin) {
//                        sendToFTP(
//                                tempFile,
//                                stringBuffer.toString(),
//                                currentLocation + "_" + Globals.name + ".txt",
//                                "Piece/"
//                        );
//                    } else downloadToLocal(
//                            currentLocation,
//                            "Piece",
//                            stringBuffer.toString(),
//                            true
//                    );
//                    currentLocation = c.getString(3);
//                    stringBuffer.setLength(0);
//                }
//
//                stringBuffer
//                        .append(c.getString(0)).append(",")
//                        .append(c.getString(1)).append(",")
//                        .append(c.getInt(2)).append("\n");
//            }
//
//            if (stringBuffer.length() > 0) {
//                if (!successLogin) {
//                    downloadToLocal(
//                            currentLocation,
//                            "Piece",
//                            stringBuffer.toString(),
//                            true
//                    );
//                } else
//                    sendToFTP(
//                            tempFile,
//                            stringBuffer.toString(),
//                            currentLocation + "_" + Globals.name + ".txt",
//                            "Piece/"
//                    );
//            }

            /**         PER CASE        **/
            sendProcess(
                    tempFile,
                    successLogin,
                    Globals.db.rawQuery(
                            "SELECT m.barcode,s.qty,s.location,m.case_pack from scanned_stock_counts s " +
                                    "inner join main m on s.main_id=m.id " +
                                    "where s.user_id=" + Globals.userId + " and s.option='3' order by s.location",
                            null
                    ),
                    stringBuffer,
                    "CASE",
                    true
            );
//            c = Globals.db.rawQuery(
//                    "SELECT m.sku,m.barcode,s.qty,s.location,m.sell_buy from scanned_stock_count s " +
//                            "inner join main m on s.main_id=m.id " +
//                            "where s.user_id=" + Globals.userId + " and s.option='3' order by s.location",
//                    null
//            );
//            currentLocation = null;
//            stringBuffer.setLength(0);
//            while (c.moveToNext()) {
//                if (currentLocation == null)
//                    currentLocation = c.getString(3);
//                else if (!currentLocation.equals(c.getString(3))) {
//                    if (successLogin) {
//                        sendToFTP(
//                                tempFile,
//                                stringBuffer.toString(),
//                                currentLocation + "_" + Globals.name + ".txt",
//                                "Case/"
//                        );
//                    } else downloadToLocal(
//                            currentLocation,
//                            "Case",
//                            stringBuffer.toString(),
//                            true
//                    );
//                    currentLocation = c.getString(3);
//                    stringBuffer.setLength(0);
//                }
//
//                stringBuffer
//                        .append(c.getString(0)).append(",")
//                        .append(c.getString(1)).append(",")
//                        .append(c.getInt(2) * Helper.convertToIntAndRemoveDot(c.getString(4))).append("\n");
//            }
//
//            if (stringBuffer.length() == 0) return;
//
//            if (!successLogin) {
//                downloadToLocal(
//                        currentLocation,
//                        "Case",
//                        stringBuffer.toString(),
//                        true
//                );
//                return;
//            }
//
//            sendToFTP(
//                    tempFile,
//                    stringBuffer.toString(),
//                    currentLocation + "_" + Globals.name + ".txt",
//                    "Case/"
//            );
        } finally {
            tempFile.delete();
        }
    }

    private static void sendProcess(File tempFile, boolean successLogin, Cursor c, StringBuffer stringBuffer, String folder, boolean perCase) throws Exception {
        String currentLocation = null;
        stringBuffer.setLength(0);
        while (c.moveToNext()) {
            if (currentLocation == null)
                currentLocation = c.getString(2);
            else if (!currentLocation.equals(c.getString(2))) {
                if (successLogin) {
                    sendToFTP(
                            tempFile,
                            stringBuffer.toString(),
                            currentLocation + "_" + Globals.name + ".txt",
                            folder + "/"
                    );
                } else downloadToLocal(
                        currentLocation,
                        folder,
                        stringBuffer.toString(),
                        true
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
                    true
            );
            return;
        }

        sendToFTP(
                tempFile,
                stringBuffer.toString(),
                currentLocation + "_" + Globals.name + ".txt",
                folder + "/"
        );
    }

    private static void downloadToLocal(String currentLocation, String folder, String content, boolean isContinues) throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_STOCK_COUNT + folder);
        if (!dir.exists())
            dir.mkdirs();
        FileService.download(
                Folders.SCANNED_STOCK_COUNT + folder + "/" + (!isContinues
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
                    Folders.SCANNED_STOCK_COUNT + folder + filename,
                    is
            );
            is.close();

            File afolder = new File(Environment.getExternalStorageDirectory() + Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.STOCK_COUNT + folder);
            if (!afolder.exists())
                afolder.mkdirs();
            FileService.download(
                    Folders.FTP_MASTER_FOLDER + Folders.ARCHIVE + Folders.STOCK_COUNT + folder + filename,
                    content
            );
        } catch (Exception e) {
            Log.d("TAG", "sendToFTP: " + e.getMessage());
            File afolder = new File(Environment.getExternalStorageDirectory() + Folders.SCANNED_STOCK_COUNT);
            if (!afolder.exists())
                afolder.mkdirs();
            FileService.download(
                    Folders.SCANNED_STOCK_COUNT + folder + filename,
                    content
            );
        }
    }

    private static int getPassNo(FTPFile[] files, String po) {
        int last = 0;
        for (FTPFile file : files) {
            if (!file.isFile()) continue;

            String[] fn = file.getName().split("\\.");
            if (fn.length < 2) continue;
            String[] sp = fn[0].split("_");
            if (sp.length != 3 || !sp[0].equals(po)) continue;

            int nlast;
            try {
                nlast = Integer.parseInt(sp[1]);
            } catch (Exception e) {
                nlast = 0;
            }
            if (sp[2].equals(Globals.name)) {
                return nlast;
            } else if (nlast > last) {
                last = nlast;
            }
        }

        return (last + 1);
    }

    public static void updateScanned(String mainID, int qty) {
        ContentValues values = new ContentValues();
        values.put("qty", qty);

        int row = Globals.db.update(
                "scanned_stock_counts",
                values,
                "location=? and main_id=? and option=? and user_id=" + Globals.userId,
                new String[]{Globals.selectedLocation, mainID, Globals.stockCountOption}
        );

        if (row > 0) return;

        values.put("user_id", Globals.userId);
        values.put("main_id", mainID);
        values.put("location", Globals.selectedLocation);
        values.put("option", Globals.stockCountOption);
        Globals.db.insert("scanned_stock_counts", null, values);
    }

    public static void clearScans() {
        Globals.db.delete("scanned_stock_counts", "user_id=?", new String[]{Globals.userId});
    }

}