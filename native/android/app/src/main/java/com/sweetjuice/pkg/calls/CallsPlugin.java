package com.sweetjuice.pkg.calls;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CallsPlugin provides access to the native Android call logs.
 * It supports fetching recent and historical call records.
 */
public class CallsPlugin implements SweetJuicePlugin {

    private static final String TAG = "CallsPlugin";
    private Context mContext;

    @Override
    public String getDomain() { return "calls"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            JSONObject args = new JSONObject(jsonArgsPayload);
            if ("getRecent".equals(action) || "getLast".equals(action)) {
                int limit = args.optInt("limit", 50);
                return getCalls(limit);
            }
            if ("getAll".equals(action)) {
                return getCalls(-1);
            }
            return new JSONObject().put("error", "Unknown action").toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String getCalls(int limit) {
        Cursor cursor = null;
        try {
            String sortOrder = CallLog.Calls.DATE + " DESC";

            cursor = mContext.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    sortOrder
            );

            if (cursor == null) {
                return callLogJson(0, new JSONArray());
            }

            JSONArray calls = new JSONArray();
            int count = 0;

            while (cursor.moveToNext()) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                try {
                    JSONObject call = new JSONObject();
                    call.put("id", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls._ID)));
                    call.put("number", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)));
                    call.put("type", callTypeToString(cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))));
                    call.put("date", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)));
                    call.put("duration", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                    call.put("cached_name", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)));

                    String geo = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.GEOCODED_LOCATION));
                    if (geo != null && !geo.isEmpty()) {
                        call.put("geo_location", geo);
                    }

                    calls.put(call);
                    count++;
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to parse call row", e);
                }
            }

            return callLogJson(count, calls);
        } catch (Exception e) {
            return errorJson(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String callTypeToString(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "incoming";
            case CallLog.Calls.OUTGOING_TYPE: return "outgoing";
            case CallLog.Calls.MISSED_TYPE: return "missed";
            case CallLog.Calls.VOICEMAIL_TYPE: return "voicemail";
            case CallLog.Calls.REJECTED_TYPE: return "rejected";
            case CallLog.Calls.BLOCKED_TYPE: return "blocked";
            case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE: return "answered_externally";
            default: return "unknown";
        }
    }

    private String callLogJson(int count, JSONArray calls) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("count", count);
            obj.put("calls", calls);
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
