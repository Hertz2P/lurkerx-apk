package com.sweetjuice.pkg.datadir;

import android.content.Context;
import android.content.Intent;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * DataDirPlugin exposes standard Android application directories (internal and external)
 * and provides basic file system operations within the app's private internal storage.
 */
public class DataDirPlugin implements SweetJuicePlugin {
    private Context mContext;

    @Override
    public String getDomain() { return "datadir"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        if (mContext == null) return "{\"error\":\"Context not attached\"}";

        try {
            switch (action) {
                case "getDirs":
                    return getDirs();
                case "readFile":
                    return readFile(jsonArgsPayload);
                case "writeFile":
                    return writeFile(jsonArgsPayload);
                case "fileExists":
                    return fileExists(jsonArgsPayload);
                case "deleteFile":
                    return deleteFile(jsonArgsPayload);
                default:
                    return "{\"error\":\"Unknown action\"}";
            }
        } catch (JSONException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String getDirs() throws JSONException {
        JSONObject dirs = new JSONObject();

        // Private internal storage
        dirs.put("files", mContext.getFilesDir().getAbsolutePath());
        dirs.put("cache", mContext.getCacheDir().getAbsolutePath());

        // External storage (scoped to app)
        File extFiles = mContext.getExternalFilesDir(null);
        if (extFiles != null) {
            dirs.put("external_files", extFiles.getAbsolutePath());
        }

        File extCache = mContext.getExternalCacheDir();
        if (extCache != null) {
            dirs.put("external_cache", extCache.getAbsolutePath());
        }

        return dirs.toString();
    }

    private String readFile(String jsonArgsPayload) throws JSONException {
        JSONObject args = new JSONObject(jsonArgsPayload);
        String path = args.optString("path", "");

        if (path.isEmpty()) {
            return fileResult(false, "", "path is required");
        }

        File file = new File(mContext.getFilesDir(), path);
        if (!file.exists()) {
            return fileResult(false, "", "file not found: " + path);
        }

        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (content.length() > 0) {
                    content.append('\n');
                }
                content.append(line);
            }
        } catch (IOException e) {
            return fileResult(false, "", "failed to read file: " + e.getMessage());
        }

        return fileResult(true, content.toString(), "");
    }

    private String writeFile(String jsonArgsPayload) throws JSONException {
        JSONObject args = new JSONObject(jsonArgsPayload);
        String path = args.optString("path", "");
        String content = args.optString("content", "");

        if (path.isEmpty()) {
            return fileResult(false, "", "path is required");
        }

        File file = new File(mContext.getFilesDir(), path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            fos.flush();
        } catch (IOException e) {
            return fileResult(false, "", "failed to write file: " + e.getMessage());
        }

        return fileResult(true, "", "");
    }

    private String fileExists(String jsonArgsPayload) throws JSONException {
        JSONObject args = new JSONObject(jsonArgsPayload);
        String path = args.optString("path", "");

        if (path.isEmpty()) {
            return fileResult(false, "", "path is required");
        }

        File file = new File(mContext.getFilesDir(), path);
        return fileResult(file.exists(), "", "");
    }

    private String deleteFile(String jsonArgsPayload) throws JSONException {
        JSONObject args = new JSONObject(jsonArgsPayload);
        String path = args.optString("path", "");

        if (path.isEmpty()) {
            return fileResult(false, "", "path is required");
        }

        File file = new File(mContext.getFilesDir(), path);
        if (!file.exists()) {
            return fileResult(false, "", "file not found: " + path);
        }

        if (!file.delete()) {
            return fileResult(false, "", "failed to delete file: " + path);
        }

        return fileResult(true, "", "");
    }

    private String fileResult(boolean success, String content, String error) {
        try {
            JSONObject result = new JSONObject();
            result.put("success", success);
            if (content.isEmpty()) {
                result.put("content", JSONObject.NULL);
            } else {
                result.put("content", content);
            }
            result.put("error", error);
            return result.toString();
        } catch (JSONException e) {
            return "{\"success\":" + success + ",\"content\":null,\"error\":\"" + error + "\"}";
        }
    }

    @Override public void onActivityResult(int req, int res, Intent d) {}
    @Override public void onRequestPermissionsResult(int req, String[] p, int[] res) {}
    @Override public void onNewIntent(Intent intent) {}
}
