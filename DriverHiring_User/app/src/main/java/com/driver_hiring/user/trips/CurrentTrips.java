package com.driver_hiring.user.trips;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.driver_hiring.user.CabNavigationActivity;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.helper.DriverModel;
import com.driver_hiring.user.helper.MySingleton;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.RestAPI;
import com.driver_hiring.user.webservices.Utility;
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
import java.lang.ref.WeakReference;
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

import static com.driver_hiring.user.RideBookActivity.START_BACKGROUND;
import static com.driver_hiring.user.RideBookActivity.STOP_BACKGROUND;

public class CurrentTrips extends Fragment implements OnMapReadyCallback {
    private static final int TRIP_COMPLETED = 1002;
    private static final int BOARDED = 1003;
    private static final int CANCELLED = 1004;

    private static final String TAG = "CurrentTrip";
    public static final int DELAY_MILLIS = 10000;
    private String UID = "";

    /****************************
     **********Driver View*******
     ****************************/
    private ImageView cChangeTripView;
    private CardView mDriverHolder;
    private CircleImageView driverImage;
    private TextView mDriverName, mDriverDistance, mDriverPrice, mDriverArrival;
    private RatingBar mDriverRating;

    private GoogleMap mGoogleMap;
    private Marker mSource, mDestination, mDriverMarker;

    private DriverModel mCurrentDriver;
    private String CabID, tripSource, tripDest, sLatLng, dLatLng, Status, Date, Time;
    private Location mSourceLocation, mDestinationLocation;

    private ProgressDialog progressDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private Polyline directionsPolyline;
    private ArrayList<LatLng> mFinalList;

    private String mDestinationDistance = "";

    /******************************
     **********Driver Current******
     ******************************/
    private String CurrentLocation, CurrentStatus, isStatusSame, destLatLng, cabTripStatus;
    private DriverHandler cDriverHandler;

    /*******************************
     ******** VIEWS FEEDBACK********
     *******************************/

    private ConstraintLayout mFeedLayout;
    private TextView feedMessage, feedDriverName, feedSkip;
    private EditText edtFeedback;
    private ImageView feedDriverImage;
    private RatingBar feedRating;
    private Button feedSubmit;
    private boolean isTripFinished = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
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
//        mDriverArrival = view.findViewById(R.id.driver_distance);
//        callDriver = view.findViewById(R.id.driver_call);
//        confirmCab = view.findViewById(R.id.confirm_trip);

        mDriverHolder.setVisibility(View.GONE);

//        confirmCab.setVisibility(View.GONE);
//        cancelCab = view.findViewById(R.id.cancel_trip);
//        cancelCab.setText("Cancel Cab");


        //FeedbackView
        mFeedLayout = view.findViewById(R.id.feedbackHolder);
        feedMessage = view.findViewById(R.id.feedbackMessage);
        feedDriverImage = view.findViewById(R.id.feedbackDriverImage);
        feedDriverName = view.findViewById(R.id.feedbackDriverName);
        feedRating = view.findViewById(R.id.feedbackRating);
        edtFeedback = view.findViewById(R.id.feedbackText);
        feedSubmit = view.findViewById(R.id.btnFeedSubmit);
        feedSkip = view.findViewById(R.id.btnFeedSkip);

        feedRating.setIsIndicator(false);

        if (cChangeTripView.getVisibility() == View.VISIBLE) {
            cChangeTripView.setVisibility(View.GONE);
        }


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
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (isTripFinished) {
            mGoogleMap.clear();
        }

