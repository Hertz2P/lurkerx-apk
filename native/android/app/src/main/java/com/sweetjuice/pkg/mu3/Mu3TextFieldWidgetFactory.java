package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3TextFieldWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        if (node.has(key)) return node.optString(key, "");
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:textfield";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        EditText et = new EditText(ctx);
        et.setHint(prop(node, "placeholder"));
        return et;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof EditText) {
            EditText et = (EditText) view;
            et.setHint(prop(node, "placeholder"));
        }
    }
}
