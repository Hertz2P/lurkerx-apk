package com.lurkerx.gpsjob;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class GpsFetch {

    public interface Callback {
        void onLocation(double lat, double lon, float accuracy, long timestamp);
        void onError(String reason);
    }

    private static final String TAG = "GpsFetch";
    private static final long TIMEOUT_MS = 15_000; // 15 seconds

    private final Context context;
    private final FusedLocationProviderClient locationClient;
    private final HandlerThread gpsThread;
    private final Handler gpsHandler;

    private boolean requestInProgress = false;

    public GpsFetch(Context context) {
        this.context = context.getApplicationContext();
        this.locationClient = LocationServices.getFusedLocationProviderClient(this.context);

        gpsThread = new HandlerThread("GpsWorker");
        gpsThread.start();
        gpsHandler = new Handler(gpsThread.getLooper());
    }

    public synchronized void fetchOnce(@NonNull Callback callback) {
        if (requestInProgress) {
            callback.onError("GPS request already in progress");
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("GPS permission not granted");
            return;
        }

        requestInProgress = true;

        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0)
                .setNumUpdates(1);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                locationClient.removeLocationUpdates(this);
                requestInProgress = false;

                Location location = result.getLastLocation();
                if (location == null) {
                    fallbackLastKnown(callback);
                    return;
                }

                deliverLocation(location, callback);
            }
        };

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                gpsHandler.getLooper()
        );

        // timeout safety net
        gpsHandler.postDelayed(() -> {
            if (requestInProgress) {
                Log.w(TAG, "GPS timeout, falling back");
                locationClient.removeLocationUpdates(locationCallback);
                requestInProgress = false;
                fallbackLastKnown(callback);
            }
        }, TIMEOUT_MS);
    }

    private void fallbackLastKnown(Callback callback) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("GPS permission not granted");
            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        callback.onError("No last known location");
                        return;
                    }
                    deliverLocation(location, callback);
                })
                .addOnFailureListener(e ->
                        callback.onError("Last location failed: " + e.getMessage())
                );
    }

    private void deliverLocation(Location location, Callback callback) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        float acc = location.hasAccuracy() ? location.getAccuracy() : -1f;
        long ts = location.getTime() > 0
                ? location.getTime()
                : System.currentTimeMillis();

        Log.d(TAG, "Received: " + lat + ", " + lon + ", " + acc + ", " + ts);

        callback.onLocation(lat, lon, acc, ts);
    }

    public void shutdown() {
        gpsThread.quitSafely();
    }
}
