package com.lurkerx.gpsjob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.lurkerx.gpsjob.notifjob.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed, starting background tasks");
            // Trigger the centralized task starter
            NotificationService.startAllBackgroundTasks(context.getApplicationContext());
        }
    }
}
