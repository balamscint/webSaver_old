package com.wpdf.dbConfig;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Dbhelper extends SQLiteOpenHelper {

    public static final String PDF_LIST = "pdf_list";
    public static final String SYS = "sys";
    public static final String NOTIFICATIONS = "notifications";
    public static final String SYS_ID = "sysId";
    private static final String DATABASE_NAME = "websaver";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_CREATE_PDF = "create table " + PDF_LIST
            + " (pdfId integer primary key autoincrement,path text not null);";
    private static final String TABLE_CREATE_SYS = "create table " + SYS
            + " (" + SYS_ID + " integer primary key " +
            "autoincrement,uuid text,account text);";

    private static final String TABLE_CREATE_NOTIFICATIONS = "create table " + NOTIFICATIONS
            + " (notification_id integer primary key autoincrement, " +
            "message text not null, read integer);";


    Dbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE_PDF);
        database.execSQL(TABLE_CREATE_SYS);
        database.execSQL(TABLE_CREATE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {

        if (oldVersion <= 2 && newVersion == 3) {
            database.execSQL(TABLE_CREATE_NOTIFICATIONS);
        } else {
            dropandcreate(database);
        }
    }

    private void dropandcreate(SQLiteDatabase database) {
        drop(database);
        onCreate(database);
    }

    private void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + PDF_LIST);
        database.execSQL("DROP TABLE IF EXISTS " + SYS);
        database.execSQL("DROP TABLE IF EXISTS " + NOTIFICATIONS);
    }
}