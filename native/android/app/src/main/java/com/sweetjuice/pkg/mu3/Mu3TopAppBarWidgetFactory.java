package com.sweetjuice.pkg.mu3;

import com.sweetjuice.pkg.mu3.MaterialIconTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3TopAppBarWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:top-app-bar";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        layout.setElevation(dp(ctx, 4));

        String navIcon = prop(node, "navigationIcon");
        if (!navIcon.isEmpty()) {
            MaterialIconTextView iconBtn = new MaterialIconTextView(ctx);
            iconBtn.setIconName(navIcon);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(ctx, 40), dp(ctx, 40));
            lp.gravity = android.view.Gravity.CENTER_VERTICAL;
            iconBtn.setLayoutParams(lp);
            layout.addView(iconBtn);
        }

        TextView title = new TextView(ctx);
        title.setText(prop(node, "title"));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLp.gravity = android.view.Gravity.CENTER_VERTICAL;
        titleLp.leftMargin = dp(ctx, 16);
        title.setLayoutParams(titleLp);
        layout.addView(title);

        return layout;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof LinearLayout)) return;
        LinearLayout layout = (LinearLayout) view;
        String title = prop(node, "title");
        if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof TextView) {
            ((TextView) layout.getChildAt(1)).setText(title);
        }
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
