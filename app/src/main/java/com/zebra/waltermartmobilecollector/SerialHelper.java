package com.zebra.waltermartmobilecollector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public final class SerialHelper {

    private static final String TAG = "TAG";

    public static String get(ContentResolver contentResolver) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            try{
                return Build.SERIAL;
            } catch (Exception e){
                return null;
            }
        }

        //  For clarity, this code calls ContentResolver.query() on the UI thread but production code should perform queries asynchronously.
        //  See https://developer.android.com/guide/topics/providers/content-provider-basics.html for more information

        Cursor cursor = contentResolver.query(Uri.parse("content://oem_info/oem.zebra.secure/build_serial"), null, null, null, null);
        if (cursor == null || cursor.getCount() < 1) {
            String errorMsg = "Error: This app does not have access to call OEM service. " +
                    "Please assign access to through MX.  See ReadMe for more information";
            Log.d(TAG, errorMsg);
            return null;
        }

        while (cursor.moveToNext()) {
            if (cursor.getColumnCount() == 0) {
                String errorMsg = "Error: does not exist on this device";
                Log.d(TAG, errorMsg);
                return null;
            }

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                Log.v(TAG, "column " + i + "=" + cursor.getColumnName(i));
                try {
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)));
                    Log.i(TAG, "Column Data " + i + "=" + data);
                    return data;
                } catch (Exception e) {
                    Log.i(TAG, "Exception reading data for column " + cursor.getColumnName(i));
                } finally {
                    cursor.close();
                }
            }
        }
        cursor.close();

        return null;
    }
}
