package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3TextWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        if (node.has(key)) return node.optString(key, "");
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:text";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        TextView tv = new TextView(ctx);
        tv.setText(prop(node, "value"));
        return tv;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof TextView) {
            ((TextView) view).setText(prop(node, "value"));
        }
    }
}
