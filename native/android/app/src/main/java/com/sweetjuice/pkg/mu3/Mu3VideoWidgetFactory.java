package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;

public class Mu3VideoWidgetFactory implements SweetJuiceWidgetFactory {

    private String prop(JSONObject node, String key) {
        if (node.has(key)) return node.optString(key, "");
        JSONObject props = node.optJSONObject("props");
        if (props != null) return props.optString(key, "");
        return "";
    }

    @Override
    public String getType() {
        return "mu3:video";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        VideoView vv = new VideoView(ctx);
        return vv;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (view instanceof VideoView) {
            String src = prop(node, "src");
            if (!src.isEmpty()) {
                try {
                    ((VideoView) view).setVideoURI(android.net.Uri.parse(src));
                } catch (Exception e) {
                }
            }
        }
    }
}
