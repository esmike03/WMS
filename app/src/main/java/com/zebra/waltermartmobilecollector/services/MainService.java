package com.zebra.waltermartmobilecollector.services;

import com.zebra.waltermartmobilecollector.Globals;

public final class MainService {

    public static void clear(){
        Globals.db.execSQL("DELETE FROM main");
        Globals.db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'main'");
    }

}
