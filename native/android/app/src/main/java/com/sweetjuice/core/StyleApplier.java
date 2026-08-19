package com.sweetjuice.core;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONObject;

/**
 * StyleApplier maps Sweet Juice style properties (JSON) onto native Android view attributes.
 * It handles layout parameters, colors, typography, and spacing.
 */
class StyleApplier {
    private static final String TAG = "SweetJuice";
    private final ViewFactory viewFactory;

    StyleApplier(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    /**
     * Applies styles from the given JSON object to the view.
     *
     * @param view  the target view
     * @param type  the widget type string
     * @param style the style JSON object, may be {@code null}
     */
    void applyStyles(View view, String type, JSONObject style) {
        try {
            if (style == null) return;

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (lp == null) {
                lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }

            if (style.has("flex")) {
                try {
                    lp.weight = (float) style.optDouble("flex");
                    if (lp.weight > 0) {
                        ViewGroup parent = (ViewGroup) view.getParent();
                        boolean horizontal = parent instanceof LinearLayout &&
                                ((LinearLayout) parent).getOrientation() == LinearLayout.HORIZONTAL;
                        if (horizontal) {
                            lp.width = 0;
                            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        } else {
                            lp.height = 0;
                            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: flex style failed", e);
                }
            }

            if (style.has("width")) {
                try {
                    lp.width = viewFactory.dpToPx((float) style.optDouble("width"));
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: width style failed", e);
                }
            }
            if (style.has("height")) {
                try {
                    lp.height = viewFactory.dpToPx((float) style.optDouble("height"));
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: height style failed", e);
                }
            }
            view.setLayoutParams(lp);

            try {
                int p = viewFactory.dpToPx((float) style.optDouble("padding", 0));
                int pv = viewFactory.dpToPx((float) style.optDouble("paddingVertical", 0));
                int ph = viewFactory.dpToPx((float) style.optDouble("paddingHorizontal", 0));
                int pt = pv != 0 ? pv : p;
                int pb = pv != 0 ? pv : p;
                int pl = ph != 0 ? ph : p;
                int pr = ph != 0 ? ph : p;
                if (view instanceof MaterialCardView) {
                    ((MaterialCardView) view).setContentPadding(pl, pt, pr, pb);
                } else {
                    view.setPadding(pl, pt, pr, pb);
                }
            } catch (Exception e) {
                Log.e(TAG, "StyleApplier: padding style failed", e);
            }

            if (style.has("backgroundColor")) {
                try {
                    String colorStr = style.optString("backgroundColor");
                    int color = Color.parseColor(colorStr);
                    if (view instanceof MaterialCardView) {
                        ((MaterialCardView) view).setCardBackgroundColor(color);
                    } else if (view instanceof MaterialButton) {
                        ((MaterialButton) view).setBackgroundTintList(
                                android.content.res.ColorStateList.valueOf(color)
                        );
                    } else {
                        view.setBackgroundColor(color);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: background color failed: " + style.optString("backgroundColor"), e);
                }
            }

            if (style.has("cornerRadius")) {
                try {
                    float radius = viewFactory.dpToPx((float) style.optDouble("cornerRadius"));
                    if (view instanceof MaterialCardView) {
                        ((MaterialCardView) view).setRadius(radius);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: cornerRadius failed", e);
                }
            }

            if (view instanceof LinearLayout) {
                try {
                    LinearLayout ll = (LinearLayout) view;
                    String align = style.optString("alignItems");
                    String justify = style.optString("justifyContent");
                    int gravity = 0;
                    if ("center".equals(align)) gravity |= Gravity.CENTER_HORIZONTAL;
                    if ("center".equals(justify)) gravity |= Gravity.CENTER_VERTICAL;
                    ll.setGravity(gravity);
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: gravity failed", e);
                }
            }

            if (view instanceof TextView) {
                try {
                    TextView tv = (TextView) view;
                    if (style.has("fontSize")) {
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) style.optDouble("fontSize"));
                    }
                    if (style.has("color")) {
                        tv.setTextColor(Color.parseColor(style.optString("color")));
                    }
                    if (style.has("weight")) {
                        String weight = style.optString("weight");
                        if ("bold".equals(weight)) {
                            tv.setTypeface(null, Typeface.BOLD);
                        } else {
                            tv.setTypeface(null, Typeface.NORMAL);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "StyleApplier: text style failed", e);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "StyleApplier: applyStyles fatal", e);
        }
    }
}
