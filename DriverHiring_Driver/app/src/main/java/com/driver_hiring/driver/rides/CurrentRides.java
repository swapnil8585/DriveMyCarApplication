package com.driver_hiring.driver.rides;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.driver_hiring.driver.DriverService;
import com.driver_hiring.driver.R;
import com.driver_hiring.driver.helper.UserModel;
import com.driver_hiring.driver.helper.MySingleton;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.RestAPI;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class CurrentRides extends Fragment implements OnMapReadyCallback {
    private static final int TRIP_COMPLETED = 1002;
    private static final int BOARDED = 1003;
    private static final int CANCELLED = 1004;

    private static final String TAG = "CurrentTrip";
    public static final int DELAY_MILLIS = 5000;
    private static final int START_BACKGROUND = 11;
    private static final int STOP_BACKGROUND = 12;
    private String UID = "";

    /****************************
     **********Driver View*******
     ****************************/
    private ImageView cChangeTripView;
    private CardView mDriverHolder;
    private CircleImageView driverImage;
    private TextView mDriverName, mDriverDistance, mDriverPrice, mDriverArrival;
    private Button callDriver, confirmCab, cancelCab;
    private RatingBar mDriverRating;

    private GoogleMap mGoogleMap;
    private Marker mSource, mDestination, mDriverMarker;

    private UserModel mCurrentTripUser;
    private String CabID, tripSource, tripDest, sLatLng, dLatLng, Status, Date, Time, UName, UContact;
    private Location mSourceLocation, mDestinationLocation;

    private ProgressDialog progressDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private Polyline directionsPolyline, driverPolyline;
    private ArrayList<LatLng> mFinalList, mDriverDirections;

    private String mDestinationDistance = "";

    /******************************
     **********Driver Current******
     ******************************/
    private String CurrentLocation, CurrentStatus, isStatusSame, destLatLng, cabTripStatus;
    private UserHandler cUserHandler;
    private boolean isTripCompleted = false, isNavigation = false, isUserBoarded = false;
    private LocationHandler mDriverHandlers;
    private TempSteps tempStep;
    private ArrayList<TempSteps> steps;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        UID = PreferenceManager.getUserId(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Driver View
        View view = inflater.inflate(R.layout.current_trips, container, false);
        cChangeTripView = (ImageView) view.findViewById(R.id.currentTripView);
        mDriverHolder = view.findViewById(R.id.driver_view_holder);
        driverImage = view.findViewById(R.id.driver_image);
        mDriverName = view.findViewById(R.id.driver_name);
        mDriverRating = view.findViewById(R.id.driver_rating);
        mDriverDistance = view.findViewById(R.id.driver_trip_distance);
        mDriverPrice = view.findViewById(R.id.driver_trip_price);
        mDriverArrival = view.findViewById(R.id.driver_distance);
        callDriver = view.findViewById(R.id.driver_call);
        confirmCab = view.findViewById(R.id.confirm_trip);

//        mDriverHolder.setVisibility(View.GONE);
        mDriverRating.setVisibility(View.GONE);

        confirmCab.setVisibility(View.VISIBLE);
        cancelCab = view.findViewById(R.id.cancel_trip);
        cancelCab.setText("Cancel Cab");

        ChangeUserView(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mDriverHandlers = new LocationHandler(getActivity(), CurrentRides.this);
        cUserHandler = new UserHandler(getActivity(), CurrentRides.this);
        if (isNavigation) {
            isNavigation = false;
        }
        new GetDriverCurrent(true).execute(UID, dateFormat.format(new Date().getTime()));

    }

    @Override
    public void onStart() {
        super.onStart();

        confirmCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTripCompleted) {
                    //TODO - Call API Again
                    //string src, string rid, string did
                    Log.d(TAG, "onClick: " + "Finished" + "\n" + PreferenceManager.getDriverTrip(getActivity()) + "\n" + UID);
                    new ChangeTripStatus("Trip Finished").execute("Finished", PreferenceManager.getDriverTrip(getActivity()), UID);
                } else if (isUserBoarded) {
                    //TODO - Call Status Change API Again
                    Log.d(TAG, "onClick: " + "Boarded" + "\n" + PreferenceManager.getDriverTrip(getActivity()) + "\n" + UID);
                    new ChangeTripStatus("Trip Started").execute("Boarded", PreferenceManager.getDriverTrip(getActivity()), UID);
                } else if (isNavigation) {
                    Log.d(TAG, "onClick: " + "Boarded" + "\n" + PreferenceManager.getDriverTrip(getActivity()) + "\n" + UID);
                    String URL = "google.navigation:q="
                            + mDestinationLocation.getLatitude() + ", " + mDestinationLocation.getLongitude() + "";
                    Log.d(TAG, "onClick: URL : " + URL);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="
                            + mDestinationLocation.getLatitude() + ", " + mDestinationLocation.getLongitude() + "");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                }
            }
        });


        callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mCurrentTripUser.getContact()));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });


        cancelCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("Cancel Trip !")
                        .setMessage("Are you sure you want to Cancel this Trip?")
                        .setPositiveButton("Yes, Cancel Trip", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ChangeTripStatus("Trip Cancelled").execute("Cancelled", PreferenceManager.getDriverTrip(getActivity()), UID);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        cChangeTripView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                animateSlideUp();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateSlideDown();
                    }
                }, DELAY_MILLIS);
            }
        });

    }


    public class GetDriverCurrent extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetTrips";
        private boolean isBackground;

        private GetDriverCurrent(boolean checkBackground) {
            this.isBackground = checkBackground;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isBackground) {
                progressDialog.setMessage("Please Wait");
                progressDialog.setCancelable(false);
                progressDialog.show();

                if (cUserHandler != null)
                    cUserHandler.sendEmptyMessage(STOP_BACKGROUND);
            }


        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.DgetCurrent(params[0], params[1]);
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
                if (mDriverHandlers != null) {
                    mDriverHandlers.isCheckComplete = true;
                }
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        if (isBackground) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            PreferenceManager.saveTripId(getActivity(), "");

                            Utility.ShowAlertDialog(getActivity(), "No Current Booking"
                                    , "You don't have any trips", false);

                            if (mDriverHandlers != null) {
                                mDriverHandlers.isCheckComplete = true;
                                mDriverHandlers.sendEmptyMessage(START_BACKGROUND);
                            }


                        } else {
                            Log.d(TAG, "onPostExecute: Trip Status No");
                        }

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        // 0   1   2   3    4     5      6      7      8      9     10   11    12    13    14    15
                        //rid,uid,did,cid,source,dest,slatlng,dlatlng,status,price,date,time,dname,dcont,dphoto,dloc
                        //rid,uid,did,cid,source,dest,srclaltlng,destlatlng,status,price,date,time,uname,ucont

