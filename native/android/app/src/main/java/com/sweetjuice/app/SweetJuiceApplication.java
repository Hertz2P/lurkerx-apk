package com.ablets.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.sweetjuice.core.SweetJuiceApp;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import juiceapp.Juiceapp;

/**
 * SweetJuiceApplication maintains the global application state, plugin registry,
 * and handles communication between the native Android platform and the Go backend.
 */
public class SweetJuiceApplication extends android.app.Application implements SweetJuiceApp {
    private final Map<String, SweetJuicePlugin> mPlugins = new HashMap<>();
    private SweetJuiceActivity mActiveActivity;

    public void setActiveActivity(SweetJuiceActivity activity) {
        mActiveActivity = activity;
    }

    @Override
    public SweetJuiceActivity getActiveActivity() {
        return mActiveActivity;
    }

    @Override
    public Class<?> getMainActivityClass() {
        return SweetJuiceActivity.class;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Juiceapp.setNativeCallHandler(new juiceapp.NativeCallHandler() {
            @Override
            public String onNativeCall(String method, String args) {
                Log.d("SweetJuice", "NativeCall: " + method);
                if ("ui:render".equals(method)) {
                    if (mActiveActivity != null) {
                        mActiveActivity.renderUI(args);
                        return "{\"status\":\"ok\"}";
                    }
                    Log.w("SweetJuice", "ui:render dropped: No active activity");
                    return "{\"error\":\"No active activity\"}";
                }

                if ("plugin:register".equals(method)) {
                    return handlePluginRegister(args);
                }

                if (method.contains(":")) {
                    String[] parts = method.split(":", 2);
                    String domain = parts[0];
                    String action = parts[1];
                    SweetJuicePlugin plugin = mPlugins.get(domain);
                    if (plugin != null) {
                        return plugin.handleAction(action, args);
                    }
                }
                return "{\"error\":\"Plugin domain not found\"}";
            }
        });
    }

    String handlePluginRegister(String args) {
        try {
            JSONArray plugins = new JSONArray(args);
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject config = plugins.optJSONObject(i);
                if (config == null) continue;
                String domain = config.optString("domain", "");
                String javaPkg = config.optString("javaPkg", "");
                String className = config.optString("class", "");
                if (domain.isEmpty() || javaPkg.isEmpty() || className.isEmpty()) continue;

                if (mPlugins.containsKey(domain)) {
                    Log.d("SweetJuice", "Plugin already registered: " + domain);
                    continue;
                }

                try {
                    String fullClassName = javaPkg + "." + className;
                    Class<?> clazz = Class.forName(fullClassName);
                    SweetJuicePlugin plugin = (SweetJuicePlugin) clazz.getDeclaredConstructor().newInstance();
                    Context ctx = mActiveActivity != null ? mActiveActivity : this;
                    plugin.onAttach(ctx);
                    mPlugins.put(domain, plugin);
                    Log.d("SweetJuice", "Dynamically registered plugin: " + domain + " -> " + fullClassName);

                    if (mActiveActivity != null) {
                        mActiveActivity.onPluginRegistered(plugin);
                    }
                } catch (Exception e) {
                    Log.e("SweetJuice", "Failed to register plugin: " + domain, e);
                }
            }
            JSONObject result = new JSONObject();
            result.put("status", "ok");
            result.put("count", mPlugins.size());
            return result.toString();
        } catch (JSONException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private void registerPlugin(SweetJuicePlugin plugin) {
        plugin.onAttach(this);
        mPlugins.put(plugin.getDomain(), plugin);
    }

    public Map<String, SweetJuicePlugin> getPlugins() {
        return mPlugins;
    }
}
