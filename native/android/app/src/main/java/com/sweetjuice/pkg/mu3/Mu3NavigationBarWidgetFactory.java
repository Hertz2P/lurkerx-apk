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

public class Mu3NavigationBarWidgetFactory implements SweetJuiceWidgetFactory {

    @Override
    public String getType() {
        return "mu3:nav-bar";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setElevation(dp(ctx, 8));

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
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(ctx, 4), dp(ctx, 8), dp(ctx, 4), dp(ctx, 8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        item.setLayoutParams(lp);
        item.setGravity(android.view.Gravity.CENTER);

        MaterialIconTextView icon = new MaterialIconTextView(ctx);
        icon.setIconName(dest.optString("icon", ""));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                dp(ctx, 24), dp(ctx, 24));
        icon.setLayoutParams(iconLp);
        item.addView(icon);

        TextView label = new TextView(ctx);
        label.setText(dest.optString("label", ""));
        label.setTextSize(12);
        label.setGravity(android.view.Gravity.CENTER);
        item.addView(label);

        return item;
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
