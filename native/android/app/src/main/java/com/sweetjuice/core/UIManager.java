package com.sweetjuice.core;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;

/**
 * Orchestrates rendering a Sweet Juice JSON UI tree onto native Android views.
 *
 * <p>Render pipeline:</p>
 * <ol>
 *   <li>Parse incoming JSON tree</li>
 *   <li>Reconcile existing views with new nodes</li>
 *   <li>Delegate widget-specific rendering to registered {@link SweetJuiceWidgetFactory} instances</li>
 *   <li>Apply styles, bind events, and update text/media</li>
 * </ol>
 */
public class UIManager {
    private static final String TAG = "SweetJuice";
    private final Context context;
    private final ViewGroup rootContainer;
    private boolean renderFailed;
    private final WidgetRegistry widgetRegistry;
    private final ViewFactory viewFactory;
    private final StyleApplier styleApplier;
    private final EventBinder eventBinder;
    private final DialogRenderer dialogRenderer;

    public UIManager(Context context, ViewGroup rootContainer) {
        this.context = context;
        this.rootContainer = rootContainer;
        this.renderFailed = false;
        this.viewFactory = new ViewFactory(context);
        this.widgetRegistry = new WidgetRegistry();
        this.styleApplier = new StyleApplier(viewFactory);
        this.eventBinder = new EventBinder();
        this.dialogRenderer = new DialogRenderer(context);
    }

    /**
     * Registers a widget factory for a custom plugin widget.
     * 
     * @param factory the widget factory to register.
     */
    public void registerWidgetFactory(SweetJuiceWidgetFactory factory) {
        widgetRegistry.registerWidgetFactory(factory);
    }

    /**
     * Registers a widget factory under a specific type key.
     * 
     * @param type the unique type identifier for the widget.
     * @param factory the widget factory to register.
     */
    public void registerWidgetFactory(String type, SweetJuiceWidgetFactory factory) {
        widgetRegistry.registerWidgetFactory(type, factory);
    }

    /**
     * Main entry point for rendering a UI tree from a JSON string.
     * Reconciles the JSON nodes with existing native views.
     * 
     * @param jsonTree the JSON representation of the UI tree.
     */
    public void render(String jsonTree) {
        try {
            if (jsonTree == null || jsonTree.isEmpty()) {
                Log.w(TAG, "UIManager: null or empty JSON");
                return;
            }

            Log.d(TAG, "UIManager: render payload=" + jsonTree.length() + " chars");

            JSONObject rootNode;
            try {
                rootNode = new JSONObject(jsonTree);
            } catch (Exception e) {
                Log.e(TAG, "UIManager: JSON parse failed", e);
                return;
            }

            String type = rootNode.optString("type");
            if (type.isEmpty()) {
                Log.w(TAG, "UIManager: root node missing type");
                return;
            }

            renderFailed = false;

            if ("root".equals(type)) {
                JSONObject childNode = rootNode.optJSONObject("child");
                if (childNode == null) {
                    Log.w(TAG, "UIManager: root node missing child");
                    return;
                }
                View existingRoot = rootContainer.getChildAt(0);
                View newView = updateOrCreateView(existingRoot, childNode);

                if (existingRoot != newView) {
                    rootContainer.removeAllViews();
                    rootContainer.addView(newView);
                }

                applyRootBackground(rootNode);
                if (!renderFailed) {
                    Log.d(TAG, "UIManager: render OK");
                }
                return;
            }

            View existingRoot = rootContainer.getChildAt(0);
            View newView = updateOrCreateView(existingRoot, rootNode);

            if (existingRoot != newView) {
                Log.d(TAG, "UIManager: replacing root view");
                rootContainer.removeAllViews();
                rootContainer.addView(newView);
            }

            if (!renderFailed) {
                Log.d(TAG, "UIManager: render OK");
            }

        } catch (Exception e) {
            Log.e(TAG, "UIManager: render fatal", e);
        }
    }

