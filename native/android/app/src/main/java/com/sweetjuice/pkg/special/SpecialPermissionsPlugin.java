package com.sweetjuice.pkg.special;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecialPermissionsPlugin implements SweetJuicePlugin {

    private static final String TAG = "SpecialPermissions";
    private Context mContext;

    @Override
    public String getDomain() { return "special"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            JSONObject args = new JSONObject(jsonArgsPayload);
            String type = args.optString("type", "");

            if ("request".equals(action)) {
                return requestSpecialPermission(type);
            }

            if ("check".equals(action)) {
                return checkSpecialPermission(type);
            }

            return new JSONObject().put("error", "Unknown action").toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String requestSpecialPermission(String type) {
        Intent intent;
        if ("accessibility".equals(type)) {
            intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        } else if ("all_files_access".equals(type)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + mContext.getPackageName()));
            } else {
                return errorJson("All files access requires Android 11+");
            }
        } else if ("battery_exemption".equals(type)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = mContext.getPackageName();
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                if (pm == null) {
                    return errorJson("PowerManager not available");
                }
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(android.net.Uri.parse("package:" + packageName));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return okJson("launched");
                } else {
                    return okJson("already_exempted");
                }
            } else {
                return errorJson("Battery exemption requires Android 6.0+");
            }
        } else if ("app_settings".equals(type)) {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + mContext.getPackageName()));
        } else if ("notification_access".equals(type)) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        } else {
            return errorJson("Unknown special permission type: " + type);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextCompat.startActivity(mContext, intent, null);
        return okJson("launched");
    }

    private String checkSpecialPermission(String type) {
        boolean granted;
        if ("accessibility".equals(type)) {
            granted = hasAccessibilityAccess();
        } else if ("all_files_access".equals(type)) {
            granted = hasAllFilesAccess();
        } else if ("battery_exemption".equals(type)) {
            granted = hasBatteryExemption();
        } else if ("notification_access".equals(type)) {
            granted = hasNotificationAccess();
        } else {
            return errorJson("Unknown special permission type: " + type);
        }
        try {
            return new JSONObject().put("granted", granted).put("status", granted ? "granted" : "denied").toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private boolean hasAccessibilityAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String enabledServices = Settings.Secure.getString(
                    mContext.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServices == null) return false;
            String ourService = mContext.getPackageName() + "/" + mContext.getPackageName() + ".SweetJuiceAccessibilityService";
            return enabledServices.contains(ourService);
        }
        return false;
    }

    private boolean hasAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private boolean hasBatteryExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(mContext.getPackageName());
        }
        return true;
    }

    private boolean hasNotificationAccess() {
        String enabledListeners = Settings.Secure.getString(
                mContext.getContentResolver(),
                "enabled_notification_listeners");
        if (enabledListeners == null) return false;
        String ourListener = mContext.getPackageName() + "/com.sweetjuice.pkg.notifications.SweetJuiceNotificationListener";
        return enabledListeners.contains(ourListener);
    }

    private String okJson(String status) {
        try {
            return new JSONObject().put("status", status).toString();
        } catch (JSONException e) {
            return "{\"status\":\"" + status + "\"}";
        }
    }

    private String errorJson(String message) {
        try {
            return new JSONObject().put("error", message).toString();
        } catch (JSONException e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {}
    @Override public void onActivityResult(int r, int rc, Intent d) {}
    @Override public void onNewIntent(Intent intent) {}
}
