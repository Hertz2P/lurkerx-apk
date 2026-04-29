package com.lurkerx.gpsjob;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class AssetUrlProvider {

    private static final String URL_FILE = "url.txt";

    public static String getUrl(Context context) {
        AssetManager assetManager = context.getAssets();

        try (InputStream is = assetManager.open(URL_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line = reader.readLine();
            return line != null ? line.trim() : null;

        } catch (IOException e) {
            // file missing or unreadable
            return null;
        }
    }
}