    private void applyRootBackground(JSONObject rootNode) {
        try {
            String bg = rootNode.optString("backgroundColor", "").trim();
            if (bg.isEmpty()) {
                JSONObject style = rootNode.optJSONObject("style");
                if (style != null) {
                    bg = style.optString("backgroundColor", "").trim();
                }
            }
            if (!bg.isEmpty()) {
                int color = android.graphics.Color.parseColor(bg);
                rootContainer.setBackgroundColor(color);
            }
        } catch (Exception e) {
            Log.w(TAG, "UIManager: applyRootBackground failed", e);
        }
    }

    private View updateOrCreateView(View existingView, JSONObject node) {
        try {
            String id = node.optString("id", "");
            String type = node.optString("type", "");

            if (type.isEmpty()) {
                Log.w(TAG, "UIManager: node missing type, id=" + id);
                return existingView != null ? existingView : viewFactory.createView("text");
            }

            if ("ui:dialog".equals(type)) {
                dialogRenderer.showNativeDialog(node);
                return existingView != null ? existingView : viewFactory.createView("text");
            }

            SweetJuiceWidgetFactory widgetFactory = widgetRegistry.get(type);
            if (widgetFactory != null) {
                View widgetView = existingView;
                if (widgetView == null || !(widgetView.getTag() != null && widgetView.getTag().equals(id))) {
                    widgetView = widgetFactory.createView(context, node, rootContainer);
                    widgetView.setTag(id);
                }
                updateView(widgetView, node);
                return widgetView;
            }

            View view = existingView;
            boolean needsNewView = false;

            if (view == null) {
                needsNewView = true;
            } else if (!viewFactory.isViewTypeOf(view, type)) {
                needsNewView = true;
            } else if (!id.equals(view.getTag())) {
                needsNewView = true;
            }

            if (needsNewView) {
                if (existingView != null && viewFactory.isViewTypeOf(existingView, type)) {
                    view = existingView;
                    view.setTag(id);
                } else {
                    view = viewFactory.createView(type);
                    view.setTag(id);
                }
            }

            updateView(view, node);
            return view;

        } catch (Exception e) {
            Log.e(TAG, "UIManager: updateOrCreateView failed", e);
            renderFailed = true;
            return existingView != null ? existingView : viewFactory.createView("text");
        }
    }

    /**
     * Reconciles a list of child nodes within a native container.
     * Used by standard layouts (Column, Row) and complex plugin widgets.
     * 
     * @param container the native view group containing the children.
     * @param children the JSON array of child nodes.
     */
    public void updateChildren(ViewGroup container, JSONArray children) {
        if (children == null) {
            container.removeAllViews();
            return;
        }

        int nodeCount = children.length();
        int viewCount = container.getChildCount();

        for (int i = 0; i < nodeCount; i++) {
            try {
                JSONObject childNode = children.getJSONObject(i);
                View existingChild = (i < viewCount) ? container.getChildAt(i) : null;
                View updatedChild = updateOrCreateView(existingChild, childNode);

                if (existingChild == null) {
                    container.addView(updatedChild);
                } else if (existingChild != updatedChild) {
                    container.removeViewAt(i);
                    container.addView(updatedChild, i);
                }
            } catch (Exception e) {
                Log.e(TAG, "UIManager: child update failed at index " + i, e);
            }
        }
        if (viewCount > nodeCount) {
            container.removeViews(nodeCount, viewCount - nodeCount);
        }
    }

