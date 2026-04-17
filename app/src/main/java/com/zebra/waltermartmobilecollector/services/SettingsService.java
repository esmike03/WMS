package com.zebra.waltermartmobilecollector.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;

import com.zebra.waltermartmobilecollector.Globals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class SettingsService {

    public static void fetchSettings() {
        Cursor c = Globals.db.rawQuery("select is_wms, store, ip_address, ftp_user, ftp_password, mms_ip_address, mms_ftp_user, mms_ftp_password, masterfile_updated_at, mms_ftp_path from settings", null);
        while (c.moveToNext()) {
            Globals.setSettings(c.getString(0).equals("1"), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
            Globals.setMmsSettings(c.getString(5), c.getString(6), c.getString(7), c.getString(9)); // ✅ pass path
            Globals.masterfileUpdatedAt = c.getString(8);
        }
        c.close();
    }

    // ✅ KEEP THIS
    public static void updateMMS(String ip, String user, String password, String path) {
        ContentValues values = new ContentValues();
        values.put("mms_ip_address", ip);
        values.put("mms_ftp_user", user);
        values.put("mms_ftp_password", password);
        values.put("mms_ftp_path", path);
        Globals.db.update("settings", values, null, null);
        Globals.resetSettings();
    }


    public static void fetchMasterfileUpdatedAt() {
        Cursor c = Globals.db.rawQuery("select masterfile_updated_at from settings limit 1", null);

        while (c.moveToNext())
            Globals.masterfileUpdatedAt = c.getString(0);
        c.close();
    }

    public static void updateMasterfileUpdatedAt() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

//        String date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now());
        String date = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").format(LocalDateTime.now());
        Globals.masterfileUpdatedAt = date;
        ContentValues values = new ContentValues();
        values.put("masterfile_updated_at", date);
        Globals.db.update("settings", values, null, null);
    }

    public static void update(boolean isWMS, String storeCode, String ip, String user, String password) {
        ContentValues values = new ContentValues();
        values.put("is_wms", isWMS ? 1 : 0);
        values.put("store", storeCode);
        values.put("ip_address", ip);
        values.put("ftp_user", user);
        values.put("ftp_password", password);
        Globals.db.update("settings", values, null, null);
        Globals.resetSettings();
    }


}
