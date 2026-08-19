package com.sweetjuice.pkg.system;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * SystemPlugin provides detailed information about the Android OS and device hardware.
 */
public class SystemPlugin implements SweetJuicePlugin {
    @Override
    public String getDomain() { return "system"; }

    @Override
    public void onAttach(Context context) {}

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            switch (action) {
                case "getInfo":
                    return getInfo();
                default:
                    return "{\"error\":\"Unknown action\"}";
            }
        } catch (JSONException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getInfo() throws JSONException {
        JSONObject info = new JSONObject();
        info.put("system_name", "Android");
        info.put("system_version", Build.VERSION.RELEASE);
        info.put("sdk_int", Build.VERSION.SDK_INT);
        info.put("release", Build.VERSION.RELEASE);
        info.put("codename", Build.VERSION.CODENAME);
        info.put("manufacturer", Build.MANUFACTURER);
        info.put("brand", Build.BRAND);
        info.put("model", Build.MODEL);
        info.put("board", Build.BOARD);
        info.put("device", Build.DEVICE);
        info.put("product", Build.PRODUCT);
        info.put("hardware", Build.HARDWARE);
        info.put("base_os", Build.VERSION.BASE_OS);
        info.put("security_patch", Build.VERSION.SECURITY_PATCH);
        info.put("is_physical_device", !isEmulator());
        return info.toString();
    }

    private boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }

    @Override public void onActivityResult(int req, int res, Intent d) {}
    @Override public void onRequestPermissionsResult(int req, String[] p, int[] res) {}
    @Override public void onNewIntent(Intent intent) {}
}
