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

public class Mu3TabsWidgetFactory implements SweetJuiceWidgetFactory {

    private JSONArray getPrimary(JSONObject node) {
        JSONArray primary = node.optJSONArray("primary");
        if (primary != null) return primary;
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optJSONArray("primary");
        return null;
    }

    private String getSelected(JSONObject node) {
        String selected = node.optString("selected", "");
        if (!selected.isEmpty()) return selected;
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString("selected", "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:tabs";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(ctx);
        group.setSingleSelection(true);
        group.setSelectionRequired(true);

        JSONArray primary = getPrimary(node);
        if (primary != null) {
            for (int i = 0; i < primary.length(); i++) {
                String label = primary.optString(i, "");
                if (label.isEmpty()) continue;
                MaterialButton btn = new MaterialButton(ctx);
                btn.setText(label);
                group.addView(btn);
            }
        }

        return group;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof MaterialButtonToggleGroup)) return;
        MaterialButtonToggleGroup group = (MaterialButtonToggleGroup) view;
        String selected = getSelected(node);
        if (!selected.isEmpty()) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) instanceof com.google.android.material.button.MaterialButton) {
                    com.google.android.material.button.MaterialButton btn =
                            (com.google.android.material.button.MaterialButton) group.getChildAt(i);
                    if (selected.equals(btn.getText())) {
                        group.check(btn.getId());
                    }
                }
            }
        }
    }
}
