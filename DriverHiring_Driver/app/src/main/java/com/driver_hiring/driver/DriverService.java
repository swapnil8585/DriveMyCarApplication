package com.driver_hiring.driver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;

import android.util.Log;
import android.util.Pair;

import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.RestAPI;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DriverService extends Service {

    private static final String TAG = "DriverService";
    private FusedLocationProviderClient mFusedClient;
    private LocationRequest mLocationRequest;
    public static Location mLocation;

    private Timer timer;
    private TimerTask mTimerTask;
    private Handler mLocationHandler = new Handler();

    private String DID = "";
    private static boolean isCheck = true;

    public DriverService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mFusedClient = LocationServices.getFusedLocationProviderClient(DriverService.this);
        mLocationRequest = createLocationRequest();
        DID = PreferenceManager.getUserId(DriverService.this);
        Log.d(TAG, "onCreate: Service");
        startLocationUpdates();
        timer = new Timer();
        startLocUpdate();
        timer.schedule(mTimerTask, 0, 3000);

    }

    private void startLocUpdate() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mLocationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (DID.compareTo("") != 0) {
                            Log.d(TAG, "run: Async");
                            if (HistoryActivity.startMockLocation) {
                                Log.d(TAG, "run: Mock is Turned On");
                                if (mLocation != null) {
                                    mFusedClient.removeLocationUpdates(mLocationCallback);
                                    if (isCheck) {
                                        isCheck = false;
                                        new StartMockLoction().execute(DID);
                                    }
                                }
                            } else {
                                Log.d(TAG, "run: Mock is Turned Off");
                                if (mLocation != null) {
                                    if (isCheck) {
                                        isCheck = false;
                                        new UpdateDriverLocation().execute(DID, mLocation.getLatitude() + "," + mLocation.getLongitude());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /********************************
     ****** Location Functions ******
     ********************************/

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: Location Update Started");
        mFusedClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult.getLastLocation() != null) {
                mLocation = locationResult.getLastLocation();
                Log.d(TAG, String.format("Location : Lat - %f, Lng -%f", mLocation.getLatitude(), mLocation.getLongitude()));
            } else {
                Log.d(TAG, "Location is Null");
            }
        }
    };


    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                    Integer.MAX_VALUE)) {
                if (getClass().getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class UpdateDriverLocation extends AsyncTask<String, JSONObject, String> {

        private static final String TAG = "LocationUpdate";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Location AsyncTask");
        }

        @Override
        protected String doInBackground(String... strings) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.DupdateLocation(strings[0], strings[1]);
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
            if (Utility.checkConnection(s)) {
                isCheck = true;
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Log.d(TAG, String.format("Local Error : %s %s", pair.first, pair.second));
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {
                        isCheck = true;
                        Log.d(TAG, "Location Update Success");
                    } else {
                        isCheck = true;
                        String error = json.getString("Data");
                        Log.d(TAG, "Server Error " + error);
                    }

                } catch (Exception e) {
                    isCheck = true;
                    Log.d(TAG, "Catch : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private static class StartMockLoction extends AsyncTask<String, JSONObject, String> {

        private static final String TAG = "MockLocation";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Location AsyncTask");
        }

        @Override
        protected String doInBackground(String... strings) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.AgetDriver(strings[0]);
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
            if (Utility.checkConnection(s)) {
                isCheck = true;
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Log.d(TAG, String.format("Local Error : %s %s", pair.first, pair.second));
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("ok") == 0) {
                        isCheck = true;
                        JSONArray jsonElements = json.getJSONArray("Data");
                        JSONObject js = jsonElements.getJSONObject(0);
                        ////did,name,email,contact,dob,photo,carmodel,carno,pass,curloc,status,cid,isworking
                        String[] latLng = js.getString("data9").split(",");
                        Log.d(TAG, "onPostExecute: LatLng = " + Arrays.asList(latLng).toString());

                        mLocation = new Location("");
                        mLocation.setLatitude(Double.parseDouble(latLng[0]));
                        mLocation.setLongitude(Double.parseDouble(latLng[1]));

                        Log.d(TAG, "Location Update Success");
                    } else {
                        isCheck = true;
                        String error = json.getString("Data");
                        Log.d(TAG, "Server Error " + error);
                    }

                } catch (Exception e) {
                    isCheck = true;
                    Log.d(TAG, "Catch : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mFusedClient != null && mLocationCallback != null) {
            Log.d(TAG, "onDestroy: Removing Location Updates");
            mFusedClient.removeLocationUpdates(mLocationCallback);
        }

        if (timer != null && mTimerTask != null) {
            timer.cancel();
            mTimerTask.cancel();

            timer = null;
            mTimerTask = null;
        }
        super.onDestroy();
    }
}
