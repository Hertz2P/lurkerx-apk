package com.custompackage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.sweetjuice.plugin.SweetJuicePlugin;

public class CustomPlugin implements SweetJuicePlugin {
    private static final String TAG = "CustomPlugin";
    private Context mContext;
    private Activity mActivity;

    @Override
    public String getDomain() { return "custom"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            if ("show_launch".equals(action)) {
                return showLaunch();
            }
            if ("read_asset".equals(action)) {
                return readAsset(jsonArgsPayload);
            }
            return "{\"error\":\"Unknown action\"}";
        } catch (Exception e) {
            Log.e(TAG, "Action failed: " + action, e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String readAsset(String jsonArgsPayload) throws Exception {
        String filename = "choices.ini";
        try (java.io.InputStream is = mContext.getAssets().open(filename);
             java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8")) {
            scanner.useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            android.util.Log.d(TAG, "read_asset: loaded " + filename + " (" + content.length() + " bytes)");
            return content;
        }
    }

    private String showLaunch() {
        PackageManager pm = mContext.getPackageManager();
        
        // Dynamically resolve the main activity name to hide it
        ComponentName mainActivity = new ComponentName(
                mContext,
                mContext.getPackageName() + ".SweetJuiceActivity"
        );

        pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
        pm.setComponentEnabledSetting(
                new ComponentName(mContext, LaunchActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        Intent intent = new Intent(mContext, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        if (mActivity != null) {
            mActivity.finish();
        }
        return "{\"status\":\"launched\"}";
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {}
    @Override public void onActivityResult(int r, int rc, android.content.Intent d) {}
    @Override public void onNewIntent(android.content.Intent intent) {}
}
