package com.zebra.waltermartmobilecollector.services;

import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;

import java.util.ArrayList;

public final class ReasonService {

    public static ArrayList<String> getDescs() {
        ArrayList<String> reasons = new ArrayList<>();

        Cursor c = Globals.db.rawQuery("select description from reasons", null);
        while (c.moveToNext())
            reasons.add(c.getString(0));
        c.close();
        return reasons;
    }

    public static boolean hasCodes() {
        boolean has = false;
        Cursor c = Globals.db.rawQuery("select code from reasons limit 1", null);
        if (c.getCount() > 0)
            has = true;
        c.close();
        return has;
    }

    public static ArrayList<String> getCodes() {
        ArrayList<String> reasons = new ArrayList<>();

        Cursor c = Globals.db.rawQuery("select code from reasons", null);
        while (c.moveToNext())
            reasons.add(c.getString(0));
        c.close();
        return reasons;
    }

    public static void clear(){
        Globals.db.execSQL("DELETE FROM reasons");
        Globals.db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'reasons'");
    }

}
