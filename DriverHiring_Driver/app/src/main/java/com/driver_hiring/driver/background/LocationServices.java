package com.driver_hiring.driver.background;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationServices implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int THRESHOLD_ACCURACY = 500;

    public static Double latitude = 0.0;
    public static Double longitude = 0.0;


    public static final int MAX_WAIT_TIME = 60000 * 5;
    private static final long INTERVAL = 60000 * 4;
    private static final long FASTEST_INTERVAL = 60000 * 3;
    private static final String TAG = "OldLServices";
    private Double lat = 0.0, lng = 0.0;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private FusedLocationProviderApi fusedLocationProviderApi = com.google.android.gms.location.LocationServices.FusedLocationApi;
    private Context context;

    public LocationServices(Context con) {

        context = con;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
//                .enableAutoManage( context, this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: Constant = " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: Result = " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        latitude = lat;
        longitude = lng;

        if (location.isFromMockProvider())
            Log.d(TAG, "onLocationChanged: Location From Mock Provider : "
                    + location.getProvider());

        if (location.hasAccuracy()) {

            Log.d(TAG, "onLocationChanged: Location Accuracy = " + location.getAccuracy());
        }

    }


}
