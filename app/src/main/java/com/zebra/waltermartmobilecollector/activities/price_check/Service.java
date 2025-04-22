package com.zebra.waltermartmobilecollector.activities.price_check;

import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;

public final class Service {

    public static Model find(String barcode){
        Model model = null;
        Cursor c = Globals.db.rawQuery(
                "SELECT description,sku,regular_price,promo_price,vendor,type,cpo FROM main WHERE barcode=? LIMIT 1",
                new String[]{barcode}
        );
        while (c.moveToNext()){
            model = new Model(
                    barcode,
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    Helper.isOutright(
                            c.getString(5),
                            c.getString(6)
                    )
            );
        }
        c.close();
        return model;
    }

}
