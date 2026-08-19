package com.ablets.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.color.DynamicColors;
import com.sweetjuice.core.UIManager;
import com.sweetjuice.plugin.SweetJuicePlugin;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import com.sweetjuice.pkg.broadcast.BroadcastPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import juiceapp.Juiceapp;

/**
 * SweetJuiceActivity is the primary entry point for the user interface.
 * It initializes the native UI layout, the Go bridge, and manages the 
 * lifecycle of the application's visual state.
 */
public class SweetJuiceActivity extends AppCompatActivity {

    private UIManager mUIManager;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);

        ((SweetJuiceApplication) getApplication()).setActiveActivity(this);

        SweetJuiceApplication app = (SweetJuiceApplication) getApplication();
        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            plugin.onAttach(this);
        }

        registerGoPlugins(app);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.TRANSPARENT);

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.addView(rootLayout);
        setContentView(scrollView);

        mUIManager = new UIManager(this, rootLayout);

        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            for (SweetJuiceWidgetFactory factory : plugin.getWidgetFactories()) {
                mUIManager.registerWidgetFactory(factory);
            }
            plugin.onWidgetFactoriesRegistered(mUIManager);
        }

        showFallback("Starting Sweet Juice...");

        try {
            Juiceapp.startApplication();
        } catch (Throwable t) {
            Log.e("SweetJuice", "Go bridge failed", t);
            showFallback("Bridge error: " + t.getMessage());
            return;
        }

        registerGoPlugins(app);

        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            for (SweetJuiceWidgetFactory factory : plugin.getWidgetFactories()) {
                mUIManager.registerWidgetFactory(factory);
            }
            plugin.onWidgetFactoriesRegistered(mUIManager);
        }

        handleIntent(getIntent());

        Juiceapp.reRender();
    }

    private void registerGoPlugins(SweetJuiceApplication app) {
        try {
            String pluginsJson = Juiceapp.getRegisteredPlugins();
            JSONArray plugins = new JSONArray(pluginsJson);
            Log.d("SweetJuice", "Registering " + plugins.length() + " Go plugins");
            app.handlePluginRegister(pluginsJson);
        } catch (Exception e) {
            Log.w("SweetJuice", "Go plugin registration failed", e);
        }
    }

    void showFallback(String msg) {
        runOnUiThread(() -> {
            rootLayout.removeAllViews();
            TextView tv = new TextView(SweetJuiceActivity.this);
            tv.setText(msg);
            tv.setTextSize(18);
            tv.setTextColor(Color.DKGRAY);
            tv.setGravity(Gravity.CENTER);
            rootLayout.addView(tv);
        });
    }

    public void onPluginRegistered(SweetJuicePlugin plugin) {
        runOnUiThread(() -> {
            if (mUIManager != null) {
                for (SweetJuiceWidgetFactory factory : plugin.getWidgetFactories()) {
                    mUIManager.registerWidgetFactory(factory);
                }
                plugin.onWidgetFactoriesRegistered(mUIManager);
            }
        });
    }

    /**
     * Triggers a UI re-render from the Go backend.
     * 
     * @param json the JSON representation of the UI tree.
     */
    public void renderUI(final String json) {
        Log.d("SweetJuice", "Activity.renderUI called, length=" + (json != null ? json.length() : 0));
        runOnUiThread(() -> {
            try {
                mUIManager.render(json);
            } catch (Exception e) {
                Log.e("SweetJuice", "UIManager.render crashed", e);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        SweetJuiceApplication app = (SweetJuiceApplication) getApplication();
        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            plugin.onNewIntent(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((SweetJuiceApplication) getApplication()).setActiveActivity(this);
        try {
            Juiceapp.handleNativeAction("app:resumed", "[]");
            Juiceapp.pollNativeEvent();
        } catch (Exception e) {
            Log.w("SweetJuice", "onResume event dispatch failed", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SweetJuiceApplication app = (SweetJuiceApplication) getApplication();
        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            plugin.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SweetJuiceApplication app = (SweetJuiceApplication) getApplication();
        for (SweetJuicePlugin plugin : app.getPlugins().values()) {
            plugin.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        if (((SweetJuiceApplication) getApplication()).getActiveActivity() == this) {
            ((SweetJuiceApplication) getApplication()).setActiveActivity(null);
        }
        super.onDestroy();
    }
}