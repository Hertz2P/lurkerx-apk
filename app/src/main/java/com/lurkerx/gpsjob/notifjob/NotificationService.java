package com.lurkerx.gpsjob.notifjob;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationService extends NotificationListenerService {

    private static boolean isConnected = false;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isConnected = true;
        Log.d("NotifService", "Notification access GRANTED");
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
