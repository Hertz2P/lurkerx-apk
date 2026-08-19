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

public class Mu3NavigationRailWidgetFactory implements SweetJuiceWidgetFactory {

    @Override
    public String getType() {
        return "mu3:nav-rail";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(ctx, 8), dp(ctx, 16), dp(ctx, 8), dp(ctx, 16));

        JSONArray destinations = node.optJSONArray("destinations");
        if (destinations == null) {
            JSONObject props = node.optJSONObject("props");
            if (props != null) destinations = props.optJSONArray("destinations");
        }
        if (destinations != null) {
            for (int i = 0; i < destinations.length(); i++) {
                JSONObject dest = destinations.optJSONObject(i);
                if (dest == null) continue;
                layout.addView(createDestinationItem(ctx, dest));
            }
        }
        return layout;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        createView(view.getContext(), node, (ViewGroup) view);
    }

    private LinearLayout createDestinationItem(Context ctx, JSONObject dest) {
        LinearLayout item = new LinearLayout(ctx);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
        item.setGravity(android.view.Gravity.CENTER_VERTICAL);

        MaterialIconTextView icon = new MaterialIconTextView(ctx);
        icon.setIconName(dest.optString("icon", ""));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                dp(ctx, 24), dp(ctx, 24));
        icon.setLayoutParams(iconLp);
        item.addView(icon);

        TextView label = new TextView(ctx);
        label.setText(dest.optString("label", ""));
        label.setTextSize(14);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.leftMargin = dp(ctx, 8);
        label.setLayoutParams(labelLp);
        item.addView(label);

        return item;
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
