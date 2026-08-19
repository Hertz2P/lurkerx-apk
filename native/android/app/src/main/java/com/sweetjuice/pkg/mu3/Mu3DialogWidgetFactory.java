package com.sweetjuice.pkg.mu3;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.sweetjuice.core.SweetJuiceApp;
import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import org.json.JSONObject;
import juiceapp.Juiceapp;

public class Mu3DialogWidgetFactory implements SweetJuiceWidgetFactory {

    @Override
    public String getType() {
        return "mu3:dialog";
    }

    @Override
    public View createView(Context ctx, JSONObject node, ViewGroup parent) {
        AppCompatActivity activity = getActiveActivity(ctx);
        if (activity == null) {
            Log.w("Mu3Dialog", "No active activity for dialog");
            return new View(ctx);
        }

        String title = node.optString("title", "");
        String message = node.optString("message", "");
        String buttonText = node.optString("buttonText", "OK");

        MaterialCardView dialogCard = new MaterialCardView(ctx);
        dialogCard.setRadius(28 * ctx.getResources().getDisplayMetrics().density);
        dialogCard.setCardElevation(6 * ctx.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                (int)(320 * ctx.getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLp.gravity = android.view.Gravity.CENTER;
        dialogCard.setLayoutParams(cardLp);

        LinearLayout inner = new LinearLayout(ctx);
        inner.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(24 * ctx.getResources().getDisplayMetrics().density);
        inner.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(ctx);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView messageView = new TextView(ctx);
        messageView.setText(message);
        messageView.setTextSize(14);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgLp.topMargin = (int)(12 * ctx.getResources().getDisplayMetrics().density);
        messageView.setLayoutParams(msgLp);

        MaterialButton btn = new MaterialButton(ctx);
        btn.setText(buttonText);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnLp.topMargin = (int)(20 * ctx.getResources().getDisplayMetrics().density);
        btn.setLayoutParams(btnLp);

        btn.setOnClickListener(v -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("action", "dialog:showAlert");
                payload.put("button", buttonText);
                Juiceapp.handleNativeAction("dialog:dismissed", "[" + payload.toString() + "]");
            } catch (Exception e) {
                Log.e("Mu3Dialog", "Failed to send dialog:dismissed", e);
            }
        });

        inner.addView(titleView);
        inner.addView(messageView);
        inner.addView(btn);
        dialogCard.addView(inner);

        return dialogCard;
    }

    @Override
    public void updateView(View view, JSONObject node) {
        if (!(view instanceof MaterialCardView)) return;
        MaterialCardView card = (MaterialCardView) view;
        android.view.View child = card.getChildAt(0);
        if (!(child instanceof LinearLayout)) return;
        LinearLayout inner = (LinearLayout) child;

        String title = node.optString("title", "");
        String message = node.optString("message", "");
        String buttonText = node.optString("buttonText", "OK");

        if (inner.getChildCount() > 0) {
            TextView titleView = (TextView) inner.getChildAt(0);
            titleView.setText(title);
        }
        if (inner.getChildCount() > 1) {
            TextView messageView = (TextView) inner.getChildAt(1);
            messageView.setText(message);
        }
        if (inner.getChildCount() > 2) {
            MaterialButton btn = (MaterialButton) inner.getChildAt(2);
            btn.setText(buttonText);
        }
    }

    private AppCompatActivity getActiveActivity(Context ctx) {
        if (ctx.getApplicationContext() instanceof SweetJuiceApp) {
            return ((SweetJuiceApp) ctx.getApplicationContext()).getActiveActivity();
        }
        return null;
    }
}