        new GetCurrentCabs().execute(UID, dateFormat.format(new Date().getTime()));

    }

    @Override
    public void onStart() {
        super.onStart();


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

        feedSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //string tid,string uid,string did,string rating,string review
                String feed = edtFeedback.getText().length() > 0 ? edtFeedback.getText().toString() : "NA";
                Log.d(TAG, String.format("onClickFeed: Rating : %s, Feed : %s", feedRating.getRating() + "", feed));
                new AddFeedback().execute(mCurrentDriver.getTripId(), UID, mCurrentDriver.getDriverId()
                        , feedRating.getRating() + "", feed);
            }
        });

        feedSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CabNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();

            }
        });

    }

    private synchronized void animateSlideUp() {
        if (getActivity() != null) {
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
        }

    }

    private synchronized void animateSlideDown() {
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
        } catch (NullPointerException nullPointer) {
            Log.d(TAG, "animateSlideDown: No Context Found");
            nullPointer.printStackTrace();
        }


    }

    public class GetCurrentCabs extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";

        private GetCurrentCabs() {
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
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getCurrent(params[0], params[1]);
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
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();


                        Utility.ShowAlertDialog(getActivity(), "No Current Booking"
                                , "You don't have any trips", false);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        // 0   1   2   3    4     5      6      7      8      9     10   11    12    13    14    15
                        //tid,uid,did,cid,source,dest,slatlng,dlatlng,status,price,date,time,dname,dcont,dphoto,dloc

                        mCurrentDriver =
                                new DriverModel(
                                        js.getString("data2"), js.getString("data12")
                                        , js.getString("data13"), js.getString("data14")
                                        , "", "", js.getString("data15")
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

                            if (cChangeTripView.getVisibility() == View.GONE) {
                                cChangeTripView.setVisibility(View.VISIBLE);
                            }

                            new GetDriverDetails().execute(mCurrentDriver.getDriverId());
                        } else {
                            Utility.ShowAlertDialog(getActivity(), "Trip Cancelled"
                                    , "Oops, Seems your trip has been Cancelled ", false);
                        }

                    } else {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    e.printStackTrace();
                }

            }

        }
    }

    public class GetDriverDetails extends AsyncTask<String, JSONObject, String> {
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
                JSONObject json = api.getDriverInfo(params[0]);
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


            Log.d(TAG, String.format("searchAllCabs: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        //name,contact,dob,photo,carmodel,carno,rating

                        mCurrentDriver.setCarModel(js.getString("data4"));
                        mCurrentDriver.setCarNo(js.getString("data5"));
                        mCurrentDriver.setRating(js.getString("data6"));

                        UpdateDriverView();

                    } else {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    e.printStackTrace();
                }

            }

        }
    }

    private void UpdateDriverView() {
        String[] src = sLatLng.split(",");
        String[] dest = dLatLng.split(",");
        String[] driver = mCurrentDriver.getCurrentLocation().split(",");

        LatLng mSrc = new LatLng(Double.parseDouble(src[0]), Double.parseDouble(src[1]));
        LatLng mDest = new LatLng(Double.parseDouble(dest[0]), Double.parseDouble(dest[1]));
        LatLng mDriver = new LatLng(Double.parseDouble(driver[0]), Double.parseDouble(driver[1]));

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

        markerOptions = new MarkerOptions();
        markerOptions.position(mDriver);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_cab));
        markerOptions.title("Car");

        mDriverMarker = mGoogleMap.addMarker(markerOptions);

        mGoogleMap.addCircle(new CircleOptions()
                .center(mSrc)
                .strokeWidth(2)
                .strokeColor(Color.BLACK)
                .fillColor(0X85ffffff)
                .radius(300));


        driverImage.setImageBitmap(getImage(mCurrentDriver.getPhoto()));
        mDriverName.setText(mCurrentDriver.getName());
        getDriverDistance(mSourceLocation);
        float distance = Float.parseFloat(mCurrentDriver.getDistance());
        DecimalFormat decimalFormat = new DecimalFormat("#.0#");
        mDriverDistance.setText(String.format(Locale.getDefault(), "%s km", decimalFormat.format(distance)));
        mDriverArrival.setText(getTime(distance) + "");
        mDriverPrice.setText(String.format("%s %s"
                , getResources().getString(R.string.currency), mCurrentDriver.getPeekCost()));
        mDriverRating.setRating(Float.parseFloat(mCurrentDriver.getRating()));

        //TODO - Uncomment After Test
        progressDialog.setMessage("Fetching Directions");

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSource.getPosition(), 14));

        mDriverHolder.setVisibility(View.VISIBLE);

        cDriverHandler = new DriverHandler(getActivity(), CurrentTrips.this);
        cDriverHandler.sendEmptyMessage(START_BACKGROUND);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                animateSlideDown();
            }
        }, DELAY_MILLIS);

        requestDirections();