//                        String driverId, String name, String contact, String photo
//                                , String carModel, String carNo, String location, String peekCost, String norCost, String Rating
//                                , String distance, String TripId

                        mCurrentTripUser =
                                new UserModel(
                                        js.getString("data1"), js.getString("data12")
                                        , js.getString("data13"), ""
                                        , "", "", ""
                                        , js.getString("data9"), "", "", ""
                                        , js.getString("data0"));

                        CabID = js.getString("data3");
                        tripSource = js.getString("data4");
                        tripDest = js.getString("data5");
                        Status = js.getString("data8");
                        sLatLng = js.getString("data6");
                        dLatLng = js.getString("data7");
                        Date = js.getString("data10");
                        Time = js.getString("data11");

                        if (Status.compareTo("Cancelled") != 0) {

                            if (Status.compareTo("Boarded") == 0) {
                                cancelCab.setVisibility(View.GONE);
                                confirmCab.setVisibility(View.GONE);
                            }

                            if (getActivity() != null)
                                PreferenceManager.saveTripId(getActivity(), js.getString("data0"));
                            UpdateDriverView();
                        }
                    } else {
                        if (isBackground) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        mDriverHandlers.isCheckComplete = true;

                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    if (isBackground) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                    mDriverHandlers.isCheckComplete = true;
                    e.printStackTrace();
                }

            }

        }
    }

    private void UpdateDriverView() {
        String[] src = sLatLng.split(",");
        String[] dest = dLatLng.split(",");
//        String[] driver = mCurrentTripUser.getCurrentLocation().split(",");

        LatLng mSrc = new LatLng(Double.parseDouble(src[0]), Double.parseDouble(src[1]));
        LatLng mDest = new LatLng(Double.parseDouble(dest[0]), Double.parseDouble(dest[1]));
//        LatLng mDriver = new LatLng(Double.parseDouble(driver[0]), Double.parseDouble(driver[1]));

        mSourceLocation = new Location("");
        mSourceLocation.setLatitude(mSrc.latitude);
        mSourceLocation.setLongitude(mSrc.longitude);

        mDestinationLocation = new Location("");
        mDestinationLocation.setLatitude(mDest.latitude);
        mDestinationLocation.setLongitude(mDest.longitude);


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mSrc);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title("Source");

        mSource = mGoogleMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions();
        markerOptions.position(mDest);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions.title("Destination");

        mDestination = mGoogleMap.addMarker(markerOptions);

        //        markerOptions = new MarkerOptions();
