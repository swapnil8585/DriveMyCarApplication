package com.driver_hiring.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.driver_hiring.user.helper.MySingleton;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.helper.TransactionDialog;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class RideBookActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String DRIVER_ID = "ID";
    public static final String DRIVER_NAME = "Name";
    public static final String DRIVER_RATING = "Rating";
    public static final String DRIVER_PRICE = "Price";
    public static final String DRIVER_IMG = "Image";
    public static final String CAB_ID = "Cab_Id";
    public static final String RIDE_DAYS = "Days";
    public static final String PLACES = "Places";

    public static final int REQUEST_CHECK_SETTINGS = 1001;
    public static final int PERMISSION_REQUEST_CODE = 1002;
    private static final String TAG = "MapFragment";
    private FusedLocationProviderClient mFusedClient;
    private Location mLocation, mDestination;

    private String address = "";

    private GoogleMap mMap;
    private Marker mSourceMarker, mDestinationMarker;

    private androidx.appcompat.widget.SearchView edtSource, edtDestination, edtDestination1;
    private LinearLayout mDestPart;
    private TableRow mDestAdd;


    //All Cabs Data
    public static final int START_BACKGROUND = 21;
    public static final int STOP_BACKGROUND = 23;

    private ArrayList<LatLng> mFinalList;
    private ProgressDialog mProgressDialog;
    private Polyline directionsPolyline;

    //Cab Views
    private CardView mCabMainHolder;
    private CardView mCabHolder;
    private Button cabConfirmButton;
    private String mDestinationDistance;
    private String UID = "";


    /****************************
     **********Driver View*******
     ****************************/
    private CardView mDriverHolder;

    private CircleImageView driverImage;
    private TextView mDriverName, mDriverDistance, mDriverPrice;
    private TextView mStartDate, mStartTime, mEndDate, mEndTime;
    private RatingBar mDriverRating;

    private String driverID, carsId, mPlaceTypes, driverName, driverImg, driverDays, driverPrice, driverRating;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private SimpleDateFormat mTimeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);

    private DatePickerDialog mStartDatePicker, mEndDatePicker;
    private TimePickerDialog mStartTimePicker, mEndTimePicker;

    private Calendar mStartDateCalender, mEndDateCalender;

    private DatePickerDialog.OnDateSetListener onStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mStartDateCalender.set(Calendar.YEAR, year);
            mStartDateCalender.set(Calendar.MONTH, month);
            mStartDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mStartDate.setText(mDateFormat.format(mStartDateCalender.getTimeInMillis()));

            mDriverPrice.setText(String.format(Locale.getDefault()
                    , "%s %.2f", getResources().getString(R.string.currency)
                    , calculatePrice(driverPrice)));
        }
    };

    private DatePickerDialog.OnDateSetListener onEnd = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mEndDateCalender.set(Calendar.YEAR, year);
            mEndDateCalender.set(Calendar.MONTH, month);
            mEndDateCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mEndDate.setText(mDateFormat.format(mEndDateCalender.getTimeInMillis()));

            mDriverPrice.setText(String.format(Locale.getDefault()
                    , "%s %.2f", getResources().getString(R.string.currency)
                    , calculatePrice(driverPrice)));
        }
    };


    private TimePickerDialog.OnTimeSetListener onStartTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mStartDateCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mStartDateCalender.set(Calendar.MINUTE, minute);
            mStartTime.setText(mTimeFormat.format(mStartDateCalender.getTimeInMillis()));

            mDriverPrice.setText(String.format(Locale.getDefault()
                    , "%s %.2f", getResources().getString(R.string.currency)
                    , calculatePrice(driverPrice)));
        }
    };

    private TimePickerDialog.OnTimeSetListener onEndTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mEndDateCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mEndDateCalender.set(Calendar.MINUTE, minute);
            mEndTime.setText(mTimeFormat.format(mEndDateCalender.getTimeInMillis()));

            mDriverPrice.setText(String.format(Locale.getDefault()
                    , "%s %.2f", getResources().getString(R.string.currency)
                    , calculatePrice(driverPrice)));
        }
    };
    private boolean mViewIsSwitched = false;
    private boolean isSearchedFirstTime = false;
    private String totalHours;

    private TransactionDialog transactionDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_fragment);

        getSupportActionBar().setTitle("Book Ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mFusedClient = LocationServices.getFusedLocationProviderClient(RideBookActivity.this);

        mProgressDialog = new ProgressDialog(RideBookActivity.this);
        mProgressDialog.setCancelable(false);

        TransactionDialog.bindTransactionListener(mTransactionComplete);

        initViews();

        UID = PreferenceManager.getUserId(RideBookActivity.this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initViews() {
        edtSource = findViewById(R.id.edt_source_add);
        edtDestination = findViewById(R.id.edt_dest_add1);
        edtDestination1 = findViewById(R.id.edt_dest_add2);
        mDestAdd = findViewById(R.id.tbl_dest_add2);
        mDestPart = findViewById(R.id.linear_destination);

        edtSource.setOnQueryTextListener(onQuerySource);
        edtDestination.setOnQueryTextListener(onQueryDest);
        edtDestination1.setOnQueryTextListener(onQueryDest1);

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

        mStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartDatePicker == null) {
                    mStartDatePicker = new DatePickerDialog(RideBookActivity.this, onStart
                            , mStartDateCalender.get(Calendar.YEAR)
                            , mStartDateCalender.get(Calendar.MONTH)
                            , mStartDateCalender.get(Calendar.DAY_OF_MONTH));
                    mStartDatePicker.getDatePicker().setMinDate(new Date().getTime());
                    mStartDatePicker.getDatePicker().setMaxDate(mEndDateCalender.getTimeInMillis());
                    mStartDatePicker.show();
                } else {
                    mStartDatePicker.show();
                }
            }
        });

        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartTimePicker == null) {
                    mStartTimePicker = new TimePickerDialog(RideBookActivity.this, onStartTime
                            , mStartDateCalender.get(Calendar.HOUR_OF_DAY)
                            , mStartDateCalender.get(Calendar.MINUTE), false);
                    mStartTimePicker.show();
                } else {
                    mStartTimePicker.show();
                }
            }
        });


        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEndDatePicker == null) {
                    mEndDatePicker = new DatePickerDialog(RideBookActivity.this, onEnd
                            , mEndDateCalender.get(Calendar.YEAR)
                            , mEndDateCalender.get(Calendar.MONTH)
                            , mEndDateCalender.get(Calendar.DAY_OF_MONTH));
                    mEndDatePicker.getDatePicker().setMinDate(mStartDateCalender.getTimeInMillis());
                    mEndDatePicker.show();
                } else {
                    mEndDatePicker.show();
                }
            }
        });


        mEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEndTimePicker == null) {
                    mEndTimePicker = new TimePickerDialog(RideBookActivity.this, onEndTime
                            , mEndDateCalender.get(Calendar.HOUR_OF_DAY)
                            , mEndDateCalender.get(Calendar.MINUTE), false);
                    mEndTimePicker.show();
                } else {
                    mEndTimePicker.show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        driverName = getIntent().getStringExtra(DRIVER_NAME);
        driverImg = getIntent().getStringExtra(DRIVER_IMG);
        driverRating = getIntent().getStringExtra(DRIVER_RATING);
        driverPrice = getIntent().getStringExtra(DRIVER_PRICE);
        driverDays = getIntent().getStringExtra(RIDE_DAYS);

        driverID = getIntent().getStringExtra(DRIVER_ID);
        mPlaceTypes = getIntent().getStringExtra(PLACES);
        carsId = getIntent().getStringExtra(CAB_ID);

        mStartDateCalender = Calendar.getInstance();
        mEndDateCalender = Calendar.getInstance();

        mEndDateCalender.add(Calendar.DAY_OF_MONTH, Integer.parseInt(driverDays));
        mEndDateCalender.set(Calendar.HOUR_OF_DAY, 10);
        mEndDateCalender.set(Calendar.MINUTE, 0);

        mStartDate.setText(mDateFormat.format(mStartDateCalender.getTimeInMillis()));
        mStartTime.setText(mTimeFormat.format(mStartDateCalender.getTimeInMillis()));

        mEndDate.setText(mDateFormat.format(mEndDateCalender.getTimeInMillis()));
        mEndTime.setText(mTimeFormat.format(mEndDateCalender.getTimeInMillis()));

        mMap = googleMap;

        mMap.setOnMarkerDragListener(markerDragListener);

        if (checkPermissions()) {
            showSettingDialog();
        } else {
            Log.d("DEBUG", "onMapReady: Permission Check");
            requestPermissions();
        }


        cabConfirmButton.setOnClickListener(onClickListener);
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //string uid, string did, string cid, string sdate, string stime, string sadd, string slatlng
            // , string edate, string etime,string eadd,string elatlng
            // ,string totdays,string tothours,string ptype,string Bdatetime,string price
            Log.d(TAG, String.format("%s, %s, %s\n %s, %s, %s %s\n %s, %s, %s %s\n %s, %s, %s %s\n %s",
                    PreferenceManager.getUserId(RideBookActivity.this), driverID, carsId
                    , mDateFormat.format(mStartDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mStartDateCalender.getTimeInMillis())
                    , edtSource.getQuery().toString()
                    , mSourceMarker.getPosition().latitude + "," + mSourceMarker.getPosition().longitude
                    , mDateFormat.format(mEndDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mEndDateCalender.getTimeInMillis())
                    , edtDestination1.getQuery().toString()
                    , mDestinationMarker.getPosition().latitude + "," + mDestinationMarker.getPosition().longitude
                    , driverDays, totalHours, mTimeStamp.format(new Date().getTime())
                    , driverPrice, mPlaceTypes));

            transactionDialog = new TransactionDialog(RideBookActivity.this, R.style.AppTheme, (Float.parseFloat(totalHours)
                    * Float.parseFloat(driverPrice)) + "");
            transactionDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorBlackTrans)));
            transactionDialog.show();


        }
    };

    private TransactionDialog.Transaction mTransactionComplete = new TransactionDialog.Transaction() {
        @Override
        public void onSuccess(@NonNull String price) {
            if (transactionDialog.isShowing())
                transactionDialog.dismiss();

            Log.d(TAG, String.format("\n%s, %s, %s\n %s, %s, %s %s\n %s, %s, %s %s\n %s, %s, %s %s\n %s",
                    PreferenceManager.getUserId(RideBookActivity.this), driverID, carsId
                    , mDateFormat.format(mStartDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mStartDateCalender.getTimeInMillis())
                    , edtSource.getQuery().toString()
                    , mSourceMarker.getPosition().latitude + "," + mSourceMarker.getPosition().longitude
                    , mDateFormat.format(mEndDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mEndDateCalender.getTimeInMillis())
                    , edtDestination1.getQuery().toString()
                    , mDestinationMarker.getPosition().latitude + "," + mDestinationMarker.getPosition().longitude
                    , driverDays, totalHours, mPlaceTypes, mTimeStamp.format(new Date().getTime())
                    , driverPrice));

            new AddRideTask().execute(PreferenceManager.getUserId(RideBookActivity.this), driverID, carsId
                    , mDateFormat.format(mStartDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mStartDateCalender.getTimeInMillis())
                    , edtSource.getQuery().toString()
                    , mSourceMarker.getPosition().latitude + "," + mSourceMarker.getPosition().longitude
                    , mDateFormat.format(mEndDateCalender.getTimeInMillis())
                    , mTimeFormat.format(mEndDateCalender.getTimeInMillis())
                    , edtDestination1.getQuery().toString()
                    , mDestinationMarker.getPosition().latitude + "," + mDestinationMarker.getPosition().longitude
                    , driverDays, totalHours, mPlaceTypes, mTimeStamp.format(new Date().getTime())
                    , price);
        }
    };

    private View.OnFocusChangeListener onFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.d(TAG, "onFocusChange: " + hasFocus);
            if (!hasFocus) {
                finish();
            }
        }
    };

    private androidx.appcompat.widget.SearchView.OnQueryTextListener onQuerySource
            = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mLocation = new Location("");

            LatLng latLng = getLocationFromAddress(query);

            if (latLng != null) {
                Log.d(TAG, "onEditorAction: Latlng not Null");
                mLocation.setLatitude(latLng.latitude);
                mLocation.setLongitude(latLng.longitude);
                String addr = getAddress(mLocation);
                edtSource.setQuery(addr, false);
                if (mDestinationMarker != null)
                    mSourceMarker.remove();
                mSourceMarker = mMap.addMarker(new MarkerOptions()
                        .title("Source")
                        .snippet(addr)
                        .position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mSourceMarker.setTag("Source");

                synchronized (this) {
                    if (isSearchedFirstTime) {
                        requestDirections();
                    }
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSourceMarker.getPosition(), 15));
            } else {
                Toast.makeText(RideBookActivity.this, "Could not find address, Try again", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private androidx.appcompat.widget.SearchView.OnQueryTextListener onQueryDest
            = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mDestination = new Location("");
            LatLng latLng = getLocationFromAddress(query);
            CloseKeyboard(edtDestination);
            CloseKeyboard(edtDestination1);
            CloseKeyboard(edtSource);

            if (latLng != null) {
                Log.d(TAG, "onEditorAction: Latlng not Null");
                mDestination.setLatitude(latLng.latitude);
                mDestination.setLongitude(latLng.longitude);
                String addr = getAddress(mDestination);
                edtDestination.setQuery(addr, false);
                edtDestination1.setQuery(addr, false);
                if (mDestinationMarker != null)
                    mDestinationMarker.remove();
                mDestinationMarker = mMap.addMarker(new MarkerOptions()
                        .title("Destination")
                        .snippet(addr)
                        .position(new LatLng(mDestination.getLatitude(), mDestination.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                edtSource.setOnQueryTextFocusChangeListener(onFocusChange);

                synchronized (this) {
                    isSearchedFirstTime = true;
                    requestDirections();
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDestinationMarker.getPosition(), 15));
            } else {
                Toast.makeText(RideBookActivity.this, "Could not find address, Try again", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private androidx.appcompat.widget.SearchView.OnQueryTextListener onQueryDest1
            = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mDestination = new Location("");
            LatLng latLng = getLocationFromAddress(query);
            if (latLng != null) {

                CloseKeyboard(edtDestination1);
                CloseKeyboard(edtSource);

                mDestination.setLatitude(latLng.latitude);
                mDestination.setLongitude(latLng.longitude);
                if (mDestinationMarker != null)
                    mDestinationMarker.remove();

                String addr = getAddress(mDestination);

                edtDestination.setQuery(getAddress(mDestination), false);
                edtDestination1.setQuery(getAddress(mDestination), false);

                mDestinationMarker = mMap.addMarker(new MarkerOptions()
                        .title("Destination")
                        .snippet(addr)
                        .position(new LatLng(mDestination.getLatitude(), mDestination.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mDestinationMarker.setTag("Destination");
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDestinationMarker.getPosition(), 15));

                if (mDestAdd.getVisibility() == View.VISIBLE)
                    mDestAdd.setVisibility(View.GONE);

                if (mDestPart.getVisibility() == View.GONE)
                    mDestPart.setVisibility(View.VISIBLE);

                synchronized (RideBookActivity.this) {
                    isSearchedFirstTime = true;
                    requestDirections();
                }

                Log.d(TAG, "onEditorAction: Latlng not Null");
            } else {
                Toast.makeText(RideBookActivity.this, "Could not find address, Try again", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };


    private GoogleMap.OnMarkerDragListener markerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            String str = (String) marker.getTag();
            LatLng latLng = marker.getPosition();

            if (str != null) {
                if (str.compareTo("Source") == 0) {

                } else if (str.compareTo("Destination") == 0) {
                    mDestination = new Location("");
                    mDestination.setLatitude(latLng.latitude);
                    mDestination.setLongitude(latLng.longitude);
                    String addr = getAddress(mDestination);
                    edtDestination1.setQuery(addr, false);

                    if (mDestinationMarker != null) {
                        mDestinationMarker.remove();
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                                .title("Destination")
                                .snippet(addr)
                                .position(new LatLng(mDestination.getLatitude(), mDestination.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        mDestinationMarker.setTag("Destination");

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDestinationMarker.getPosition(), 15));

                    }
                }
            }
        }
    };

    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off
        SettingsClient client = LocationServices.getSettingsClient(RideBookActivity.this);

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
                                .startResolutionForResult(RideBookActivity.this, REQUEST_CHECK_SETTINGS);
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

    private String getAddress(Location location) {
        String addr = "";
        Geocoder geocoder = new Geocoder(RideBookActivity.this);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException | IllegalArgumentException ioException) {
            // Catch network or other I/O problems.
            Log.e(TAG, ioException.getMessage());
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            addr = "No";
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            addr = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
        }
        return addr;
    }

    private void startLocationUpdates() {
        Log.d("DEBUG", "startLocationUpdates: ");
        if (ActivityCompat.checkSelfPermission(RideBookActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RideBookActivity.this
                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG", "startLocationUpdates: Permission Granted");
            startLastLocation();
        } else {
            ActivityCompat.requestPermissions(RideBookActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(RideBookActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RideBookActivity.this
                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.d("DEBUG", "requestPermissions: Requesting Permission");
        ActivityCompat.requestPermissions(RideBookActivity.this
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLastLocation() {
        Log.d("DEBUG", "startLastLocation: Starting Location Check");
        mFusedClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d("DEBUG", "onComplete: Received Last Location");
                        plotLocation(task.getResult());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DEBUG", "onFailure: " + e.getMessage());
                        Toast.makeText(RideBookActivity.this, "Something went wrong, could find you location."
                                , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void plotLocation(Location result) {
        if (result != null) {
            Log.d("Location", "Location Not Null");
            mLocation = result;
            address = getAddress(mLocation);
            edtSource.setQuery(address, false);

            CloseKeyboard(edtSource);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("Source");
            markerOptions.snippet(address);
            markerOptions.position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions.draggable(true);

            mSourceMarker = mMap.addMarker(markerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSourceMarker.getPosition(), 15));


        } else {
            Log.d("Location", "Location is Null");
        }
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
                ActivityCompat.requestPermissions(RideBookActivity.this
                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(RideBookActivity.this);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            } else if (address.size() == 0)
                return null;

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (latLng == null) {
            return null;
        } else
            return latLng;
    }

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBs6ifdtEVD4B2hn0yNtezAvIr0GQJsxRI";

    private void requestDirections() {

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage("Please wait.....");
            mProgressDialog.show();
        }

        if (directionsPolyline != null) {
            Log.d(TAG, "requestDirections: Polyline is Not Null");
            directionsPolyline.remove();
        } else
            Log.d(TAG, "requestDirections: Polyline is Null");

        Log.d(TAG, "requestDirections: URL : " + getUrl());

        StringRequest stringRequest = new StringRequest(getUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && response.length() > 0) {

                    Log.d(TAG, "onResponse: " + response);

                    try {
                        parseJSon(response);

                    } catch (JSONException e) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        e.printStackTrace();
                    }
                } else {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    Log.d(TAG, "Response is Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Log.d(TAG, "Error Volley + " + error.getMessage());
            }
        });

        MySingleton.getInstance(RideBookActivity.this).addToRequestQueue(stringRequest);
    }

    private String getUrl() {
        String link = "NA";
        try {
            String source = URLEncoder.encode(mLocation.getLatitude() + "," + mLocation.getLongitude(), "utf-8");
            String destination = URLEncoder.encode(mDestination.getLatitude() + "," + mDestination.getLongitude(), "utf-8");
            mFinalList = new ArrayList<LatLng>();
            link = DIRECTION_URL_API + "origin=" + source + "&destination=" + destination + "&departure_time=" + System.currentTimeMillis() + "&traffic_model=best_guess&key=" + GOOGLE_API_KEY;
            Log.d(TAG, "onMapReady: Link - " + link);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return link;
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        JSONObject jsonData = new JSONObject(data);

        if (jsonData.getString("status").compareTo("OK") == 0) {
            Log.d(TAG, "parseJSon: " + jsonData.getString("status"));
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
//        for (int i = 0; i < jsonRoutes.length(); i++)
//        {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(0);

            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");

            mDestinationDistance = jsonDistance.getString("text");

            //Retriving data from from steps
            JSONArray jsonsteps = jsonLeg.getJSONArray("steps");
            PolylineOptions po = new PolylineOptions();
            po.color(Color.BLACK);
            po.width(6);

            for (int s = 0; s < jsonsteps.length(); s++) {
                JSONObject jsonstep = jsonsteps.getJSONObject(s);

                //Taking Latlng as start and end objects
                JSONObject start = jsonstep.getJSONObject("start_location");
                JSONObject end = jsonstep.getJSONObject("end_location");

                LatLng src = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
                LatLng dest = new LatLng(end.getDouble("lat"), end.getDouble("lng"));


                //Taking points as object & decoding it
                JSONObject polyline_step = jsonstep.getJSONObject("polyline");
                String points = polyline_step.getString("points");
                List<LatLng> ll_points = decodePolyLine(points);
                for (int l = 0; l < ll_points.size(); l++) {
                    mFinalList.add(ll_points.get(l));
                    po.add(ll_points.get(l));
                }
            }

            directionsPolyline = mMap.addPolyline(po);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDestinationMarker.getPosition(), 12));
        } else {
            Location src = new Location("");
            src.setLatitude(mSourceMarker.getPosition().latitude);
            src.setLongitude(mSourceMarker.getPosition().longitude);

            Location dest = new Location("");
            dest.setLatitude(mDestinationMarker.getPosition().latitude);
            dest.setLongitude(mDestinationMarker.getPosition().longitude);
            mDestinationDistance = ((src.distanceTo(dest)) / 1000) + " km";

            Toast.makeText(this, "Could not find directions from source address", Toast.LENGTH_SHORT).show();
        }

        showCabView();
    }

    private void showCabView() {
        driverImage.setImageBitmap(getImage(driverImg));
        mDriverName.setText(driverName);

        mDriverPrice.setText(String.format(Locale.getDefault()
                , "%s %.2f", getResources().getString(R.string.currency)
                , calculatePrice(driverPrice)));

        mDriverDistance.setText(mDestinationDistance);
        if (driverRating.length() > 0) {
            mDriverRating.setRating(Float.parseFloat(driverRating));
        }


        Log.d(TAG, "showCabView: Distance : " + mDestinationDistance + " Price : " + driverPrice);

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (mDriverHolder.getVisibility() == View.GONE)
            mDriverHolder.setVisibility(View.VISIBLE);

        if (cabConfirmButton.getVisibility() == View.GONE)
            cabConfirmButton.setVisibility(View.VISIBLE);

    }

    private float calculatePrice(String driverPrice) {
        float price = Float.parseFloat(driverPrice);

        long difference = Math.abs(mStartDateCalender.getTimeInMillis() - mEndDateCalender.getTimeInMillis());
        long hours = TimeUnit.MILLISECONDS.toHours(difference);

        totalHours = String.valueOf(hours);

        Log.d(TAG, "calculatePrice:Difference " + difference + " Hours : " + hours);
        return price * hours;
    }

    private Bitmap getImage(String driverImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(driverImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class AddRideTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "RegisterTask";
        private ProgressDialog progressDialog;

        private AddRideTask() {
            progressDialog = new ProgressDialog(RideBookActivity.this);
            progressDialog.setCancelable(false);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UaddRide(params[0], params[1], params[2], params[3], params[4], params[5]
                        , params[6], params[7], params[8], params[9], params[10], params[11]
                        , params[12], params[13], params[14], params[15]);
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
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(RideBookActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {
                        Utility.ShowAlertDialog(RideBookActivity.this, "Already !"
                                , "You have already booked a ride for the selected date.", false);

                    } else if (StatusValue.compareTo("true") == 0) {

                        Toast.makeText(RideBookActivity.this, "Ride Booked Successfully"
                                , Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RideBookActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(RideBookActivity.this, LoginActivity.class));
                                finish();
                            }
                        }, 100);
                    }

                } catch (Exception e) {
                    Toast.makeText(RideBookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
}
