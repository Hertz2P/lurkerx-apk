package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Mu3ButtonGroupWidgetFactory implements SweetJuiceWidgetFactory {

    private JSONArray getChildren(JSONObject node) {
        JSONArray children = node.optJSONArray("children");
        if (children != null) return children;
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optJSONArray("children");
        return null;
    }

    @Override
    public String getType() {
        return "mu3:button-group";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout group = new LinearLayout(ctx);
        group.setOrientation(LinearLayout.HORIZONTAL);
        return group;
    }

    @Override
    public void updateView(View view, JSONObject node) {
    }
}
