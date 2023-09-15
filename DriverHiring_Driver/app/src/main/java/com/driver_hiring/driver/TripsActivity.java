package com.driver_hiring.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.driver_hiring.driver.background.RideLocationUpdate;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.models.RideModel;
import com.driver_hiring.driver.rides.UpcomingRides;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Date;

public class TripsActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    public static final String RIDE_DETAILS = "Ride_Details";
    private String mRideString;
    private RideModel mRideModel;
    private EditText mEditName, mEditSrcAddress, mEditDestAddress, mEditType, mSDate, mEDate,
            mEditDays, mEditHrs;
    private RatingBar mUserRating;
    private AppCompatButton mCabDetails, mRideStart, mRideFinish;

    private GoogleMap mGoogleMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        getSupportActionBar().setTitle("Ride Details");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCabDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripsActivity.this, CabDetailsActivity.class);
                intent.putExtra(CabDetailsActivity.CAB_ID, mRideModel.getCabID());
                startActivity(intent);
            }
        });

        mRideStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChangeStatus("Ride has been started").execute(mRideModel.getRideID(), "Started", "Driver");
            }
        });

        mRideFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChangeStatus("Ride has been finished").execute(mRideModel.getRideID(), "Finished", "Driver");
            }
        });
    }

    private void initViews() {
        mEditName = findViewById(R.id.rides_name);
        mEditSrcAddress = findViewById(R.id.rides_src);
        mEditDestAddress = findViewById(R.id.rides_dest);
        mEditType = findViewById(R.id.rides_type);
        mSDate = findViewById(R.id.rides_s_date);
        mEDate = findViewById(R.id.rides_e_date);
        mEditDays = findViewById(R.id.rides_tot_days);
        mEditHrs = findViewById(R.id.rides_tot_hrs);
        mUserRating = findViewById(R.id.rides_cab_rating);
        mUserRating.setClickable(false);

        mCabDetails = findViewById(R.id.rides_cab_details);
        mRideStart = findViewById(R.id.rides_cab_start);
        mRideFinish = findViewById(R.id.rides_cab_finish);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setValues() {
        Bundle bundle;
        if (getIntent().getExtras() != null) {
            bundle = getIntent().getBundleExtra(RIDE_DETAILS);
            mRideString = bundle.getString(RIDE_DETAILS);
            Log.d("MAIN", "onCreate: " + mRideString);
            mRideModel = (RideModel) new Gson().fromJson(mRideString, RideModel.class);
        }

        mEditName.setText(mRideModel.getUserName());
        mEditSrcAddress.setText(mRideModel.getStartAddress());
        mEditDestAddress.setText(mRideModel.getEndAddress());
        mEditType.setText(mRideModel.getPlacetype());
        mSDate.setText(mRideModel.getStartDate());
        mEDate.setText(mRideModel.getEndDate());
        mEditDays.setText(mRideModel.getTotalDays());
        mEditHrs.setText(mRideModel.getTotalhHours());
        if (mRideModel.getRatings().length() > 0)
            mUserRating.setRating(Float.parseFloat(mRideModel.getRatings()));

        String[] srcLatlng = mRideModel.getSourceLatLng().split(",");

        mGoogleMaps.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .position(new LatLng(Double.parseDouble(srcLatlng[0]), (Double.parseDouble(srcLatlng[0]))))
                .snippet("Source"));

        String[] destLatlng = mRideModel.getEndLatLng().split(",");

        mGoogleMaps.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(new LatLng(Double.parseDouble(destLatlng[0]), (Double.parseDouble(destLatlng[0]))))
                .snippet("Destination"));

        mGoogleMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(destLatlng[0]), (Double.parseDouble(destLatlng[0])))
                , 11));

        if (mRideModel.getStatus().contains("Booked")) {
            mRideStart.setVisibility(View.VISIBLE);
        }

        if (mRideModel.getStatus().contains("Started")) {
            mRideFinish.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMaps = googleMap;

        setValues();
    }

    private class ChangeStatus extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "ChangeStatus";
        private String Message = "";
        private ProgressDialog progressDialog;

        public ChangeStatus(@NonNull String message) {
            progressDialog = new ProgressDialog(TripsActivity.this);
            this.Message = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null) {
                progressDialog.setMessage("Please Wait");
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                //string rid, string did
                JSONObject json = api.DchangeStatus(params[0], params[1], params[2]);
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
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            Log.d(TAG, String.format("ChangeTrip: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(TripsActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {


                        Toast.makeText(TripsActivity.this, Message, Toast.LENGTH_SHORT).show();

                        if (Message.contains("started")) {
                            Log.d(TAG, "onPostExecute: Started");

                            PreferenceManager.setRideStarted(TripsActivity.this
                                    , mRideModel.getRideID(), true);

                            mRideFinish.setVisibility(View.VISIBLE);
                            mRideStart.setVisibility(View.GONE);

                            stopService(new Intent(TripsActivity.this, RideLocationUpdate.class));
                            startService(new Intent(TripsActivity.this, RideLocationUpdate.class));

                        } else if (Message.contains("finished")) {
                            Log.d(TAG, "onPostExecute: Started");

                            PreferenceManager.setRideStarted(TripsActivity.this
                                    , "", false);

                            stopService(new Intent(TripsActivity.this, RideLocationUpdate.class));

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 200);
                        }

                    } else if (StatusValue.compareTo("false") == 0) {
                        Utility.ShowAlertDialog(TripsActivity.this, "Oops !", "Something Went Wrong" +
                                ", Please Try Again", false);
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
