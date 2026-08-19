package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3SpacerWidgetFactory implements SweetJuiceWidgetFactory {

    @Override
    public String getType() {
        return "mu3:spacer";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        return new View(ctx);
    }

    @Override
    public void updateView(View view, JSONObject node) {
    }
}
