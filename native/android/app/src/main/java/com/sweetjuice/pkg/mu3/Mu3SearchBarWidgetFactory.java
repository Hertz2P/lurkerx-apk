package com.sweetjuice.pkg.mu3;

import com.sweetjuice.pkg.mu3.MaterialIconTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Mu3SearchBarWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:search-bar";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dp(ctx, 12), dp(ctx, 8), dp(ctx, 12), dp(ctx, 8));
        layout.setElevation(dp(ctx, 2));

        MaterialIconTextView iconBtn = new MaterialIconTextView(ctx);
        iconBtn.setIconName("search");
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                dp(ctx, 40), dp(ctx, 40));
        iconLp.gravity = android.view.Gravity.CENTER_VERTICAL;
        iconBtn.setLayoutParams(iconLp);
        layout.addView(iconBtn);

        EditText input = new EditText(ctx);
        input.setHint(prop(node, "hint"));
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputLp.leftMargin = dp(ctx, 8);
        inputLp.gravity = android.view.Gravity.CENTER_VERTICAL;
        input.setLayoutParams(inputLp);
        layout.addView(input);

        return layout;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof LinearLayout)) return;
        LinearLayout layout = (LinearLayout) view;
        if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof EditText) {
            ((EditText) layout.getChildAt(1)).setHint(prop(node, "hint"));
        }
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
