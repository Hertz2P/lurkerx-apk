package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import com.sweetjuice.core.UIManager;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Mu3CardWidgetFactory implements SweetJuiceWidgetFactory {
    private UIManager mUIManager;

    public void setUIManager(UIManager uiManager) {
        this.mUIManager = uiManager;
    }

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:card";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialCardView card = new MaterialCardView(ctx);
        card.setRadius(8 * ctx.getResources().getDisplayMetrics().density);
        card.setCardElevation(2 * ctx.getResources().getDisplayMetrics().density);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(ctx);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int)(16 * ctx.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(ctx);
        titleView.setText(prop(node, "title"));
        titleView.setTextSize(20);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView subtitleView = new TextView(ctx);
        subtitleView.setText(prop(node, "subtitle"));
        subtitleView.setTextSize(14);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = (int)(8 * ctx.getResources().getDisplayMetrics().density);
        subtitleView.setLayoutParams(lp);

        layout.addView(titleView);
        layout.addView(subtitleView);
        card.addView(layout);

        return card;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof MaterialCardView)) return;
        MaterialCardView card = (MaterialCardView) view;
        android.view.View child = card.getChildAt(0);
        if (!(child instanceof android.widget.LinearLayout)) return;
        android.widget.LinearLayout layout = (android.widget.LinearLayout) child;

        if (layout.getChildCount() > 0) {
            TextView titleView = (TextView) layout.getChildAt(0);
            titleView.setText(prop(node, "title"));
        }
        if (layout.getChildCount() > 1) {
            TextView subtitleView = (TextView) layout.getChildAt(1);
            subtitleView.setText(prop(node, "subtitle"));
        }
    }
}