//        markerOptions.position(mDriver);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//        markerOptions.title("Car");
//        mDriverMarker = mGoogleMap.addMarker(markerOptions);

        mGoogleMap.addCircle(new CircleOptions()
                .center(mSrc)
                .strokeWidth(2)
                .strokeColor(Color.BLACK)
                .fillColor(0X85ffffff)
                .radius(300));


        mDriverName.setText(mCurrentTripUser.getName());
//        getDriverDistance(mSourceLocation);
        float distance = getDistance(mSourceLocation, mDestinationLocation);
        DecimalFormat decimalFormat = new DecimalFormat("#.0#");
        mDriverDistance.setText(String.format(Locale.getDefault(), "%s km", decimalFormat.format(distance)));
        mDriverArrival.setText(getTime(distance) + "");
        mDriverPrice.setText(String.format("%s %s"
                , getResources().getString(R.string.currency), mCurrentTripUser.getPeekCost()));

//        mDriverRating.setVisibility(View.INVISIBLE);
//        mDriverRating.setState(Float.parseFloat(mCurrentTripUser.getName()));

        progressDialog.setMessage("Fetching Directions");

        cUserHandler.sendEmptyMessage(START_BACKGROUND);

        mDriverHandlers.isTripViewShown = false;
        mDriverHandlers.finishedTrip = false;
        mDriverHandlers.isCheckComplete = true;
        mDriverHandlers.sendEmptyMessage(STOP_BACKGROUND);

        mDriverHandlers.sendEmptyMessage(START_DRIVER_LOCATION_PLOTTING);

        if (Status.compareTo("Booked") == 0) {
            //Directions with Driver Location
            if (DriverService.mLocation != null) {
                requestDirections(getSrcDestURl(DriverService.mLocation, mSourceLocation), true);
            } else {
                Log.d(TAG, "Driver Location Null");
            }
        } else if (Status.compareTo("Boarded") == 0) {
            //Directions without Driver Location
            requestDirections(getSrcDestURl(mSourceLocation, mDestinationLocation), false);
        }


