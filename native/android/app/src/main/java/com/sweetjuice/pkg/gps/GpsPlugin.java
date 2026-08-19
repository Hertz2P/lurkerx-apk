package com.sweetjuice.pkg.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.sweetjuice.plugin.SweetJuicePlugin;
import org.json.JSONException;
import org.json.JSONObject;

import juiceapp.Juiceapp;

public class GpsPlugin implements SweetJuicePlugin {

    private static final String TAG = "GpsPlugin";
    private Context mContext;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public String getDomain() { return "gps"; }

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public String handleAction(String action, String jsonArgsPayload) {
        try {
            if ("getCurrentLocation".equals(action)) {
                return getCurrentLocation();
            }
            if ("startMonitoring".equals(action)) {
                return startMonitoring();
            }
            if ("stopMonitoring".equals(action)) {
                return stopMonitoring();
            }
            return new JSONObject().put("error", "Unknown action").toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return errorJson("Location permission not granted");
        }

        Location gpsLoc = null;
        Location netLoc = null;
        try {
            gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.w(TAG, "GPS provider unavailable", e);
        }
        try {
            netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.w(TAG, "Network provider unavailable", e);
        }

        Location best = null;
        if (gpsLoc != null && netLoc != null) {
            best = gpsLoc.getAccuracy() < netLoc.getAccuracy() ? gpsLoc : netLoc;
        } else if (gpsLoc != null) {
            best = gpsLoc;
        } else if (netLoc != null) {
            best = netLoc;
        }

        if (best == null) {
            return errorJson("Location unavailable");
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("latitude", best.getLatitude());
            obj.put("longitude", best.getLongitude());
            obj.put("accuracy", best.getAccuracy());
            obj.put("altitude", best.getAltitude());
            obj.put("speed", best.getSpeed());
            obj.put("timestamp", best.getTime());
            return obj.toString();
        } catch (JSONException e) {
            return errorJson(e.getMessage());
        }
    }

    private String startMonitoring() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return errorJson("Location permission not granted");
        }

        try {
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(Location loc) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("latitude", loc.getLatitude());
                        obj.put("longitude", loc.getLongitude());
                        obj.put("accuracy", loc.getAccuracy());
                        obj.put("altitude", loc.getAltitude());
                        obj.put("speed", loc.getSpeed());
                        obj.put("timestamp", loc.getTime());
                        Juiceapp.handleNativeAction("gps:changed", obj.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to emit location", e);
                    }
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            return okJson("monitoring started");
        } catch (Exception e) {
            return errorJson(e.getMessage());
        }
    }

    private String stopMonitoring() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
        return okJson("monitoring stopped");
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
