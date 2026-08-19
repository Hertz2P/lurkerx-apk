package com.sweetjuice.core;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * ViewFactory is responsible for instantiating native Android views based on 
 * Sweet Juice widget types and handling density-independent pixel conversions.
 */
public class ViewFactory {
    private static final String TAG = "SweetJuice";
    private final Context context;

    public ViewFactory(Context context) {
        this.context = context;
    }

    /**
     * Converts a dp value to raw pixels using the current display metrics.
     *
     * @param dp the value in density-independent pixels
     * @return the equivalent pixel value
     */
    public int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Determines whether the given view matches the expected native type for a widget type string.
     *
     * @param view the view to inspect
     * @param type the widget type string
     * @return {@code true} if the view is of the expected type for the widget
     */
    public boolean isViewTypeOf(View view, String type) {
        if (view == null) return false;
        switch (type) {
            case "column":
            case "vstack":
            case "row":
            case "hstack":
            case "button-group":
            case "segmented-button":
                return view instanceof LinearLayout && !(view instanceof MaterialCardView);
            case "card":
                return view instanceof MaterialCardView;
            case "text":
                return view instanceof TextView && !(view instanceof MaterialButton) && !(view instanceof EditText);
            case "button":
            case "text-button":
            case "outlined-button":
            case "tonal-button":
            case "elevated-button":
            case "icon-button":
                return view instanceof MaterialButton;
            case "textfield":
                return view instanceof EditText;
            case "spacer":
                return view.getClass().equals(View.class);
            case "image":
                return view instanceof ImageView;
            case "video":
                return view instanceof VideoView;
            case "fab":
            case "extended-fab":
                return view instanceof FloatingActionButton || view instanceof ExtendedFloatingActionButton;
            default:
                return false;
        }
    }

    /**
     * Creates a new native Android view for the given widget type.
     *
     * @param type the widget type string
     * @return a new native view instance
     */
    public View createView(String type) {
        View v;
        try {
            switch (type) {
                case "column":
                case "vstack":
                    LinearLayout col = new LinearLayout(context);
                    col.setOrientation(LinearLayout.VERTICAL);
                    col.setBackgroundColor(Color.TRANSPARENT);
                    v = col;
                    break;
                case "row":
                case "hstack":
                    LinearLayout row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setBackgroundColor(Color.TRANSPARENT);
                    v = row;
                    break;
                case "card":
                    MaterialCardView card = new MaterialCardView(context);
                    card.setRadius(dpToPx(8));
                    LinearLayout cardLayout = new LinearLayout(context);
                    cardLayout.setOrientation(LinearLayout.VERTICAL);
                    cardLayout.setBackgroundColor(Color.TRANSPARENT);
                    card.addView(cardLayout);
                    v = card;
                    break;
                case "text":
                    TextView tv = new TextView(context);
                    v = tv;
                    break;
                case "button":
                    MaterialButton btn = new MaterialButton(context);
                    v = btn;
                    break;
                case "text-button":
                    MaterialButton textBtn = new MaterialButton(context);
                    textBtn.setBackgroundColor(Color.TRANSPARENT);
                    textBtn.setElevation(0);
                    textBtn.setStateListAnimator(null);
                    v = textBtn;
                    break;
                case "outlined-button":
                    MaterialButton outlinedBtn = new MaterialButton(context);
                    outlinedBtn.setStrokeWidth(1);
                    v = outlinedBtn;
                    break;
                case "tonal-button":
                    MaterialButton tonalBtn = new MaterialButton(context);
                    tonalBtn.setElevation(0);
                    v = tonalBtn;
                    break;
                case "elevated-button":
                    MaterialButton elevatedBtn = new MaterialButton(context);
                    elevatedBtn.setElevation(dpToPx(2));
                    v = elevatedBtn;
                    break;
                case "icon-button":
                    MaterialButton iconBtn = new MaterialButton(context);
                    iconBtn.setIcon(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_edit));
                    iconBtn.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
                    v = iconBtn;
                    break;
                case "fab":
                    v = new FloatingActionButton(context);
                    break;
                case "extended-fab":
                    ExtendedFloatingActionButton efab = new ExtendedFloatingActionButton(context);
                    v = efab;
                    break;
                case "segmented-button":
                    MaterialButtonToggleGroup group = new MaterialButtonToggleGroup(context);
                    group.setSingleSelection(true);
                    group.setSelectionRequired(true);
                    v = group;
                    break;
                case "button-group":
                    LinearLayout btnGroup = new LinearLayout(context);
                    btnGroup.setOrientation(LinearLayout.HORIZONTAL);
                    btnGroup.setBackgroundColor(Color.TRANSPARENT);
                    v = btnGroup;
                    break;
                case "textfield":
                    EditText et = new EditText(context);
                    v = et;
                    break;
                case "spacer":
                    v = new View(context);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case "image":
                    ImageView iv = new ImageView(context);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    v = iv;
                    break;
                case "video":
                    VideoView vv = new VideoView(context);
                    v = vv;
                    break;
                default:
                    Log.w(TAG, "UIManager: unknown type=" + type + ", using plain View");
                    v = new View(context);
                    v.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (Exception e) {
            Log.e(TAG, "UIManager: createView failed for type=" + type, e);
            v = new View(context);
            v.setBackgroundColor(Color.TRANSPARENT);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        v.setLayoutParams(lp);
        return v;
    }
}
