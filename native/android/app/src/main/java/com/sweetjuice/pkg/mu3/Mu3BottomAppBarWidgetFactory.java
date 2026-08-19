package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3BottomAppBarWidgetFactory implements SweetJuiceWidgetFactory {

    @Override
    public String getType() {
        return "mu3:bottom-app-bar";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        layout.setBackgroundColor(android.graphics.Color.WHITE);
        layout.setElevation(dp(ctx, 8));
        return layout;
    }

    @Override
    public void updateView(View view, JSONObject node) {
    }

    private int dp(Context ctx, float dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
}
