package com.zebra.waltermartmobilecollector.activities.return_to_vendor;

import android.content.ContentValues;
import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;

import java.util.ArrayList;

public final class Service {

    public static void get(int start, int limit, ArrayList<Model> data) {
        Cursor c = Globals.db.rawQuery("SELECT m.barcode,m.description,m.vendor,s.reason,s.qty from scanned_rtvs s " +
                "inner join main m on m.id=s.main_id where s.user_id=? LIMIT " + limit + " OFFSET " + start, new String[]{Globals.userId});
        while (c.moveToNext()) {
            data.add(new Model(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    Globals.getStoreCode()
            ));
        }
    }

    public static Model scannedDetails(String code, String reason) {
        Cursor c = Globals.db.rawQuery(
                "select m.id,m.sku,m.description,m.type,m.cpo,m.vendor,s.qty from main m " +
                        "left join scanned_rtvs s on s.main_id=m.id and s.reason=? and s.user_id=" + Globals.userId +
                        " where m.barcode=? limit 1",
                new String[]{reason, code}
        );

        Model p = null;
        while (c.moveToNext()) {
            p = new Model(
                    c.getString(0),
                    code,
                    c.getString(1),
                    c.getString(2),
                    null
            );
            p.setUpdatedQty(c.getString(6));
            p.setVendor(c.getString(5));
            p.setIsAllowed(c.getString(3), c.getString(4));
        }
        c.close();
        return p;
    }

    public static Cursor getDataToSendToFTP() {
        return Globals.db.rawQuery(
                "SELECT m.vendor,m.sku,s.qty,s.reason,m.barcode,m.description,r.description as rdesc from scanned_rtvs s " +
                        "inner join main m on m.id=s.main_id " +
                        "inner join reasons r on r.code=s.reason " +
                        "where s.user_id=" + Globals.userId,null
        );
    }

    public static void updateScanned(String id, String reason, int qty) {
        ContentValues values = new ContentValues();
        values.put("qty", qty);

        int rows = Globals.db.update("scanned_rtvs", values, "main_id=? and user_id=? and reason=?",
                new String[]{id, Globals.userId, reason});

        if (rows > 0) return;

        values.put("reason", reason);
        values.put("user_id", Globals.userId);
        values.put("main_id", id);
        Globals.db.insert("scanned_rtvs", null, values);
    }

    public static void clearScans() {
        Globals.db.delete("scanned_rtvs", "user_id=?", new String[]{Globals.userId});
    }

}