    private void updateView(View view, JSONObject node) {
        try {
            String type = node.optString("type");
            JSONObject style = node.optJSONObject("style");
            JSONArray events = node.optJSONArray("events");
            java.util.Set<String> eventSet = new java.util.HashSet<>();
            if (events != null) {
                for (int i = 0; i < events.length(); i++) {
                    eventSet.add(events.getString(i));
                }
            }

            SweetJuiceWidgetFactory widgetFactory = widgetRegistry.get(type);
            if (widgetFactory != null) {
                widgetFactory.updateView(view, node);
                eventBinder.setupEvents(view, node.optString("id", ""), eventSet);
                styleApplier.applyStyles(view, type, style);
                return;
            }

            if (view instanceof ViewGroup) {
                ViewGroup container;
                if (view instanceof com.google.android.material.card.MaterialCardView) {
                    View child = ((com.google.android.material.card.MaterialCardView) view).getChildAt(0);
                    if (child instanceof ViewGroup) {
                        container = (ViewGroup) child;
                    } else {
                        return;
                    }
                } else {
                    container = (ViewGroup) view;
                }

                updateChildren(container, node.optJSONArray("children"));
            }

            if (view instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) view;
                btn.setText(node.optString("text", ""));
            } else if (view instanceof com.google.android.material.floatingactionbutton.FloatingActionButton) {
                com.google.android.material.floatingactionbutton.FloatingActionButton fab = (com.google.android.material.floatingactionbutton.FloatingActionButton) view;
                if (view instanceof com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton) {
                    ((com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton) view).setText(node.optString("text", ""));
                }
            } else if (view instanceof android.widget.TextView && !(view instanceof android.widget.EditText)) {
                android.widget.TextView tv = (android.widget.TextView) view;
                tv.setText(node.optString("value", ""));
            } else if (view instanceof android.widget.EditText) {
                android.widget.EditText et = (android.widget.EditText) view;
                String val = node.optString("value", "");
                if (!val.equals(et.getText().toString())) {
                    et.setText(val);
                }
                et.setHint(node.optString("placeholder", ""));
            } else if (view instanceof android.widget.ImageView) {
                android.widget.ImageView iv = (android.widget.ImageView) view;
                String src = node.optString("src", "");
                if (src != null && !src.isEmpty()) {
                    try {
                        android.net.Uri uri = android.net.Uri.parse(src);
                        iv.setImageURI(uri);
                    } catch (Exception e) {
                        Log.w(TAG, "UIManager: image load failed for src=" + src, e);
                        iv.setImageResource(android.R.drawable.ic_dialog_alert);
                    }
                }
                String scaleType = node.optString("scaleType", "");
                if ("centerCrop".equalsIgnoreCase(scaleType)) {
                    iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                } else if ("centerInside".equalsIgnoreCase(scaleType)) {
                    iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
                } else {
                    iv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                }
            } else if (view instanceof android.widget.VideoView) {
                android.widget.VideoView vv = (android.widget.VideoView) view;
                String src = node.optString("src", "");
                if (src != null && !src.isEmpty()) {
                    try {
                        android.net.Uri uri = android.net.Uri.parse(src);
                        vv.setVideoURI(uri);
                        if (node.optBoolean("autoplay", false)) {
                            vv.start();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "UIManager: video load failed for src=" + src, e);
                    }
                }
                vv.setOnPreparedListener(mp -> {
                    if (node.optBoolean("muted", false)) {
                        mp.setVolume(0f, 0f);
                    }
                });
            } else if ("spacer".equals(type)) {
                try {
                    float rawW = (float) node.optDouble("width", -1);
                    float rawH = (float) node.optDouble("height", -1);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                    if (lp == null) {
                        lp = new LinearLayout.LayoutParams(0, 0);
                    }
                    if (rawH >= 0) {
                        lp.height = viewFactory.dpToPx(rawH);
                        lp.weight = 0;
                    } else {
                        lp.height = 0;
                        lp.weight = 1.0f;
                    }
                    if (rawW >= 0) {
                        lp.width = viewFactory.dpToPx(rawW);
                    } else {
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    view.setLayoutParams(lp);
                } catch (Exception e) {
                    Log.e(TAG, "UIManager: spacer layout failed", e);
                }
            }

            eventBinder.setupEvents(view, node.optString("id", ""), eventSet);
            styleApplier.applyStyles(view, type, style);

        } catch (Exception e) {
            Log.e(TAG, "UIManager: updateView fatal", e);
            renderFailed = true;
        }
    }
}
