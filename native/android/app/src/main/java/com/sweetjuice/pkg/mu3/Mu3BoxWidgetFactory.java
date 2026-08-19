package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.card.MaterialCardView;
import com.sweetjuice.core.UIManager;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3BoxWidgetFactory implements SweetJuiceWidgetFactory {
    private UIManager mUIManager;

    public void setUIManager(UIManager uiManager) {
        this.mUIManager = uiManager;
    }

    @Override
    public String getType() {
        return "mu3:box";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        MaterialCardView box = new MaterialCardView(ctx);
        box.setRadius(12 * ctx.getResources().getDisplayMetrics().density);
        box.setCardElevation(2 * ctx.getResources().getDisplayMetrics().density);
        box.setUseCompatPadding(true);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(ctx);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int)(12 * ctx.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.setTag("box_content");

        box.addView(layout);
        updateView(box, node);
        return box;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof MaterialCardView)) return;
        MaterialCardView box = (MaterialCardView) view;
        View content = box.findViewWithTag("box_content");

        if (content instanceof ViewGroup && mUIManager != null) {
            mUIManager.updateChildren((ViewGroup) content, node.optJSONArray("children"));
        }
    }
}
