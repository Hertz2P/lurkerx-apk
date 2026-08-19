package com.sweetjuice.plugin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONObject;

/**
 * SweetJuiceWidgetFactory is the extension point for custom native UI components.
 * 
 * A plugin can register factories for custom widget types (e.g., "mu3:card").
 * When the UIManager encounters a node with a matching type, it delegates
 * view creation and updates to this factory.
 */
public interface SweetJuiceWidgetFactory {

    /**
     * Returns the unique widget type identifier.
     * 
     * @return the type string (e.g., "mu3:box").
     */
    String getType();

    /**
     * Instantiates a new native Android view for the given widget node.
     * 
     * @param ctx the Android context (usually an Activity).
     * @param node the JSON representation of the widget node.
     * @param parent the parent view group.
     * @return a new {@link View} instance.
     */
    View createView(Context ctx, JSONObject node, ViewGroup parent);

    /**
     * Updates an existing native view with new properties from the Go backend.
     * 
     * @param view the existing native view to update.
     * @param node the new JSON representation of the widget node.
     */
    void updateView(View view, JSONObject node);
}
