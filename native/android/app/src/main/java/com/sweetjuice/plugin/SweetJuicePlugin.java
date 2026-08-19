package com.sweetjuice.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.sweetjuice.core.UIManager;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;

/**
 * SweetJuicePlugin is the base interface for all native Android extensions.
 * Plugins handle actions from Go and can provide custom widget factories.
 */
public interface SweetJuicePlugin {
    
    /**
     * Returns the unique domain/namespace for this plugin.
     */
    String getDomain();
    
    /**
     * Called when the plugin is registered (with Application context) 
     * or when an activity becomes active (with Activity context).
     */
    void onAttach(Context context);
    
    /**
     * Primary entry point for actions called from Go.
     */
    String handleAction(String action, String jsonArgsPayload);
    
    /**
     * Optional: Return widget factories provided by this plugin.
     * The framework will register them with UIManager during startup.
     */
    default SweetJuiceWidgetFactory[] getWidgetFactories() {
        return new SweetJuiceWidgetFactory[0];
    }

    /**
     * Optional: Called after UIManager is ready, allowing plugins to register
     * dynamic widget factories or inject UI behaviors at runtime.
     */
    default void onWidgetFactoriesRegistered(UIManager manager) {}

    /**
     * Optional: Called when an activity returns a result.
     */
    default void onActivityResult(int requestCode, int resultCode, Intent data) {}
    
    /**
     * Optional: Called when a permission request returns a result.
     */
    default void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}

    /**
     * Optional: Called when the activity receives a new intent (e.g., Deep Linking).
     */
    default void onNewIntent(Intent intent) {}
}