//        if (progressDialog.isShowing())
//            progressDialog.dismiss();
    }

    private String getUrlWithDriver(Location mLocation, Location mSourceLocation, Location mDestinationLocation) {
        String link = "NA";
        try {
            String source = URLEncoder.encode(mLocation.getLatitude() + "," + mLocation.getLongitude(), "utf-8");
            String destination = URLEncoder.encode(mDestinationLocation.getLatitude() + "," + mDestinationLocation.getLongitude(), "utf-8");
            String waypoints = URLEncoder.encode(mSourceLocation.getLatitude() + "," + mSourceLocation.getLongitude(), "utf-8");
            mFinalList = new ArrayList<LatLng>();
            link = DIRECTION_URL_API + "origin=" + source + "&destination=" + destination + "&waypoints=" + waypoints + "&departure_time=" + System.currentTimeMillis() + "&traffic_model=best_guess&key=" + GOOGLE_API_KEY;
            Log.d(TAG, "getSrcDestURl: Link - " + link);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return link;
    }

    /**************************************
     ********** Utility Functions *********
     **************************************/

    private void ChangeUserView(boolean isShown) {
        if (isShown) {

            if (cChangeTripView.getVisibility() == View.GONE) {
                cChangeTripView.setVisibility(View.VISIBLE);
            }

            if (mDriverHolder.getVisibility() == View.GONE) {
                mDriverHolder.setVisibility(View.VISIBLE);
            }
        } else {

            if (cChangeTripView.getVisibility() == View.VISIBLE) {
                cChangeTripView.setVisibility(View.GONE);
            }

            if (mDriverHolder.getVisibility() == View.VISIBLE) {
                mDriverHolder.setVisibility(View.GONE);
            }

        }
    }

    private void animateSlideUp() {

        try {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cChangeTripView.setRotation(180);
                    mDriverHolder.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mDriverHolder.startAnimation(animation);
        } catch (Exception exp) {
            exp.printStackTrace();
        }


    }

    private void animateSlideDown() {
        try {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cChangeTripView.setRotation(0);
                    mDriverHolder.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mDriverHolder.startAnimation(animation);
        } catch (Exception exp) {
            exp.printStackTrace();
        }


    }

    public static int getDistance(Location src, Location dest) {
        return (int) (src.distanceTo(dest) / 1000);
    }

    private int getTime(float distance) {
        if (distance == 0 || distance == 1) {
            return 2;
        } else
            return (int) distance * 4;
    }

    public static Bitmap getImageBitmap(String Simage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(Simage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static class UserHandler extends Handler {
        private Context appContext;
        private CurrentRides mMapFragment;
        private static TimerTask timerTask;
        private static Timer timer;
        private boolean isTripCheck = true;
        private AlertDialog alertTripCancel;

        public UserHandler(@NonNull Context context, CurrentRides mapsFragment) {
            this.appContext = context;
            mMapFragment = mapsFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRIP_COMPLETED:
                    //TODO - Stop Timer and Show Feedback Dialog,
                    //TODO - On Dialog Submit,Clear Map and Call GetTrips
                    mMapFragment.isTripCompleted = true;
                    mMapFragment.confirmCab.setText("Start Trip");

                    stopBackgroundCheck();

                    clearAllView();

                    if (mMapFragment.mDriverHolder.getVisibility() == View.VISIBLE) {
                        mMapFragment.mDriverHolder.setVisibility(View.GONE);
                    }


                    if (mMapFragment.cChangeTripView.getVisibility() == View.VISIBLE) {
                        mMapFragment.cChangeTripView.setVisibility(View.GONE);
                    }

                    break;
                case BOARDED:

                    mMapFragment.isUserBoarded = true;
                    mMapFragment.confirmCab.setText("Start Trip");

                    mMapFragment.ChangeUserView(true);

                    break;

                case CANCELLED:

                    stopBackgroundCheck();

                    mMapFragment.ChangeUserView(false);

                    PreferenceManager.saveTripId(appContext, "");

                    clearAllView();

                    AlertDialog.Builder builder = new AlertDialog.Builder(appContext)
                            .setTitle("Trip Cancelled")
                            .setMessage("This trip has been Cancelled")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mMapFragment.new GetDriverCurrent(true).execute(mMapFragment.UID, mMapFragment.dateFormat.format(new Date().getTime()));
                                }
                            });
                    alertTripCancel = builder.create();
                    if (!alertTripCancel.isShowing())
                        alertTripCancel.show();

                    break;
                case START_BACKGROUND:
                    Log.d(TAG, "handleMessage: Service Started");
                    startBackgroundCheck();
                    break;
                case STOP_BACKGROUND:
                    Log.d(TAG, "handleMessage: Service Stopped");
                    stopBackgroundCheck();
                    break;
            }
        }

        private void clearAllView() {
            mMapFragment.mGoogleMap.clear();
        }

        private void stopBackgroundCheck() {
            if (timer != null && timerTask != null) {
                timer.cancel();
                timer.purge();
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
                                if (isTripCheck) {
                                    String tripId = PreferenceManager.getDriverTrip(appContext);
                                    if (tripId.compareTo("") != 0) {
                                        Log.d(TAG, "TripID : " + tripId);
                                        isTripCheck = false;
                                        mMapFragment.new CheckTripStatus().execute(tripId);
                                    } else {
                                        Log.d(TAG, "TripID : No Trip ID");
                                    }
                                }
                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                        }
                    });
                }
            };

            timer = new Timer("UserHandler");
            timer.schedule(timerTask, 0, 3000);
        }
    }

    private class CheckTripStatus extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetDriver";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.DgetTripStatus(params[0]);
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

            Log.d(TAG, String.format("CheckTripStatus: %s", s));

            if (Utility.checkConnection(s)) {
                cUserHandler.isTripCheck = true;
                Log.d(TAG, "onPostExecute: No Internet");
//                Pair<String, String> pair = Utility.GetErrorMessage(s);
//                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        //CurrentStatus
                        CurrentStatus = js.getString("data0");
                        cUserHandler.isTripCheck = true;
                        if (CurrentStatus.compareTo("Cancelled") == 0) {
                            cUserHandler.sendEmptyMessage(CANCELLED);
                        } else if (CurrentStatus.compareTo("Boarded") == 0) {
                            if (!isNavigation) {
                                isUserBoarded = false;
                                isNavigation = true;
                                Log.d(TAG, "onPostExecute: Boarded Again");
                                confirmCab.setText("Start Navigation");
                                confirmCab.setVisibility(View.VISIBLE);
                            }
                        }


                    } else if (StatusValue.compareTo("no") == 0) {
                        cUserHandler.isTripCheck = true;
                        Log.d(TAG, "onPostExecute: No");
                    } else {
                        cUserHandler.isTripCheck = true;
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private class ChangeTripStatus extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "ChangeStatus";
        private String Message = "";

        public ChangeTripStatus(String message) {
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
            RestAPI api = new RestAPI();
            try {
                //string src, string rid, string did
                JSONObject json = api.DchangeTripStatus(params[0], params[1], params[2]);
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
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {

                        if (Message.compareTo("Trip Started") == 0) {
                            confirmCab.setVisibility(View.GONE);
                            cancelCab.setVisibility(View.GONE);
                        } else {
                            ChangeUserView(false);
                            mGoogleMap.clear();
                            PreferenceManager.saveTripId(getActivity(), "");
                            new GetDriverCurrent(true).execute(UID, dateFormat.format(new Date().getTime()));
                        }

                        Toast.makeText(getActivity(), Message, Toast.LENGTH_SHORT).show();

                    } else if (StatusValue.compareTo("false") == 0) {
                        Utility.ShowAlertDialog(getActivity(), "Oops !", "Something Went Wrong" +
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


    /**************************************
     ******Location Plotting Handler*******
     **************************************/

    //Handler Constants
    private static final int START_DRIVER_LOCATION_PLOTTING = 100;
    private static final int STOP_DRIVER_LOCATION_PLOTTING = 101;

    private static class LocationHandler extends Handler {
        private Context context;
        private CurrentRides currentRides;
        private static Timer timer, timerPlot;
        private static TimerTask timerTask, timerTaskPlot;
        private static Handler handler, handlerPlot;
        private Marker mDriverMarker;
        private boolean isTripViewShown = false, finishedTrip = false, isCheckComplete = true;

        private LocationHandler(@NonNull Context appContext, @NonNull CurrentRides trips) {
            this.context = appContext;
            this.currentRides = trips;
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
                case START_DRIVER_LOCATION_PLOTTING:
                    startPlotting();
                    break;
                case STOP_DRIVER_LOCATION_PLOTTING:
                    stopPlotting();
                    break;
            }

        }

        private void startPlotting() {
            timerPlot = new Timer();
            timerTaskPlot = new TimerTask() {
                @Override
                public void run() {
                    handlerPlot = new Handler(Looper.getMainLooper());
                    handlerPlot.post(new Runnable() {
                        @Override
                        public void run() {
                            Location location = DriverService.mLocation;
                            if (location != null) {
                                LatLng mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                if (mDriverMarker != null) {
                                    mDriverMarker.remove();
                                }

                                if (!isTripViewShown) {
                                    Log.d(TAG, "Checking Driver Location < 500 : " + DriverService.mLocation.distanceTo(currentRides.mSourceLocation));
                                    if (DriverService.mLocation.distanceTo(currentRides.mSourceLocation) < 500) {

                                        Log.d(TAG, "Checking DriverLocation Less Than 500");

                                        isTripViewShown = true;
                                        currentRides.ChangeUserView(true);
                                        currentRides.isUserBoarded = true;
                                        currentRides.confirmCab.setText("Start Trip");
                                        currentRides.cChangeTripView.performClick();
                                    }
                                } else {
                                    Log.d(TAG, "Checking Driver Closed");
                                }

                                if (!finishedTrip) {
                                    if (DriverService.mLocation.distanceTo(currentRides.mDestinationLocation) < 500) {

                                        Log.d(TAG, "Checking DriverLocation Less Than 500");

                                        finishedTrip = true;
                                        currentRides.ChangeUserView(true);
                                        currentRides.isTripCompleted = true;
                                        if (currentRides.cancelCab.getVisibility() == View.VISIBLE)
                                            currentRides.cancelCab.setVisibility(View.GONE);

                                        if (currentRides.confirmCab.getVisibility() == View.GONE)
                                            currentRides.confirmCab.setVisibility(View.VISIBLE);

                                        currentRides.confirmCab.setText("Finish Trip");
                                        currentRides.cChangeTripView.performClick();
                                    }
                                }

                                mDriverMarker = currentRides.mGoogleMap
                                        .addMarker(
                                                new MarkerOptions()
                                                        .position(mCurrentLatLng)
                                                        .title("Driver")
                                                        .icon(BitmapDescriptorFactory
                                                                .fromResource(R.drawable.car))
                                        );

                                currentRides.mGoogleMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(mDriverMarker.getPosition(), 14));
                            } else {
                                Log.d(TAG, "run: Location is Null");
                            }
                        }
                    });
                }
            };
            timerPlot.schedule(timerTaskPlot, 0, 2000);
        }

        private void stopPlotting() {
            if (timerPlot != null && timerTaskPlot != null) {
                timerPlot.cancel();
                timerTaskPlot.cancel();

                timerPlot = null;
                timerTaskPlot = null;
            }
        }

        private void stopBackgroundCheck() {
            if (timer != null && timerTask != null) {
                Log.d(TAG, "stopBackgroundCheck: Current Check Stop");

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
                                if (isCheckComplete) {
                                    isCheckComplete = false;
                                    currentRides.new GetDriverCurrent(false).execute(currentRides.UID
                                            , currentRides.dateFormat.format(new Date().getTime()));
                                }


                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                        }
                    });
                }
            };

            timer = new Timer("LocationHandler");
            timer.schedule(timerTask, 0, 3000);
        }
    }

    /**************************************
     ********* Directions API Call ********
     **************************************/

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBs6ifdtEVD4B2hn0yNtezAvIr0GQJsxRI";

    private void requestDirections(String url, final boolean isDriver) {


        if (directionsPolyline != null) {
            Log.d(TAG, "requestDirections: Polyline is Not Null");
            directionsPolyline.remove();
        } else
            Log.d(TAG, "requestDirections: Polyline is Null");

        Log.d(TAG, "requestDirections: URL : " + url);

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && response.length() > 0) {

//                    Log.d(TAG, "onResponse: " + response);
                    logLargeString(response);

                    try {
                        if (isDriver)
                            parseDriverDirections(response);
                        else
                            parseJSon(response);

                    } catch (JSONException e) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        e.printStackTrace();
                    }
                } else {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    Log.d(TAG, "Response is Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.d(TAG, "Error Volley + " + error.getMessage());
            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void parseDriverDirections(String response) throws JSONException {
        if (response == null)
            return;
        JSONObject jsonData = new JSONObject(response);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);
        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");

        mDestinationDistance = jsonDistance.getString("text");

        //Retriving data from from steps
        JSONArray jsonsteps = jsonLeg.getJSONArray("steps");
        PolylineOptions po = new PolylineOptions();
        po.color(Color.BLUE);
        po.width(6);

        for (int s = 0; s < jsonsteps.length(); s++) {
            JSONObject jsonstep = jsonsteps.getJSONObject(s);

            //Taking points as object & decoding it
            JSONObject polyline_step = jsonstep.getJSONObject("polyline");
            String points = polyline_step.getString("points");
            List<LatLng> ll_points = decodePolyLine(points);
            for (int l = 0; l < ll_points.size(); l++) {
                mFinalList.add(ll_points.get(l));
                po.add(ll_points.get(l));
            }
        }

        driverPolyline = mGoogleMap.addPolyline(po);
        requestDirections(getSrcDestURl(mSourceLocation, mDestinationLocation), false);

    }

    public void logLargeString(String str) {
        if (str.length() > 3000) {
            Log.i("RESPONSE", str.substring(0, 3000));
            logLargeString(str.substring(3000));
        } else {
            Log.i("RESPONSE", str); // continuation
        }
    }

    private String getSrcDestURl(Location mSourceLocation, Location mDestinationLocation) {
        String link = "NA";
        try {
            String source = URLEncoder.encode(mSourceLocation.getLatitude() + "," + mSourceLocation.getLongitude(), "utf-8");
            String destination = URLEncoder.encode(mDestinationLocation.getLatitude() + "," + mDestinationLocation.getLongitude(), "utf-8");
            mFinalList = new ArrayList<LatLng>();
            link = DIRECTION_URL_API + "origin=" + source + "&destination=" + destination + "&departure_time=" + System.currentTimeMillis() + "&traffic_model=best_guess&key=" + GOOGLE_API_KEY;
            Log.d(TAG, "getSrcDestURl: Link - " + link);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return link;
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);

        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");

//        JSONArray jsonSteps;
//        if (jsonLeg.has("steps")) {
//            jsonSteps = jsonLeg.getJSONArray("steps");
////            parseJsonSteps(jsonSteps);
//        }

        mDestinationDistance = jsonDistance.getString("text");

        //Retriving data from from steps
        JSONArray jsonsteps = jsonLeg.getJSONArray("steps");
        PolylineOptions po = new PolylineOptions();
        po.color(Color.BLACK);
        po.width(6);

        for (int s = 0; s < jsonsteps.length(); s++) {
            JSONObject jsonstep = jsonsteps.getJSONObject(s);

            //Taking points as object & decoding it
            JSONObject polyline_step = jsonstep.getJSONObject("polyline");
            String points = polyline_step.getString("points");
            List<LatLng> ll_points = decodePolyLine(points);
            for (int l = 0; l < ll_points.size(); l++) {
                mFinalList.add(ll_points.get(l));
                po.add(ll_points.get(l));
            }
        }

        directionsPolyline = mGoogleMap.addPolyline(po);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSource.getPosition(), 14));

        mDriverHandlers.isCheckComplete = true;
        mDriverHandlers.sendEmptyMessage(STOP_BACKGROUND);

        if (progressDialog.isShowing())
            progressDialog.dismiss();

    }

    private void parseJsonSteps(JSONArray jsonSteps) {
        String DURA = "duration", TEXT = "text", VALUE = "value", DIST = "distance", END_LOC = "end_location", MANEUV = "maneuver", START_LOC = "start_location", LAT = "lat", LNG = "lng", POINTS = "points", HTML_INSTRUC = "html_instructions", POLY = "polyline";
        try {
            for (int i = 0; i < jsonSteps.length(); i++) {
                JSONObject eachStep = jsonSteps.getJSONObject(i);
                JSONObject dist = eachStep.getJSONObject(DIST);
                tempStep = new TempSteps();
                tempStep.setHtmlInstruction(Html.fromHtml(eachStep.getString(HTML_INSTRUC)).toString());
                tempStep.setDistance(dist.getString(TEXT));

                JSONObject end = eachStep.getJSONObject(END_LOC);

                Location tempEnd = new Location("");
                tempEnd.setLongitude(end.getDouble(LNG));
                tempEnd.setLatitude(end.getDouble(LAT));
                tempStep.setEndLocation(tempEnd);
                JSONObject start = eachStep.getJSONObject(START_LOC);
                Location temp = new Location("");
                temp.setLongitude(start.getDouble(LNG));
                temp.setLatitude(start.getDouble(LAT));
                tempStep.setStartLocation(temp);
                markEachTurn(temp);

                JSONObject poly = eachStep.getJSONObject(POLY);
                if (eachStep.has(MANEUV)) {
                    tempStep.setManeuver(eachStep.getString(MANEUV));
                } else {
                    tempStep.setManeuver(Html.fromHtml(eachStep.getString(HTML_INSTRUC)).toString());
                }
                //Log.i(TAG, "parseJson: " + tempStep.getHtml_instruc()());
                tempStep.setPolyline(poly.getString(POINTS));
//                latlongList.addll(decodePolyLine(poly.getString(POINTS)));
                steps.add(tempStep);
            }

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void markEachTurn(Location endloc) {
        mGoogleMap.addCircle(new CircleOptions().radius(1)
                .strokeColor(getResources()
                        .getColor(android.R.color.transparent))
                .center(new LatLng(endloc.getLatitude(), endloc.getLongitude()))
                .clickable(true).fillColor(Color.BLACK));
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
        if (mDriverHandlers != null) {
            mDriverHandlers.sendEmptyMessage(STOP_DRIVER_LOCATION_PLOTTING);
        }
        super.onDestroy();
    }

    private class TempSteps {
        private String htmlInstruction, distance, maneuver, polyline;
        private Location endLocation, startLocation;

        public String getHtmlInstruction() {
            return htmlInstruction;
        }

        public void setHtmlInstruction(String htmlInstruction) {
            this.htmlInstruction = htmlInstruction;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public Location getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(Location endLocation) {
            this.endLocation = endLocation;
        }

        public Location getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
        }

        public String getManeuver() {
            return maneuver;
        }

        public void setManeuver(String maneuver) {
            this.maneuver = maneuver;
        }

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(String polyline) {
            this.polyline = polyline;
        }
    }
}
