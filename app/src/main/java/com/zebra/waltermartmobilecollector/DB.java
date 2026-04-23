package com.zebra.waltermartmobilecollector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zebra.waltermartmobilecollector.services.Encryptor;

public class DB extends SQLiteOpenHelper {

    private static final int DB_VERSION = 12;

    public DB(Context context) {
        super(context, "waltermart_mobile_collector", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");

        db.execSQL("CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT,"
                + "username TEXT,"
                + "password TEXT,"
                + "role TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS settings (" +
                "is_wms BOOLEAN," +
                "store TEXT," +
                "ip_address TEXT," +
                "ftp_user TEXT," +
                "ftp_password TEXT," +
                "mms_ip_address TEXT," +
                "mms_ftp_user TEXT," +
                "mms_ftp_password TEXT," +
                "masterfile_updated_at TEXT)");

        // buy is the factor
        db.execSQL("CREATE TABLE IF NOT EXISTS main ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "sku TEXT,"
                + "description TEXT,"
                + "long_desc TEXT,"
                + "short_desc TEXT,"
                + "type TEXT,"
                + "barcode TEXT,"
                + "upc_type TEXT,"
                + "vendor TEXT,"
                + "dept TEXT,"
                + "subdept TEXT,"
                + "class TEXT,"
                + "subclass TEXT,"
                + "buyer TEXT,"
                + "selling TEXT,"
                + "buy TEXT,"
                + "sell_buy TEXT,"
                + "inner_pack TEXT,"
                + "case_pack TEXT,"
                + "manuf_list TEXT,"
                + "cube TEXT,"
                + "ucost TEXT,"
                + "set_code TEXT,"
                + "rep_code TEXT,"
                + "event_no TEXT,"
                + "event_type TEXT,"
                + "regular_price TEXT,"
                + "promo_price TEXT,"
                + "start_date TEXT,"
                + "end_date TEXT,"
                + "deal_price_method TEXT,"
                + "deal_price_qty TEXT,"
                + "deal_price_amount TEXT,"
                + "ols TEXT,"
                + "cpo TEXT,"
                + "dti TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS pos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "po TEXT,"
                + "sku TEXT,"
                + "qty TEXT,"
                + "factor TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS reasons ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "code TEXT,"
                + "description TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS scanned_pos ("
                + "user_id INTEGER,"
                + "po_id INTEGER,"
                + "main_id INTEGER,"
                + "qty TEXT,"
                + "si_num TEXT,"
                + "username TEXT,"
                + "last_scanned_date TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (po_id) REFERENCES pos(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS scanned_rtvs ("
                + "user_id INTEGER,"
                + "main_id INTEGER,"
                + "reason TEXT,"
                + "qty TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (main_id) REFERENCES main(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS scanned_sts ("
                + "user_id INTEGER,"
                + "main_id INTEGER,"
                + "is_per_piece BOOLEAN DEFAULT 1,"
                + "from_loc TEXT,"
                + "to_loc TEXT,"
                + "qty TEXT,"
                + "si_num TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (main_id) REFERENCES main(id) ON DELETE CASCADE)");

        updates(db);

        /** password of superadmin = W@lt3rm4rt  **/
        /**   Encrypted W@lt3rm4rt = rwJjkzRZEnJYjxqNjISBjA==  **/
        String password;
        try {
            password = Encryptor.encrypt("W@lt3rm4rt");
        } catch (Exception _){
            /** password = password **/
            password = "y4hcbc+vCc4LYI/+y6O/cw==";
        }

        db.execSQL("INSERT INTO users (name,username,password,role) VALUES " +
                "('SuperAdmin', 'superadmin', '"+password+"', 'Super Admin')");
        db.execSQL("INSERT INTO settings (masterfile_updated_at, is_wms, store, ip_address, ftp_user, ftp_password, mms_ip_address, mms_ftp_user, mms_ftp_password, mms_ftp_path) VALUES " +
                "('', 1, '401', '192.168.0.122','ftp-user','1234', '192.168.0.71', 'ftp-user', '1234', '')");
    }

    private void updates(SQLiteDatabase db){
        v3(db);
        v5(db);
        v6(db);
        v12(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) v2(db);
        if (oldVersion < 3) v3(db);
        if (oldVersion < 4) v4(db);
        if (oldVersion < 5) v5(db);
        if (oldVersion < 6) v6(db);
        if (oldVersion < 7) v7(db);
        if (oldVersion < 8) v8(db);
        if (oldVersion < 9) v9(db);
        if (oldVersion < 10) v10(db);
        if (oldVersion < 11) v11(db);
        if (oldVersion < 12) v12(db);
    }

    private void v2(SQLiteDatabase db){
        db.execSQL("ALTER TABLE scanned_pos ADD COLUMN main_id INTEGER");
    }
    private void v3(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS pcount_locations (name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS scanned_pcounts ("
                + "user_id INTEGER,"
                + "main_id INTEGER,"
                + "location TEXT,"
                + "qty TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (main_id) REFERENCES main(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS pcount_withdrawals ("
                + "user_id INTEGER,"
                + "main_id INTEGER,"
                + "location TEXT,"
                + "qty TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (main_id) REFERENCES main(id) ON DELETE CASCADE)");
    }
    private void v4(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS scanned_stock_counts ("
                + "user_id INTEGER,"
                + "main_id INTEGER,"
                + "location TEXT,"
                + "qty TEXT,"
                + "option TEXT,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (main_id) REFERENCES main(id) ON DELETE CASCADE)");
    }
    private void v5(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS pcount_locations");

        db.execSQL("ALTER TABLE scanned_pcounts ADD COLUMN option TEXT DEFAULT '1'");
        db.execSQL("ALTER TABLE scanned_pcounts ADD COLUMN single BOOLEAN DEFAULT 1");
    }
    private void v6(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS pcount_parent_childs (" +
                "parent_id INTEGER," +
                "child_id INTEGER," +
                "qty TEXT," +
                "FOREIGN KEY (parent_id) REFERENCES main(id) ON DELETE CASCADE," +
                "FOREIGN KEY (child_id) REFERENCES main(id) ON DELETE CASCADE)");
    }

    private void v7(SQLiteDatabase db){
        db.execSQL("ALTER TABLE scanned_pos ADD COLUMN si_num TEXT");
        db.execSQL("ALTER TABLE scanned_sts ADD COLUMN si_num TEXT");
    }


    private void v8(SQLiteDatabase db){
        try {
            db.execSQL("ALTER TABLE scanned_sts ADD COLUMN si_num TEXT");
        } catch (Exception e) {}
        try {
            db.execSQL("ALTER TABLE scanned_sts ADD COLUMN main_id INTEGER");
        } catch (Exception e) {}
    }

    private void v9(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE scanned_pos ADD COLUMN username TEXT");
        } catch (Exception e) {}
        try {
            db.execSQL("ALTER TABLE scanned_pos ADD COLUMN last_scanned_date TEXT");
        } catch (Exception e) {}
    }

    private void v10(SQLiteDatabase db){
        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ip_address TEXT DEFAULT ''");
        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ftp_user TEXT DEFAULT ''");
        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ftp_password TEXT DEFAULT ''");
    }

    private void v11(SQLiteDatabase db){
//        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ip_address TEXT DEFAULT ''");
//        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ftp_user TEXT DEFAULT ''");
//        db.execSQL("ALTER TABLE settings ADD COLUMN mms_ftp_password TEXT DEFAULT ''");
    }

    private void v12(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE settings ADD COLUMN mms_ftp_path TEXT DEFAULT ''");
        } catch (Exception e) {}
    }
}