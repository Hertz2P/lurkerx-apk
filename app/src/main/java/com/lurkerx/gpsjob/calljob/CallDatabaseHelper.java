package com.lurkerx.gpsjob.calljob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calljob.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CALLS = "calls";
    public static final String COL_ID = "_id";
    public static final String COL_NUMBER = "number";
    public static final String COL_TYPE = "type";
    public static final String COL_DATE = "timestamp";
    public static final String COL_DURATION = "duration";
    public static final String COL_SENT = "sent";

    public CallDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE =
                "CREATE TABLE " + TABLE_CALLS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NUMBER + " TEXT NOT NULL, " +
                        COL_TYPE + " INTEGER, " +
                        COL_DATE + " INTEGER, " +
                        COL_DURATION + " INTEGER, " +
                        COL_SENT + " INTEGER DEFAULT 0," +
                        "UNIQUE(" + COL_NUMBER + ", " + COL_TYPE + ", " + COL_DATE + ", " + COL_DURATION + ")" +
                        ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLS);
        onCreate(db);
    }

    public void insertCall(String number, int type, long date, long duration) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NUMBER, number);
        values.put(COL_TYPE, type);
        values.put(COL_DATE, date);
        values.put(COL_DURATION, duration);

        db.insertWithOnConflict(TABLE_CALLS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getUnsentCalls() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_CALLS,
                null,
                COL_SENT + " = 0",
                null,
                null,
                null,
                COL_DATE + " ASC"
        );
    }

    public void markCallAsSent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENT, 1);
        db.update(TABLE_CALLS, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteSentCalls() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CALLS, COL_SENT + " = 1", null);
    }
}
