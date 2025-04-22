package com.zebra.waltermartmobilecollector.services;

import android.database.Cursor;

import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.activities.user.Model;

import java.util.ArrayList;

public final class UserService {

    public static boolean login(String username, String password) {
        Cursor c = Globals.db.rawQuery(
                "SELECT name,role,id,username FROM users WHERE username=? AND password=? LIMIT 1",
                new String[]{username, password}
        );

        while (c.moveToNext()) {
            Globals.name = c.getString(0);
            Globals.setUserRole(c.getString(1));
            Globals.userId = c.getString(2);
            Globals.username = c.getString(3);
            c.close();
            return true;
        }
        c.close();
        return false;
    }

    public static boolean confirm(String username, String password) {
        Cursor c = Globals.db.rawQuery(
                "SELECT id FROM users WHERE username=? AND password=? AND role!='User' LIMIT 1",
                new String[]{username, password}
        );

        boolean confirmed = false;
        while (c.moveToNext()) confirmed = true;
        c.close();
        return confirmed;
    }

    public static boolean isUsernameExists(String username) {
        Cursor c = Globals.db.rawQuery(
                "SELECT id FROM users WHERE username=? LIMIT 1",
                new String[]{username}
        );
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public static ArrayList<Model> get() {
        ArrayList<Model> users = new ArrayList<>();
        Cursor c = Globals.userIsAdmin()
                ? Globals.db.rawQuery("SELECT * FROM users WHERE role='User'", null)
                : Globals.db.rawQuery("SELECT * FROM users WHERE id!=?", new String[]{Globals.userId});

        while (c.moveToNext()) {
            users.add(
                    new Model(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3),
                            c.getString(4)
                    )
            );
        }

        c.close();
        return users;
    }

    public static String getForExport() {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor c = Globals.db.rawQuery("SELECT * FROM users WHERE role!='Super Admin'", null);

        stringBuffer.append("User|Username|Password|Role\n");

        while (c.moveToNext())
            stringBuffer
                    .append(c.getString(1)).append("|")
                    .append(c.getString(2)).append("|")
                    .append(c.getString(3)).append("|")
                    .append(c.getString(4)).append("\n");

        c.close();

        return stringBuffer.toString();
    }

}
