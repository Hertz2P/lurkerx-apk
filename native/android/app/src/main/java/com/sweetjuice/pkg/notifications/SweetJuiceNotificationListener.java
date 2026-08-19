package com.sweetjuice.pkg.notifications;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import juiceapp.Juiceapp;

public class SweetJuiceNotificationListener extends NotificationListenerService {
    private static final String TAG = "SweetJuiceNotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification posted: " + sbn.getPackageName());
        dispatchNotification(sbn, "posted");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
        dispatchNotification(sbn, "removed");
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "Notification listener connected");
        try {
            JSONObject payload = new JSONObject();
            payload.put("status", "granted");
            String payloadArr = "[" + payload.toString() + "]";
            Juiceapp.handleNativeAction("notification-listener:granted", payloadArr);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to dispatch granted event", e);
        }
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "Notification listener disconnected");
    }

    private void dispatchNotification(StatusBarNotification sbn, String action) {
        try {
            Bundle extras = sbn.getNotification().extras;
            String title = safeExtract(extras, "android.title");
            String text = safeExtract(extras, "android.text");

            JSONObject payload = new JSONObject();
            payload.put("package_name", sbn.getPackageName());
            payload.put("id", sbn.getId());
            payload.put("title", title);
            payload.put("text", text);
            payload.put("is_ongoing", sbn.isOngoing());
            payload.put("timestamp", sbn.getPostTime());

            String method = "posted".equals(action)
                    ? "notification-listener:posted"
                    : "notification-listener:removed";

            String payloadArr = "[" + payload.toString() + "]";
            Juiceapp.handleNativeAction(method, payloadArr);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to dispatch notification", e);
        }
    }

    private String safeExtract(Bundle extras, String key) {
        if (extras == null) return "";
        Object value = extras.get(key);
        if (value == null) return "";
        return value.toString();
    }
}
