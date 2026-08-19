package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Mu3SegmentedButtonWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    private JSONArray getOptions(JSONObject node) {
        JSONArray options = node.optJSONArray("options");
        if (options != null) return options;
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optJSONArray("options");
        return null;
    }

    @Override
    public String getType() {
        return "mu3:segmented-button";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(ctx);
        group.setSingleSelection(true);
        group.setSelectionRequired(true);

        JSONArray options = getOptions(node);
        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                String option = options.optString(i);
                if (option.isEmpty()) continue;
                MaterialButton btn = new MaterialButton(ctx);
                btn.setText(option);
                group.addView(btn);
            }
        }
        return group;
    }

    @Override
    public void updateView(View view, JSONObject node) {
    }
}
