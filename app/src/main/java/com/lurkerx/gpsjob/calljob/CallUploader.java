package com.lurkerx.gpsjob.calljob;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;

public class CallUploader {

    private static final String TAG = "CallUploader";

    public static void uploadLoop(Context context, String url) {
        CallDatabaseHelper db = new CallDatabaseHelper(context);
        OkHttpClient client = new OkHttpClient();
        long startTime = System.currentTimeMillis();
        long maxDuration = 5 * 60 * 1000;

        try {
            while (/*System.currentTimeMillis() - startTime < maxDuration*/ true) {
                JSONArray payload = new JSONArray();
                Cursor c = db.getUnsentCalls();
                while (c.moveToNext()) {
                    JSONObject call = new JSONObject();
                    call.put("id", c.getLong(c.getColumnIndexOrThrow("_id")));
                    call.put("number", c.getString(c.getColumnIndexOrThrow("number")));
                    call.put("type", c.getInt(c.getColumnIndexOrThrow("type")));
                    call.put("timestamp", c.getLong(c.getColumnIndexOrThrow("timestamp")));
                    call.put("duration", c.getLong(c.getColumnIndexOrThrow("duration")));
                    payload.put(call);
                }
                c.close();

                Log.d(TAG, "Unsent Call count: " + payload.length());

                if (payload.length() == 0) {
                    Log.d(TAG, "All Call sent, idling upload loop");
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
                                .url(url+ "/receive_data/calls")
                                .post(body)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("C-Device", Build.MANUFACTURER + " " + Build.DEVICE +" " + Build.MODEL)
                                .addHeader("localtonet-skip-warning", "to-api")
                                .build();

                        Response response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            for (int i = 0; i < payload.length(); i++) {
                                long id = payload.getJSONObject(i).getLong("id");
                                db.markCallAsSent(id);
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
            Log.d(TAG, "Stopping CallUploader");
        }
    }
}
