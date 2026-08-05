package com.lurkerx.gpsjob.notifjob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotifDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notifjob.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTIF = "notifications";

    public static final String COL_ID = "_id";
    public static final String COL_PACKAGE = "package_name";
    public static final String COL_TITLE = "title";
    public static final String COL_TEXT = "text";
    public static final String COL_TIME = "timestamp";
    public static final String COL_SENT = "sent";

    public NotifDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NOTIF + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_PACKAGE + " TEXT, " +
                        COL_TITLE + " TEXT, " +
                        COL_TEXT + " TEXT, " +
                        COL_TIME + " INTEGER, " +
                        COL_SENT + " INTEGER DEFAULT 0," +
                        "UNIQUE(" + COL_PACKAGE + ", " + COL_TITLE + ", " + COL_TEXT + ", " + COL_TIME + ")" +
                        ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIF);
        onCreate(db);
    }

    public void insertNotification(String pkg, String title, String text, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PACKAGE, pkg);
        values.put(COL_TITLE, title);
        values.put(COL_TEXT, text);
        values.put(COL_TIME, timestamp);
        db.insertWithOnConflict(TABLE_NOTIF, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public Cursor getUnsentNotifs(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_NOTIF,
                null,
                COL_SENT + "=0",
                null,
                null,
                null,
                COL_TIME + " DESC",
                String.valueOf(limit)
        );
    }

    public void markNotifAsSent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENT, 1);
        db.update(TABLE_NOTIF, values, "_id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
