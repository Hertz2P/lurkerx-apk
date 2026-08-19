package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3ExtendedFabWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:extended-fab";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        ExtendedFloatingActionButton fab = new ExtendedFloatingActionButton(ctx);
        fab.setText(prop(node, "text"));
        return fab;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof ExtendedFloatingActionButton) {
            ((ExtendedFloatingActionButton) view).setText(prop(node, "text"));
        }
    }
}
