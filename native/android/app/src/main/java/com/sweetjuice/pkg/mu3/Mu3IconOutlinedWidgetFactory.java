package com.sweetjuice.pkg.mu3;

import com.sweetjuice.pkg.mu3.MaterialIconTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3IconOutlinedWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:icon-outlined";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialIconTextView icon = new MaterialIconTextView(ctx);
        icon.setIconName(prop(node, "name"));
        int size = (int)(24 * ctx.getResources().getDisplayMetrics().density);
        icon.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        return icon;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof MaterialIconTextView) {
            ((MaterialIconTextView) view).setIconName(prop(node, "name"));
        }
    }
}
