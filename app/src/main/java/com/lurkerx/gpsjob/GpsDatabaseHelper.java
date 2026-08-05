package com.lurkerx.gpsjob;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GpsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gpsjob.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_GPS = "gps";

    public static final String COL_ID = "_id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_ACCURACY = "accuracy";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_SENT = "sent";

    public GpsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GPS_TABLE =
                "CREATE TABLE " + TABLE_GPS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_LATITUDE + " REAL NOT NULL, " +
                        COL_LONGITUDE + " REAL NOT NULL, " +
                        COL_ACCURACY + " REAL, " +
                        COL_TIMESTAMP + " INTEGER, " +
                        COL_SENT + " INTEGER DEFAULT 0" +
                        ")";
        db.execSQL(CREATE_GPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
        onCreate(db);
    }

    public void insertLocation(double latitude, double longitude, float accuracy, long timestamp) {

        if (latitude == 0.0 && longitude == 0.0) return;
        if (timestamp < 1_000_000_000_000L) return; // reject junk epoch

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LATITUDE, latitude);
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_ACCURACY, accuracy);
        values.put(COL_TIMESTAMP, timestamp);

        db.insert(TABLE_GPS, null, values);
    }

    public Cursor getUnsentLocations() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_GPS,
                new String[]{
                        COL_ID,
                        COL_LATITUDE,
                        COL_LONGITUDE,
                        COL_ACCURACY,
                        COL_TIMESTAMP
                },
                COL_SENT + " = 0",
                null,
                null,
                null,
                COL_TIMESTAMP + " ASC"
        );
    }
    public void markLocationAsSent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_SENT, 1);

        db.update(
                TABLE_GPS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }
    public void deleteSentLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GPS, COL_SENT + " = 1", null);
        db.close();
    }
}
