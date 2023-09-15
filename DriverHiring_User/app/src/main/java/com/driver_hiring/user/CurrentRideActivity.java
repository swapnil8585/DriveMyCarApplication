package com.driver_hiring.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.models.RideModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.driver_hiring.user.RideBookActivity.PERMISSION_REQUEST_CODE;
import static com.driver_hiring.user.RideBookActivity.REQUEST_CHECK_SETTINGS;
import static com.driver_hiring.user.RideBookActivity.START_BACKGROUND;
import static com.driver_hiring.user.RideBookActivity.STOP_BACKGROUND;

public class CurrentRideActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String DRIVER_IMG = "ID";
    public static final String RIDE_DETAILS = "Ride_Details";
    private static final String TAG = "CurrentRide";

    private String address = "";

    private GoogleMap mMap;
    private Marker mSourceMarker, mDestinationMarker, mDriverMarker;

    private ProgressDialog mProgressDialog;

    //Cab Views
    private LinearLayout mDestPart;
    private CardView mCabMainHolder;
    private Button cabConfirmButton;


    /****************************
     **********Driver View*******
     ****************************/
    private CardView mDriverHolder;

    private CircleImageView driverImage;
    private TextView mDriverName, mDriverDistance, mDriverPrice;
    private TextView mStartDate, mStartTime, mEndDate, mEndTime;
    private TextView mSourceAddress, mDestinationAddress;
    private RatingBar mDriverRating;

    private String driverImg;
    private static String driverLatLng, driverLocDate, driverLocTime;

    private boolean mViewIsSwitched = false;

    private RideModel mRideDetails;
    private Location mSrcLocation, mDestination;
    private CabHandler cabHandler;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_fragment);

        getSupportActionBar().setTitle("Current Ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mProgressDialog = new ProgressDialog(CurrentRideActivity.this);
        mProgressDialog.setCancelable(false);

        initViews();

        String UID = PreferenceManager.getUserId(CurrentRideActivity.this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cabHandler = new CabHandler(CurrentRideActivity.this, CurrentRideActivity.this);
        cabHandler.sendEmptyMessage(START_BACKGROUND);
    }

    private void initViews() {
        findViewById(R.id.edt_source_add).setVisibility(View.GONE);
        findViewById(R.id.edt_dest_add1).setVisibility(View.GONE);
        findViewById(R.id.edt_dest_add2).setVisibility(View.GONE);
        findViewById(R.id.tbl_dest_add2).setVisibility(View.GONE);

        findViewById(R.id.linear_destination).setVisibility(View.VISIBLE);

        mSourceAddress = findViewById(R.id.edt_source_add_ride);
        mDestinationAddress = findViewById(R.id.edt_dest_add_ride);

        mSourceAddress.setVisibility(View.VISIBLE);
        mDestinationAddress.setVisibility(View.VISIBLE);

        mCabMainHolder = findViewById(R.id.maps_main_holder);
        cabConfirmButton = findViewById(R.id.buttonConfirmCab);

        //Driver View
        mDriverHolder = findViewById(R.id.driver_view_holder);

        driverImage = findViewById(R.id.driver_image);
        mDriverName = findViewById(R.id.driver_name);
        mDriverRating = findViewById(R.id.driver_rating);
        mDriverDistance = findViewById(R.id.driver_trip_distance);
        mDriverPrice = findViewById(R.id.driver_trip_price);

        mStartDate = findViewById(R.id.mstart_date);
        mStartTime = findViewById(R.id.mstart_time);
        mEndDate = findViewById(R.id.mend_date);
        mEndTime = findViewById(R.id.mend_time);

        cabConfirmButton.setText("Confirm Ride");

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Bundle bundle;
        if (getIntent().getExtras() != null) {
            bundle = getIntent().getBundleExtra(RIDE_DETAILS);
            String mRideString = bundle.getString(RIDE_DETAILS);
            Log.d("MAIN", "onCreate: " + mRideString);
            mRideDetails = (RideModel) new Gson().fromJson(mRideString, RideModel.class);
            driverImg = bundle.getString(DRIVER_IMG);
        }

        mMap = googleMap;

        if (checkPermissions()) {
            showSettingDialog();
        } else {
            Log.d("DEBUG", "onMapReady: Permission Check");
            requestPermissions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ic_menu_ride, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.menu_switch_view) {
            if (!mViewIsSwitched) {
                mViewIsSwitched = true;
                switchMapViews(true);
            } else {
                mViewIsSwitched = false;
                switchMapViews(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_switch_view);
        if (mViewIsSwitched) {
            menuItem.setTitle("Show All");
        } else {
            menuItem.setTitle("Show Map");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void switchMapViews(boolean switch_view) {
        if (switch_view) {
            //Show Only Map View and Button.
            if (mCabMainHolder.getVisibility() == View.VISIBLE)
                mCabMainHolder.setVisibility(View.GONE);
            if (mDriverHolder.getVisibility() == View.VISIBLE)
                mDriverHolder.setVisibility(View.GONE);
        } else {
            //Show Only All Views
            if (mCabMainHolder.getVisibility() == View.GONE)
                mCabMainHolder.setVisibility(View.VISIBLE);

            if (mDriverHolder.getVisibility() == View.GONE)
                mDriverHolder.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();
    }

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off
        SettingsClient client = LocationServices.getSettingsClient(CurrentRideActivity.this);

        Log.d("DEBUG", "onMapReady: GPS Check");

        Task<LocationSettingsResponse> result = client.checkLocationSettings(builder.build());
        result.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("DEBUG", "onSuccess: onSuccess");
                startLocationUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.d("DEBUG", "onSuccess: ResolvableApiException");
                    try {
                        ((ResolvableApiException) e)
                                .startResolutionForResult(CurrentRideActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
    }

    private void CloseKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void startLocationUpdates() {
        Log.d("DEBUG", "startLocationUpdates: ");
        if (ActivityCompat.checkSelfPermission(CurrentRideActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(CurrentRideActivity.this
                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG", "startLocationUpdates: Permission Granted");
            startLastLocation();
        } else {
            ActivityCompat.requestPermissions(CurrentRideActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(CurrentRideActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(CurrentRideActivity.this
                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.d("DEBUG", "requestPermissions: Requesting Permission");
        ActivityCompat.requestPermissions(CurrentRideActivity.this
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLastLocation() {
        Log.d("DEBUG", "startLastLocation: Starting Location Check");
        //Start Background Location Check

        //setValues
        showCabView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                Log.d("DEBUG", "onMapReady: Permission Granted");
                showSettingDialog();

            } else {
                // Permission was denied or request was cancelled
                Log.d("DEBUG", "onMapReady: Not Permission Granted");
                ActivityCompat.requestPermissions(CurrentRideActivity.this
                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void showCabView() {
        driverImage.setImageBitmap(getImage(mRideDetails.getPhoto()));
        mDriverName.setText(mRideDetails.getUserName());

        mDriverPrice.setText(String.format(Locale.getDefault()
                , "%s %s", getResources().getString(R.string.currency)
                , mRideDetails.getPrice()));

        if (mRideDetails.getRatings().length() > 0)
            mDriverRating.setRating(Float.parseFloat(mRideDetails.getRatings()));

        mSourceAddress.setText(mRideDetails.getStartAddress());
        mDestinationAddress.setText(mRideDetails.getEndAddress());

        String[] location = mRideDetails.getSourceLatLng().split(",");

        mSrcLocation = new Location("");
        mSrcLocation.setLatitude(Double.parseDouble(location[0]));
        mSrcLocation.setLongitude(Double.parseDouble(location[1]));

        mSourceMarker = mMap.addMarker(new MarkerOptions()
                .title("Source")
                .position(new LatLng(mSrcLocation.getLatitude(), mSrcLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        location = mRideDetails.getEndLatLng().split(",");

        mDestination = new Location("");
        mDestination.setLatitude(Double.parseDouble(location[0]));
        mDestination.setLongitude(Double.parseDouble(location[1]));

        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .title("Destination")
                .position(new LatLng(mDestination.getLatitude(), mDestination.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mDestinationMarker.setTag("Destination");

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDestinationMarker.getPosition(), 15));

        mStartDate.setText(mRideDetails.getStartDate());
        mStartTime.setText(mRideDetails.getStartTime());

        mEndDate.setText(mRideDetails.getEndDate());
        mEndTime.setText(mRideDetails.getEndTime());

        mDriverDistance.setText((mSrcLocation.distanceTo(mDestination) / 1000) + " km");


        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (mDriverHolder.getVisibility() == View.GONE)
            mDriverHolder.setVisibility(View.VISIBLE);

    }

    private Bitmap getImage(String driverImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(driverImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    @Override
    public void onDestroy() {
        if (cabHandler != null) {
            cabHandler.sendEmptyMessage(STOP_BACKGROUND);
        }
        super.onDestroy();
    }

    private static class CabHandler extends Handler {
        private Context appContext;
        private CurrentRideActivity mCurrentRide;
        private TimerTask timerTask;
        private Timer timer;
        private boolean check = true;

        public CabHandler(@NonNull Context context, CurrentRideActivity mapsFragment) {
            this.appContext = context;
            this.mCurrentRide = mapsFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_BACKGROUND:
                    startBackgroundCheck();
                    break;
                case STOP_BACKGROUND:
                    stopBackgroundCheck();
                    break;
            }
        }

        private void stopBackgroundCheck() {
            if (timer != null && timerTask != null) {
                timer.cancel();
                timerTask.cancel();

                timerTask = null;
                timer = null;
            }
        }

        private void startBackgroundCheck() {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (check) {
                                    check = false;
                                    new GetDriverLocation(mCurrentRide).execute(mCurrentRide
                                            .mRideDetails.getRideID());
                                }
                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                        }
                    });
                }
            };

            timer = new Timer();
            timer.schedule(timerTask, 0, 3000);
        }
    }

    public static class GetDriverLocation extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";
        private WeakReference<CurrentRideActivity> mCurrentReference;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        private GetDriverLocation(CurrentRideActivity currentRideActivity) {
            this.mCurrentReference = new WeakReference<CurrentRideActivity>(currentRideActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
//            Log.d(TAG, "doInBackground: Date : " + params[1]);
            try {
                JSONObject json = api.UgetDriverLocation(params[0]);
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

            Log.d(TAG, String.format("Current: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Log.d(TAG, "onPostExecute: " + pair.second);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {
                        Log.d(TAG, "onPostExecute: No Locations");
                        new GetCurrentCabs(mCurrentReference.get()).execute(
                                PreferenceManager.getUserId(mCurrentReference.get())
                                , dateFormat.format(new Date().getTime()), "Current");
                    } else if (StatusValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject js = jsonArray.getJSONObject(0);
                        driverLatLng = js.getString("data0");
                        driverLocDate = js.getString("data1");
                        driverLocTime = js.getString("data2");

                        mCurrentReference.get().
                                plotDriverLocation(driverLatLng, driverLocDate, driverLocTime);


                    } else {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                        new GetCurrentCabs(mCurrentReference.get()).execute(
                                PreferenceManager.getUserId(mCurrentReference.get())
                                , dateFormat.format(new Date().getTime()), "Current");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    new GetCurrentCabs(mCurrentReference.get()).execute(
                            PreferenceManager.getUserId(mCurrentReference.get())
                            , dateFormat.format(new Date().getTime()), "Current");
                }

            }

        }
    }

    private void plotDriverLocation(String driverLatLng, String driverLocDate, String driverLocTime) {
        String[] location = driverLatLng.split(",");

        LatLng mDriverLocation = new LatLng(Double.parseDouble(location[0].trim()
        ), Double.parseDouble(location[1].trim()));
        if (mDriverMarker != null)
            mDriverMarker.remove();

        mDriverMarker = mMap.addMarker(new MarkerOptions()
                .snippet(String.format("Date : %s\n Time : %s"
                        , driverLocDate, driverLocTime))
                .position(mDriverLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDriverLocation, 11));

        new GetCurrentCabs(CurrentRideActivity.this).execute(PreferenceManager.getUserId(CurrentRideActivity.this)
                , dateFormat.format(new Date().getTime()), "Current");
    }

    public static class GetCurrentCabs extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";
        private WeakReference<CurrentRideActivity> mCurrentReference;

        private GetCurrentCabs(@NonNull CurrentRideActivity currentRideActivity) {
            mCurrentReference = new WeakReference<CurrentRideActivity>(currentRideActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            Log.d(TAG, "doInBackground: Date : " + params[1]);
            try {
                JSONObject json = api.UgetRides(params[0], params[1], params[2]);
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
            mCurrentReference.get().cabHandler.check = true;
            Log.d(TAG, String.format("Current: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Log.d(TAG, "onPostExecute: " + pair.first + "" + pair.second);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {
                        Log.d(TAG, "onPostExecute: Ride Finsihed");
                        Toast.makeText(mCurrentReference.get(), "Ride has been Finished"
                                , Toast.LENGTH_SHORT).show();
                        mCurrentReference.get().finish();
                    } else if (StatusValue.compareTo("ok") == 0) {
                        //DO Nothing
                        Log.d(TAG, "onPostExecute: Ride Going On");
                    } else {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

}
