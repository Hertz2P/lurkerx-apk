package com.lurkerx.gpsjob;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GpsUploader {

    private static final String TAG = "GpsUploader";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void uploadLoop(Context context, String url) {
        new Thread(() -> {
            GpsDatabaseHelper db = new GpsDatabaseHelper(context);
            OkHttpClient client = new OkHttpClient();

            while (true) {
                try {
                    JSONArray payload = new JSONArray();
                    var cursor = db.getUnsentLocations();
                    while (cursor.moveToNext()) {
                        JSONObject gps = new JSONObject();
                        gps.put("id", cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                        gps.put("latitude", cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                        gps.put("longitude", cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                        gps.put("timestamp", cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                        gps.put("accuracy", cursor.getDouble(cursor.getColumnIndexOrThrow("accuracy")));
                        payload.put(gps);
                    }
                    cursor.close();

                    Log.d(TAG, "Unsent Gps count: " + payload.length());
                    if (payload.length() == 0) {
                        Thread.sleep(10_000); // no data, wait 10s
                        continue;
                    }

                    // Send to server asynchronously
                    boolean[] batchSent = {false};
                    int retryCount = 0;

                    while (!batchSent[0] && retryCount < 5) {
                        RequestBody body = RequestBody.create(payload.toString(), JSON);
                        Request request = new Request.Builder()
                                .url(url+ "receive_data/gps")
                                .post(body)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("C-Device", Build.MANUFACTURER + " " + Build.DEVICE + " " + Build.MODEL)
                                .addHeader("localtonet-skip-warning", "to-api")
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                // mark each uploaded GPS as sent
                                for (int i = 0; i < payload.length(); i++) {
                                    long id = payload.getJSONObject(i).getLong("id");
                                    db.markLocationAsSent(id);
                                }
                                batchSent[0] = true;
                                Log.d(TAG, "Batch uploaded successfully");
                            } else {
                                Log.w(TAG, "Server error: " + response.code());
                                retryCount++;
                                Thread.sleep(5_000);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Network error, retrying batch", e);
                            retryCount++;
                            Thread.sleep(5_000);
                        }
                    }

                    if (!batchSent[0]) {
                        Log.w(TAG, "Failed to upload batch after retries, waiting for next cycle");
                    }

                    Thread.sleep(5_000); // wait 5s before next upload attempt

                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in upload loop", e);
                    try {
                        Thread.sleep(10_000);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }
            }
        }).start();
    }
}
