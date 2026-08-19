package com.sweetjuice.pkg.mu3;

import com.sweetjuice.pkg.mu3.MaterialIconTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Mu3ToolbarWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    private JSONArray getItems(JSONObject node) {
        JSONArray items = node.optJSONArray("items");
        if (items != null) return items;
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optJSONArray("items");
        return null;
    }

    @Override
    public String getType() {
        return "mu3:toolbar";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        String orientation = prop(node, "orientation");
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation("vertical".equalsIgnoreCase(orientation)
                ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        layout.setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8));

        JSONArray items = getItems(node);
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item == null) continue;

                LinearLayout itemLayout = new LinearLayout(ctx);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                MaterialIconTextView icon = new MaterialIconTextView(ctx);
                icon.setIconName(item.optString("icon", ""));
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                        dp(ctx, 24), dp(ctx, 24));
                iconLp.rightMargin = dp(ctx, 4);
                icon.setLayoutParams(iconLp);
                itemLayout.addView(icon);

                TextView label = new TextView(ctx);
                label.setText(item.optString("label", ""));
                label.setTextSize(14);
                itemLayout.addView(label);

                layout.addView(itemLayout);
            }
        }
        return layout;
    }

    @Override
    public void updateView(View view, JSONObject node) {
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
