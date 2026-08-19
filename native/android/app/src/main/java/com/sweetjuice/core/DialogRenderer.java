package com.sweetjuice.core;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import juiceapp.Juiceapp;

/**
 * DialogRenderer facilitates the display of native Android {@link AlertDialog}s 
 * triggered by the Go backend via the {@code ui:dialog} widget type.
 */
class DialogRenderer {
    private static final String TAG = "SweetJuice";
    private final Context context;

    DialogRenderer(Context context) {
        this.context = context;
    }

    /**
     * Renders and displays a native Android {@link android.app.AlertDialog} 
     * from a Sweet Juice dialog node.
     * 
     * @param node the JSON representation of the dialog.
     */
    void showNativeDialog(JSONObject node) {
        try {
            String title = node.optString("title", "");
            String message = node.optString("message", "");
            String buttonText = node.optString("buttonText", "OK");
            String id = node.optString("id", "");

            new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonText, (dialog, which) -> {
                    try {
                        JSONObject event = new JSONObject();
                        event.put("id", id);
                        event.put("name", "confirm");
                        JSONObject data = new JSONObject();
                        data.put("button", buttonText);
                        event.put("data", data);
                        String payload = event.toString();
                        Juiceapp.handleMessageFromFrontend("ui:event", payload);
                    } catch (Exception e) {
                        Log.e(TAG, "UIManager: dialog confirm event failed", e);
                    }
                })
                .setCancelable(false)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "UIManager: showNativeDialog failed", e);
        }
    }
}
