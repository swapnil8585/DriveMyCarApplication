package com.driver_hiring.driver.background;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RideLocationUpdate extends Service {

    private static final String TAG = "RideLocation";
    //Server Data Variables
    private String date, time;
    private SimpleDateFormat dateFormat, timeFormat;
    private Date dt;
    private Context mContext;

    // Flag for GPS status
    boolean isGPSEnabled = false;
    private Boolean internet = null;

    //GPS Access variables
    private ConnectivityManager cm;
    double latitude = 0.0; // Latitude
    double longitude = 0.0; // Longitude
    protected GoogleApiClient googleApiClient;

    //Timer to Run on Background
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    //Wake-Lock and Power Manger - Lock
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private WifiManager wm;
    private WifiManager.WifiLock wlock;

    private TimerTask task;
    private Handler hand = new Handler();

    private LocationServices locationServices;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onStartCommand(intent, flags, startId);
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            Log.d(TAG, "onCreate: ");

            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            timeFormat = new SimpleDateFormat("HH:mm:ss");

            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Hiring:Driver");
            wl.acquire();

            wm = (WifiManager) RideLocationUpdate.this.getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                wlock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "ipro");
                wlock.acquire();
            }


            locationServices = new LocationServices(this);

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .build();
            googleApiClient.connect();

            checkGPS();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkGPS() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isGPSEnabled = true;
                        Log.d(TAG, "GPS Success: ");
                        timer = new Timer();
                        getLocation();
                        timer.schedule(task, 0, 500);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        isGPSEnabled = false;
                        Log.d(TAG, "GPS Failure: ");
                        timer = new Timer();
                        initializeTimerTask();
                        timer.schedule(timerTask, 0, (60000));
                        break;
                }
            }
        });
    }

    public void getLocation() {
        task = new TimerTask() {
            @Override
            public void run() {
                hand.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Location GPS: " + latitude + "," + longitude);
                        if (LocationServices.latitude != 0.0 && LocationServices.longitude != 0.0) {
                            latitude = LocationServices.latitude;
                            longitude = LocationServices.longitude;

                            timer.cancel();
                            task.cancel();
                            timer = null;

                            timer = new Timer();
                            initializeTimerTask();
                            timer.schedule(timerTask, 0, (60000));
                        }
                    }
                });
            }
        };

    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            dt = Calendar.getInstance().getTime();
                            date = dateFormat.format(dt);
                            time = timeFormat.format(dt);

                            internet = checkinternet();

                            Log.d(TAG, "Location Start: ");

                            if (PreferenceManager.isRideAvailable(RideLocationUpdate.this)) {
                                Log.d(TAG, "Location Ride: ");
                                if (internet) {
                                    Log.d(TAG, "Location Internet: ");
                                    if (isGPSEnabled) {

                                        latitude = LocationServices.latitude;
                                        longitude = LocationServices.longitude;

                                        Log.d(TAG, "Location GPS: " + latitude + "," + longitude);

                                        if (latitude != 0 && longitude != 0) {
                                            new SendLatlng().execute(PreferenceManager.getRideID(RideLocationUpdate.this)
                                                    , latitude + "," + longitude, date, time);
                                        }
                                    }
                                } else {

                                    if (isGPSEnabled) {
                                        Log.d(TAG, "Location GPS: ");
                                        latitude = LocationServices.latitude;
                                        longitude = LocationServices.longitude;

                                    }
                                }
                            } else {

                                Log.d(TAG, "run: No Ride Available");
                            }
                        } catch (Exception exp) {

                            latitude = LocationServices.latitude;
                            longitude = LocationServices.longitude;
                        }
                        locationServices = new LocationServices(RideLocationUpdate.this);

                    }

                });
            }
        };
    }

    public Boolean checkinternet() {
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            return true;
        } else {
            return false;
        }

    }

    private boolean checkGPSEnabled() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                com.google.android.gms.location.LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isGPSEnabled = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        isGPSEnabled = false;
//                        ADD(isGPS+"-"+uid+"-"+lat+","+lng,"T");
                        break;
                }
            }
        });
        return isGPSEnabled;
    }

    public class SendLatlng extends AsyncTask<String, JSONObject, String> {

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.DaddLocation(params[0], params[1], params[2], params[3]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);
            if (s.compareTo("true") == 0) {
                dt = Calendar.getInstance().getTime();
                time = timeFormat.format(dt);
            }
        }
    }


    public void closeTimers() {
        try {
            timer.cancel();
            timerTask.cancel();
            timer = null;
        } catch (Exception e) {
        }

        try {
            wl.release();
        } catch (Exception e) {
        }

        try {
            wlock.release();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeTimers();
    }
}
