package com.wpdf.dbConfig;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Dbcon {

    private Context context;
    private SQLiteDatabase database;

    public Dbcon(Context context) {
        this.context = context;
        open();
    }

    private Dbcon open() throws SQLException {
        Dbhelper dbHelper = new Dbhelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            dbHelper.onCreate(database);

        }
        return this;
    }

    public void close() {
        if (database != null && database.isOpen())
            database.close();
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }


    public long insert(String values[], String names[], String tbl) {
        ContentValues initialValues = createContentValues(values, names);

        return database.insert(tbl, null, initialValues);
    }

    public boolean delete(String tbl, String where, String fValue[]) {
        return database.delete(tbl, where, fValue) > 0;
    }


    public Cursor fetch(String tbl, String names[], String select, String args[], String order) {

        Cursor mCursor = database.query(true, tbl, names,
                select, args, null, null, order, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean update(String where, String values[], String names[], String tbl, String args[]) {

        ContentValues updateValues = createContentValues(values, names);

        return database.update(tbl, updateValues, where, args) > 0;
    }

    private ContentValues createContentValues(String values[], String names[]) {
        ContentValues values1 = new ContentValues();

        for (int i = 0; i < values.length; i++) {
            values1.put(names[i], values[i]);
        }

        return values1;
    }

}