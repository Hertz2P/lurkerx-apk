package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3ImageWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        if (node.has(key)) return node.optString(key, "");
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:image";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        ImageView iv = new ImageView(ctx);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return iv;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof ImageView) {
            String src = prop(node, "src");
            if (!src.isEmpty()) {
                try {
                    ((ImageView) view).setImageURI(android.net.Uri.parse(src));
                } catch (Exception e) {
                }
            }
        }
    }
}