//        if (progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
    }

    private void getDriverDistance(Location mSrc) {
        String[] src = mCurrentDriver.getCurrentLocation().split(",");
        Location mLocation = new Location("");
        mLocation.setLatitude(Double.parseDouble(src[0]));
        mLocation.setLongitude(Double.parseDouble(src[1]));

        mCurrentDriver.setDistance((mLocation.distanceTo(mSrc) / 1000) + "");
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

    private Bitmap getImage(String Simage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(Simage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBs6ifdtEVD4B2hn0yNtezAvIr0GQJsxRI";

    private void requestDirections() {


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
                Log.d(TAG, "Error Volley Occurred");
            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private String getUrl() {
        String link = "NA";
        try {
            String source = URLEncoder.encode(mSourceLocation.getLatitude() + "," + mSourceLocation.getLongitude(), "utf-8");
            String destination = URLEncoder.encode(mDestinationLocation.getLatitude() + "," + mDestinationLocation.getLongitude(), "utf-8");
            mFinalList = new ArrayList<LatLng>();
            link = DIRECTION_URL_API + "origin=" + source + "&destination=" + destination + "&departure_time=" + System.currentTimeMillis() + "&traffic_model=best_guess&key=" + GOOGLE_API_KEY;
            Log.d(TAG, "getUrl: Link - " + link);

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
//        for (int i = 0; i < jsonRoutes.length(); i++)
//        {
        JSONObject jsonRoute = jsonRoutes.getJSONObject(0);

        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
        JSONObject jsonDuration_traffic = jsonLeg.getJSONObject("duration_in_traffic");
        JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

        String time_for_traffic = jsonDuration_traffic.getString("text");

        //one method retrivng last points Object (Accurate)
//        List<LatLng> ll = decodePolyLine(overview_polylineJson.getString("points"));

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

        directionsPolyline = mGoogleMap.addPolyline(po);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mSource.getPosition(), 14));

        mDriverHolder.setVisibility(View.VISIBLE);

        cDriverHandler = new DriverHandler(getActivity(), CurrentTrips.this);
        cDriverHandler.sendEmptyMessage(START_BACKGROUND);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                animateSlideDown();
            }
        }, DELAY_MILLIS);

        if (progressDialog.isShowing())
            progressDialog.dismiss();

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

    private static class DriverHandler extends Handler {
        private Context appContext;
        private CurrentTrips mMapFragment;
        private TimerTask timerTask;
        private Timer timer;
        private boolean isCheckComplete = true;
        private boolean isBoarded = false;

        public DriverHandler(@NonNull Context context, CurrentTrips mapsFragment) {
            this.appContext = context;
            mMapFragment = mapsFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case TRIP_COMPLETED:

                        try {
                            stopBackgroundCheck();

                            mMapFragment.setValuesToFeed();

                            clearAllView();

                            if (mMapFragment.mDriverHolder.getVisibility() == View.VISIBLE) {
                                mMapFragment.mDriverHolder.setVisibility(View.GONE);
                            }

                            if (mMapFragment.mFeedLayout.getVisibility() == View.GONE) {
                                mMapFragment.mFeedLayout.setVisibility(View.VISIBLE);
                            }

                            if (mMapFragment.cChangeTripView.getVisibility() == View.VISIBLE) {
                                mMapFragment.cChangeTripView.setVisibility(View.GONE);
                            }

                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }


                        break;
                    case BOARDED:
//                        if (mMapFragment.cancelCab.getVisibility() == View.VISIBLE)
//                            mMapFragment.cancelCab.setVisibility(View.GONE);
                        break;
                    case CANCELLED:
                        stopBackgroundCheck();

                        if (mMapFragment.mDriverHolder.getVisibility() == View.VISIBLE) {
                            mMapFragment.mDriverHolder.setVisibility(View.GONE);
                        }

                        if (mMapFragment.cChangeTripView.getVisibility() == View.VISIBLE) {
                            mMapFragment.cChangeTripView.setVisibility(View.GONE);
                        }

                        clearAllView();

                        AlertDialog.Builder builder = new AlertDialog.Builder(appContext)
                                .setTitle("Trip Cancelled")
                                .setMessage("This trip has been Cancelled")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(appContext, CabNavigationActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        appContext.startActivity(intent);
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        break;
                    case START_BACKGROUND:
                        startBackgroundCheck();
                        break;
                    case STOP_BACKGROUND:
                        stopBackgroundCheck();
                        break;
                }

            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }

        private void clearAllView() {
            mMapFragment.mGoogleMap.clear();
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
                                if (isCheckComplete) {
                                    mMapFragment.new GetDriverStatus().execute(
                                            mMapFragment.mCurrentDriver.getDriverId(), mMapFragment.mCurrentDriver.getTripId(), isBoarded + "");
                                }
                            } catch (Exception exp) {
                                exp.printStackTrace();
                            }
                        }
                    });
                }
            };

            timer = new Timer("DriverHandler");
            timer.schedule(timerTask, 0, 3000);
        }
    }

    private void setValuesToFeed() {
        feedMessage.setText(String.format("%s %s", getResources().getString(R.string.feedhint), mCurrentDriver.getName()));
        feedDriverName.setText(mCurrentDriver.getName());
        feedDriverImage.setImageBitmap(getImage(mCurrentDriver.getPhoto()));
    }

    private class GetDriverStatus extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetDriver";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (cDriverHandler != null)
                cDriverHandler.isCheckComplete = false;
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getDriverLocation(params[0], params[1], params[2]);
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


            Log.d(TAG, String.format("GetDriverStatus: %s", s));

            if (Utility.checkConnection(s)) {

                Log.d(TAG, "onPostExecute: No Internet");
                if (cDriverHandler != null)
                    cDriverHandler.isCheckComplete = true;
//                Pair<String, String> pair = Utility.GetErrorMessage(s);
//                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        //curloc,status
                        CurrentLocation = js.getString("data0");
                        CurrentStatus = js.getString("data1");
                        isStatusSame = js.getString("data2");

                        if (isStatusSame.compareTo("diff") == 0) {
                            destLatLng = js.getString("data3");
                            cabTripStatus = js.getString("data4");
                        }

                        updateDriverLocation();

                    } else {
                        if (cDriverHandler != null)
                            cDriverHandler.isCheckComplete = true;
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    if (cDriverHandler != null)
                        cDriverHandler.isCheckComplete = true;
                    e.printStackTrace();
                }

            }

        }
    }

    private class AddFeedback extends AsyncTask<String, JSONObject, String> {

        private static final String TAG = "AddFeedback";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.feedback(params[0], params[1], params[2], params[3], params[4]);
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

            Log.d(TAG, String.format("Response: %s", s));

            if (Utility.checkConnection(s)) {
                Log.d(TAG, "onPostExecute: No Internet");
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(getActivity(), "Thanks, Your feedback has Been Submitted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), CabNavigationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finish();
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

    private void updateDriverLocation() {
        String[] driver = CurrentLocation.split(",");

        LatLng mUpdatedLocation = new LatLng(Double.parseDouble(driver[0]), Double.parseDouble(driver[1]));
        LatLng mCurrentLocation = mDriverMarker.getPosition();

        if (isStatusSame.compareTo("same") == 0) {
            if (!mUpdatedLocation.equals(mCurrentLocation)) {

                mCurrentDriver.setCurrentLocation(CurrentLocation);

                mDriverMarker.remove();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mUpdatedLocation);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_cab));
                markerOptions.title("Car");

                getDriverDistance(mSourceLocation);
                float distance = Float.parseFloat(mCurrentDriver.getDistance());

                DecimalFormat decimalFormat = new DecimalFormat("#.0#");
                mDriverDistance.setText(String.format(Locale.getDefault(), "%s km", decimalFormat.format(distance)));
                mDriverArrival.setText(getTime(distance) + "");

                mDriverMarker = mGoogleMap.addMarker(markerOptions);
            }

        } else if (isStatusSame.compareTo("diff") == 0) {
            if (cabTripStatus.compareTo("Boarded") == 0) {

                if (!mUpdatedLocation.equals(mCurrentLocation)) {

                    mDriverMarker.remove();

                    mCurrentDriver.setCurrentLocation(CurrentLocation);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mUpdatedLocation);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_cab));
                    markerOptions.title("Car");

                    Location nLocation = new Location("");
                    String[] dlatlng = destLatLng.split(",");
                    nLocation.setLongitude(Double.parseDouble(dlatlng[0]));
                    nLocation.setLongitude(Double.parseDouble(dlatlng[1]));

                    getDriverDistance(nLocation);
                    float distance = Float.parseFloat(mCurrentDriver.getDistance());

                    int total = (int) (distance + getDistance(mSourceLocation, nLocation));

                    mCurrentDriver.setDistance(total + "");

                    DecimalFormat decimalFormat = new DecimalFormat("#.0#");
                    mDriverDistance.setText(String.format(Locale.getDefault(), "%s km", decimalFormat.format(distance)));
                    mDriverArrival.setText(getTime(total) + "");

                    mDriverMarker = mGoogleMap.addMarker(markerOptions);
                }

            } else {
                if (!mUpdatedLocation.equals(mCurrentLocation)) {

                    mDriverMarker.remove();

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(mUpdatedLocation);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_cab));
                    markerOptions.title("Car");

                    getDriverDistance(mSourceLocation);
                    float distance = Float.parseFloat(mCurrentDriver.getDistance());
                    DecimalFormat decimalFormat = new DecimalFormat("#.0#");
                    mDriverDistance.setText(String.format(Locale.getDefault(), "%s km", decimalFormat.format(distance)));
                    mDriverArrival.setText(getTime(distance) + "");

                    mDriverMarker = mGoogleMap.addMarker(markerOptions);
                }
            }
        }

        if (CurrentStatus.compareTo("Boarded") == 0) {
            cDriverHandler.isBoarded = true;
            cDriverHandler.sendEmptyMessage(BOARDED);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDriverMarker.getPosition(), 14));
        }

        if (CurrentStatus.compareTo("Finished") == 0) {
            isTripFinished = true;
            cDriverHandler.sendEmptyMessage(TRIP_COMPLETED);
        }

        if (CurrentStatus.compareTo("Cancelled") == 0) {
            cDriverHandler.sendEmptyMessage(CANCELLED);
        }

        if (cDriverHandler != null) {
            Log.d(TAG, "updateDriverLocation: Boolean Changed");
            cDriverHandler.isCheckComplete = true;
        }


    }

    private static class CancelTrip extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;
        private WeakReference<Context> appContext;
        private CurrentTrips cTrips;

        public CancelTrip(@NonNull Context mContext, CurrentTrips cTrips) {
            this.appContext = new WeakReference<Context>(mContext);
            this.cTrips = cTrips;
            progressDialog = new ProgressDialog(appContext.get());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.CancelTrip(strings[0]);
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
            Log.d(TAG, "Cancel Trips : " + s);
            if (Utility.checkConnection(s)) {
                Log.d(TAG, "onPostExecute: No Internet");
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(appContext.get(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String statusValue = json.getString("status");

                    if (statusValue.compareTo("true") == 0) {

                        cTrips.cDriverHandler.sendEmptyMessage(CANCELLED);

                    } else if (statusValue.compareTo("false") == 0) {
                        Toast.makeText(appContext.get(), "Cannot cancel this trip !", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }
    }

}
