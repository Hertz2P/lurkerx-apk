package com.sweetjuice.pkg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import juiceapp.Juiceapp;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            
            Log.d("SweetJuice", "System boot detected. Starting backend...");
            
            // Wake up the Go backend
            Juiceapp.startApplication();
            
            // Signal Go with the actual system intent action
            BroadcastPlugin.post(intent.getAction(), "{}");
        }
    }
}
