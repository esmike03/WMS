package com.zebra.waltermartmobilecollector.activities.price_check;

import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;

public final class Service {

    private static String getType(String type) {
        if (type.equalsIgnoreCase("CC")) return "Concessionaire";
        if (type.equalsIgnoreCase("CO")) return "Consignment";
        if (type.equalsIgnoreCase("MC")) return "ModCon";
        if (type.equalsIgnoreCase("02")) return "Commodity";
        if (type.equalsIgnoreCase("03")) return "Special Orders";
        if (type.equalsIgnoreCase("04")) return "Dump";
        if (type.equalsIgnoreCase("05")) return "Virtual Inventory";
        if (type.equalsIgnoreCase("07")) return "Core";
        if (type.equalsIgnoreCase("12")) return "Non-Merchandise";
        if (type.equalsIgnoreCase("21")) return "Class Control";

        return "Outright";
    }

    public static Model find(String barcode) {
        Model model = null;
        Cursor c = Globals.db.rawQuery(
                "SELECT description,sku,regular_price,promo_price,vendor,type,cpo FROM main WHERE barcode=? LIMIT 1",
                new String[]{barcode}
        );
        while (c.moveToNext()) {
            model = new Model(
                    barcode,
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    getType(c.getString(5))
//                    Helper.isOutright(
//                            c.getString(5),
//                            c.getString(6)
//                    )
            );
        }
        c.close();
        return model;
    }

}
