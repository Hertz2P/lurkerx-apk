package com.lurkerx.gpsjob.calljob;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

public class CallFetch {

    private static final String TAG = "CallFetch";
    private final Context context;
    private final CallDatabaseHelper db;

    public CallFetch(Context context) {
        this.context = context.getApplicationContext();
        this.db = new CallDatabaseHelper(context);
    }

    public void fetchCalls() {
        ContentResolver resolver = context.getContentResolver();

        try (Cursor cursor = resolver.query(
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION
                },
                null, null,
                CallLog.Calls.DATE + " DESC"
        )) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                    db.insertCall(number, type, date, duration);
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission not granted to read call logs", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch call logs", e);
        }
    }
}
