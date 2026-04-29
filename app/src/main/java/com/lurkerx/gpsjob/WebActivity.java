package com.lurkerx.gpsjob;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.graphics.Color;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class WebActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        //window.setStatusBarColor(Color.parseColor("#808080"));
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

        webView.loadUrl(/*"http://10.145.203.193:5100"*/"file:///android_asset/index.html");

        ensureChannel();
        //startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        webView.post(() -> webView.evaluateJavascript(
                "window.onAndroidPermissionsChanged && window.onAndroidPermissionsChanged();",
                null
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.post(()-> webView.evaluateJavascript("window.onResume && window.onResume();", null));
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getApplicationContext().getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel("upload") == null) {
                NotificationChannel channel = new NotificationChannel(
                        "upload",
                        "GPS Upload",
                        NotificationManager.IMPORTANCE_LOW
                );
                nm.createNotificationChannel(channel);
            }
        }
    }
}