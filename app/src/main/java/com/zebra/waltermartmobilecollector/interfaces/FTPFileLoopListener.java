package com.zebra.waltermartmobilecollector.interfaces;

import android.database.sqlite.SQLiteStatement;

import java.util.List;

public interface FTPFileLoopListener {

    boolean onRow(SQLiteStatement statement, List<String> rows);

}
