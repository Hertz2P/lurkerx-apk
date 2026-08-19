package com.sweetjuice.pkg.app;

import com.sweetjuice.plugin.SweetJuicePlugin;

/**
 * AppPlugin handles generic application-level metadata and events.
 */
public class AppPlugin implements SweetJuicePlugin {

    @Override
    public String getDomain() { return "app"; }

    @Override
    public void onAttach(android.content.Context context) { }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        return "{}";
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {}
    @Override public void onActivityResult(int r, int rc, android.content.Intent d) {}
    @Override public void onNewIntent(android.content.Intent intent) {}
}
