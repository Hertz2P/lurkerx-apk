package com.sweetjuice.pkg.permissions;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sweetjuice.plugin.SweetJuicePlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import juiceapp.Juiceapp;

/**
 * PermissionsPlugin handles system runtime permissions.
 * It is background-aware for status checks but requires an active Activity for request prompts.
 */
public class PermissionsPlugin implements SweetJuicePlugin {
    private Context mContext;
    private AppCompatActivity mActivity;
    private static final int PERMISSION_REQ_CODE = 9911;

    @Override
    public String getDomain() { return "permissions"; }

    @Override
    public void onAttach(Context context) { 
        this.mContext = context; 
        if (context instanceof AppCompatActivity) {
            this.mActivity = (AppCompatActivity) context;
        }
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        if ("check".equals(action)) {
            String perm = parsePermissionFromJson(jsonArgsPayload);
            int result = ContextCompat.checkSelfPermission(mContext, perm);
            return result == PackageManager.PERMISSION_GRANTED ? "{\"status\":\"granted\"}" : "{\"status\":\"denied\"}";
        }

        if ("request".equals(action)) {
            if (mActivity == null) {
                return "{\"error\":\"No active UI to request permissions\"}";
            }
            String perm = parsePermissionFromJson(jsonArgsPayload);
            ActivityCompat.requestPermissions(mActivity, new String[]{perm}, PERMISSION_REQ_CODE);
            return "{\"status\":\"requested\"}";
        }

        if ("requestMultiple".equals(action)) {
            if (mActivity == null) {
                return "{\"error\":\"No active UI to request permissions\"}";
            }
            try {
                JSONObject obj = new JSONObject(jsonArgsPayload);
                JSONArray perms = obj.getJSONArray("permissions");
                String[] permArray = new String[perms.length()];
                for (int i = 0; i < perms.length(); i++) {
                    permArray[i] = perms.getString(i);
                }
                ActivityCompat.requestPermissions(mActivity, permArray, PERMISSION_REQ_CODE);
                return "{\"status\":\"requested\"}";
            } catch (JSONException e) {
                return errorJson("Invalid permissions payload: " + e.getMessage());
            }
        }

        return "{\"error\":\"Unknown action\"}";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE && permissions != null && grantResults != null) {
            for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                String permission = permissions[i];

                JSONObject result = new JSONObject();
                try {
                    result.put("permission", permission);
                    result.put("granted", granted);
                    String payload = "[" + result.toString() + "]";
                    Juiceapp.handleNativeAction("permissions:result", payload);
                } catch (JSONException e) {
                    Log.e("PermissionsPlugin", "Error creating result JSON", e);
                }
            }
        }
    }

    @Override public void onActivityResult(int r, int rc, Intent d) {}
    @Override public void onNewIntent(Intent intent) {}

    private String errorJson(String message) {
        try {
            return new JSONObject().put("error", message).toString();
        } catch (JSONException e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    private String parsePermissionFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.optString("permission", android.Manifest.permission.CAMERA);
        } catch (JSONException e) {
            return android.Manifest.permission.CAMERA;
        }
    }
}
