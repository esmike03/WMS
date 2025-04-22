package com.zebra.waltermartmobilecollector.activities.store_transfer;

import android.content.ContentValues;
import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;

import java.util.ArrayList;

public final class Service {


    public static void get(int start, int limit, ArrayList<Model> data) {
        Cursor c = Globals.db.rawQuery("SELECT m.sku,m.description,s.qty,s.is_per_piece,s.from_loc,s.to_loc from scanned_sts s " +
                "inner join main m on m.id=s.main_id where s.user_id=? LIMIT "+ limit + " OFFSET " + start, new String[]{Globals.userId});
        while (c.moveToNext()) {
            data.add(new Model(
                    null,
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3).equals("1"),
                    c.getString(4),
                    c.getString(5)
            ));
        }
    }

    public static Model scannedDetails(String code, int isPerPiece){
        Cursor c = Globals.db.rawQuery(
                "select m.id,m.barcode,m.description,m.sku,s.qty as uqty from main m " +
                        "left join scanned_sts s on s.main_id=m.id " +
                        "and s.user_id=" + Globals.userId +
                        " and s.is_per_piece=" + isPerPiece +
                        " where m.barcode=? limit 1",
                new String[]{code}
        );

        Model p = null;
        while (c.moveToNext()) {
            p = new Model(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4)
            );
        }
        c.close();
        return p;
    }

    public static String getDataToSendToFTP(){
        StringBuffer stringBuffer = new StringBuffer();

        Cursor c = Globals.db.rawQuery("SELECT s.from_loc,s.to_loc,m.sku,s.qty from scanned_sts s " +
                "inner join main m on m.id=s.main_id where s.user_id=?", new String[]{Globals.userId});
        while (c.moveToNext())
            stringBuffer
                    .append(c.getString(0) + ",")
                    .append(c.getString(1) + ",")
                    .append(c.getString(2) + ",")
                    .append(c.getString(3) + "\n");
        c.close();
        return stringBuffer.toString();
    }

    public static void updateScanned(String id, int qty, int isPerPiece) {
        ContentValues values = new ContentValues();
        values.put("qty", qty);

        int rows = Globals.db.update("scanned_sts", values, "main_id=? and user_id=? and is_per_piece=" + isPerPiece,
                new String[]{ id, Globals.userId });

        if (rows > 0) return;

        values.put("user_id", Globals.userId);
        values.put("is_per_piece", isPerPiece);
        values.put("main_id", id);
        Globals.db.insert("scanned_sts", null, values);
    }

    public static void clearScans(){
        Globals.db.delete("scanned_sts", "user_id=?", new String[]{Globals.userId});
    }

}
