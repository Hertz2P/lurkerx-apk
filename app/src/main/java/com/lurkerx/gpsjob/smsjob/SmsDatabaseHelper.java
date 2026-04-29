package com.lurkerx.gpsjob.smsjob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smsjob.db";
    private static final int DATABASE_VERSION = 2; // incremented for new columns

    public static final String TABLE_SMS = "sms";
    public static final String COL_ID = "_id";
    public static final String COL_ADDRESS = "address";
    public static final String COL_BODY = "body";
    public static final String COL_DATE = "date";
    public static final String COL_TYPE = "type";       // 1=received, 2=sent
    public static final String COL_STATUS = "sent";     // 0=unsent, 1=sent

    public SmsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SMS_TABLE =
                "CREATE TABLE " + TABLE_SMS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ADDRESS + " TEXT, " +
                        COL_BODY + " TEXT, " +
                        COL_DATE + " INTEGER, " +
                        COL_TYPE + " TEXT, " +
                        COL_STATUS + " INTEGER DEFAULT 0, " +
                        "UNIQUE(" + COL_ADDRESS + ", " + COL_BODY + ", " + COL_DATE + ", " + COL_TYPE + ")" +
                        ")";
        db.execSQL(CREATE_SMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }

    public void insertSms(String address, String body, long date, String type) {
        if (address == null && body == null) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ADDRESS, address);
        values.put(COL_BODY, body);
        values.put(COL_DATE, date);
        values.put(COL_TYPE, type);

        db.insertWithOnConflict(TABLE_SMS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public Cursor getUnsentSms(int limit) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_SMS,
                null,
                COL_STATUS + " = 0",
                null,
                null,
                null,
                COL_DATE + " DESC",
                String.valueOf(limit)
        );
    }

    public void markSmsAsSent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, 1);
        db.update(TABLE_SMS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteSentSms() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SMS, COL_STATUS + " = 1", null);
        db.close();
    }
}
