package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3OutlinedButtonWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:outlined-button";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialButton btn = new MaterialButton(ctx);
        btn.setText(prop(node, "text"));
        return btn;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof MaterialButton) {
            ((MaterialButton) view).setText(prop(node, "text"));
        }
    }
}
