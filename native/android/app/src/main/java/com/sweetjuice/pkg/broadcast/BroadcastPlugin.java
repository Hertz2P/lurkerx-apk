package com.sweetjuice.pkg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import juiceapp.Juiceapp;
import androidx.core.content.ContextCompat;

/**
 * BroadcastPlugin bridges the Android system's Intent-based broadcast infrastructure 
 * to the Go backend, allowing for both receiving and sending system-wide broadcasts.
 */
public class BroadcastPlugin implements SweetJuicePlugin {
    private static final String TAG = "BroadcastPlugin";
    private Context mContext;
    private static final Set<String> mRegisteredActions = new HashSet<>();

    @Override
    public String getDomain() { return "broadcast"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            JSONObject args = new JSONObject(jsonArgsPayload);
            if ("register".equals(action)) {
                String intentAction = args.optString("action", "");
                registerDynamicReceiver(intentAction);
                return "{\"status\":\"registered\"}";
            }
            if ("send".equals(action)) {
                String intentAction = args.optString("action", "");
                JSONObject extras = args.optJSONObject("extras");
                sendBroadcast(intentAction, extras);
                return "{\"status\":\"sent\"}";
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
        return "{\"error\":\"Unknown action\"}";
    }

    private void registerDynamicReceiver(String action) {
        if (action.isEmpty() || mRegisteredActions.contains(action)) return;

        IntentFilter filter = new IntentFilter(action);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                post(intent.getAction(), intentToMap(intent));
            }
        }, filter);
        
        mRegisteredActions.add(action);
        Log.d(TAG, "Dynamic receiver registered for: " + action);
    }

    private void sendBroadcast(String action, JSONObject extras) throws JSONException {
        Intent intent = new Intent(action);
        if (extras != null) {
            java.util.Iterator<String> keys = extras.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object val = extras.get(key);
                if (val instanceof Integer) intent.putExtra(key, (Integer) val);
                else if (val instanceof Boolean) intent.putExtra(key, (Boolean) val);
                else intent.putExtra(key, val.toString());
            }
        }
        mContext.sendBroadcast(intent);
    }

    private static Map<String, Object> intentToMap(Intent intent) {
        Map<String, Object> map = new HashMap<>();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                map.put(key, extras.get(key));
            }
        }
        return map;
    }

    /**
     * Post a broadcast from Native to Go.
     */
    public static void post(String name, Object data) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("name", name);
            payload.put("data", data);
            
            String json = "[" + payload.toString() + "]";
            Juiceapp.handleNativeAction("broadcast:post", json);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create broadcast payload", e);
        }
    }

    @Override public void onActivityResult(int req, int res, Intent d) {}
    @Override public void onRequestPermissionsResult(int req, String[] p, int[] res) {}
    @Override public void onNewIntent(Intent intent) {}
}
