package com.sweetjuice.pkg.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * SmsPlugin provides read-only access to device SMS messages across various folders.
 */
public class SmsPlugin implements SweetJuicePlugin {

    private static final String TAG = "SmsPlugin";
    private Context mContext;

    @Override
    public String getDomain() { return "sms"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            JSONObject args = new JSONObject(jsonArgsPayload);
            if ("getRecent".equals(action) || "getLast".equals(action)) {
                int limit = args.optInt("limit", 100);
                return getRecentSms(limit);
            }
            if ("getInbox".equals(action)) {
                return getSmsFolder(Telephony.Sms.Inbox.CONTENT_URI, "inbox", -1);
            }
            if ("getSent".equals(action)) {
                return getSmsFolder(Telephony.Sms.Sent.CONTENT_URI, "sent", -1);
            }
            if ("getDrafts".equals(action)) {
                return getSmsFolder(Telephony.Sms.Draft.CONTENT_URI, "draft", -1);
            }
            if ("getAll".equals(action)) {
                return getAllSms();
            }
            return new JSONObject().put("error", "Unknown action").toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String getRecentSms(int limit) {
        return getSmsFolder(Telephony.Sms.CONTENT_URI, "all", limit);
    }

    private String getSmsFolder(Uri uri, String folder, int limit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(mContext);
            if (defaultSmsPackage != null && !defaultSmsPackage.equals(mContext.getPackageName())) {
                // Warning only, try to read anyway as we might have READ_SMS permission
                Log.w(TAG, "Not default SMS app, might fail to read if no permission");
            }
        }

        Cursor cursor = null;
        try {
            String sortOrder = "date DESC";

            cursor = mContext.getContentResolver().query(uri, null, null, null, sortOrder);
            if (cursor == null) {
                return folderJson(folder, 0, new JSONArray());
            }

            JSONArray messages = new JSONArray();
            int count = 0;

            while (cursor.moveToNext()) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("id", cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                    msg.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                    msg.put("body", cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    
                    // If uri is generic CONTENT_URI, we might want to get the folder from the type column
                    String typeStr = folder;
                    if ("all".equals(folder)) {
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                        switch (type) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX: typeStr = "inbox"; break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT: typeStr = "sent"; break;
                            case Telephony.Sms.MESSAGE_TYPE_DRAFT: typeStr = "draft"; break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX: typeStr = "outbox"; break;
                            case Telephony.Sms.MESSAGE_TYPE_FAILED: typeStr = "failed"; break;
                            case Telephony.Sms.MESSAGE_TYPE_QUEUED: typeStr = "queued"; break;
                            default: typeStr = "unknown";
                        }
                    }

                    msg.put("type", typeStr);
                    msg.put("timestamp", cursor.getLong(cursor.getColumnIndexOrThrow("date")));
                    msg.put("read", cursor.getInt(cursor.getColumnIndexOrThrow("read")) == 1);
                    messages.put(msg);
                    count++;
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to parse SMS row", e);
                }
            }

            return folderJson(folder, count, messages);
        } catch (Exception e) {
            return errorJson(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getAllSms() {
        return getSmsFolder(Telephony.Sms.CONTENT_URI, "all", -1);
    }

    private String folderJson(String folder, int count, JSONArray messages) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("folder", folder);
            obj.put("count", count);
            obj.put("messages", messages);
            return obj.toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String okJson(String status) {
        try {
            return new JSONObject().put("status", status).toString();
        } catch (JSONException e) {
            return "{\"status\":\"" + status + "\"}";
        }
    }

    private String errorJson(String message) {
        try {
            return new JSONObject().put("error", message).toString();
        } catch (JSONException e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] g) {}
    @Override public void onActivityResult(int r, int rc, android.content.Intent d) {}
    @Override public void onNewIntent(android.content.Intent intent) {}
}
