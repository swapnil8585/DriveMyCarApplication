package com.driver_hiring.driver.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.driver.R;
import com.driver_hiring.driver.TripsActivity;
import com.driver_hiring.driver.background.RideLocationUpdate;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.models.RideModel;
import com.driver_hiring.driver.rides.CurrentRides;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.RestAPI;
import com.driver_hiring.driver.webservices.Utility;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private String DID = "";
    private TextView current_name, current_price, current_dist, current_details, textWorking;
    private EditText current_src, current_dest;
    private RideModel mCurrentTrip;
    private ImageView workingButton;
    private LinearLayout mNoTrip, mTrips;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private ProgressDialog progressDialog;
    private TextView mProfileCheck;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DID = PreferenceManager.getUserId(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mProfileCheck = view.findViewById(R.id.home_profile);
        current_name = view.findViewById(R.id.current_trip_name);
        current_price = view.findViewById(R.id.current_trip_cost);
        current_dist = view.findViewById(R.id.current_trip_dist);
        current_details = view.findViewById(R.id.showTripsDetails);
        current_src = view.findViewById(R.id.current_trip_src);
        current_dest = view.findViewById(R.id.current_trip_dest);
        mTrips = view.findViewById(R.id.textTrips);
        mNoTrip = view.findViewById(R.id.textNoTrips);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        new CheckProfileTask().execute(PreferenceManager.getUserId(getActivity()));

        current_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mRideString = new Gson().toJson(mCurrentTrip);

                Bundle bundle = new Bundle();
                bundle.putString(TripsActivity.RIDE_DETAILS, mRideString);
                Log.d("HOME", "onClick: " + mRideString);
                Intent intent = new Intent(getActivity(), TripsActivity.class);
                intent.putExtra(TripsActivity.RIDE_DETAILS, bundle);
                startActivity(intent);
            }
        });
    }

    public class CheckProfileTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "CheckProfileTask";


        private CheckProfileTask() {

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
//                Log.d(TAG, "doInBackground: Param[0]" + params[0] + "Param[1]" + params[1]);
                JSONObject json = api.DcheckProfile(params[0]);
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

            Log.d(TAG, String.format("CheckProfileTask: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {

                        if (mProfileCheck.getVisibility() == View.VISIBLE) {
                            mProfileCheck.setVisibility(View.GONE);
                        }

                        new GetCurrentCabs().execute(DID, dateFormat.format(new Date().getTime()), "Current");

                    } else if (StatusValue.compareTo("false") == 0) {

                        if (mProfileCheck.getVisibility() == View.GONE) {
                            mProfileCheck.setVisibility(View.VISIBLE);
                        }
                        new GetCurrentCabs().execute(DID, dateFormat.format(new Date().getTime()), "Current");
                    } else {
                        if (mProfileCheck.getVisibility() == View.VISIBLE) {
                            mProfileCheck.setVisibility(View.GONE);
                        }
                        new GetCurrentCabs().execute(DID, dateFormat.format(new Date().getTime()), "Current");

                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Details " + error);
                    }

                } catch (Exception e) {
                    if (mProfileCheck.getVisibility() == View.VISIBLE) {
                        mProfileCheck.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "onPostExecute: Exception : " + e.getMessage());
//                    e.printStackTrace();
                }

            }

        }
    }

    public class GetCurrentCabs extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";


        private GetCurrentCabs() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!progressDialog.isShowing()) {
                progressDialog.setMessage("Please Wait");
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.DgetRides(params[0], params[1], params[2]);
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

            Log.d(TAG, String.format("Current: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        mNoTrip.setVisibility(View.VISIBLE);
                        mTrips.setVisibility(View.GONE);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);
                        // 0   1   2   3    4     5      6      7      8      9     10   11    12    13
                        //rid,uid,did,cid,source,dest,slatlng,dlatlng,status,price,date,time,uname,ucont

                        //0rideID, 1userID, 2userName, 3cabID, 4Status, 5Price, 6Ratings, 7startDate, 8startTime
                        //            , 9startAddress, 10sourceLatLng, 11endDate, 12endTime, 13endAddress, 14endLatLng, 15totalDays
                        //            , 16totalhHours, 17placetype
                        //0Rid,1Uid,2uName,3Cid,4Status,5Price,6Ratings,7Rid,8sdate,9stime,10sadd,11slatlng,12edate
                        // ,13etime,14add,15elatlng,
                        //16totdays,17tothours,18placetype

                        mCurrentTrip =
                                new RideModel(
                                        js.getString("data0"), js.getString("data1"), js.getString("data2"),
                                        js.getString("data3"), js.getString("data4"), js.getString("data5"),
                                        js.getString("data6"), js.getString("data8"), js.getString("data9"),
                                        js.getString("data10"), js.getString("data11"), js.getString("data12"),
                                        js.getString("data13"), js.getString("data14"), js.getString("data15"),
                                        js.getString("data16"), js.getString("data17"), js.getString("data18"));

                        setValues();

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

    private void setValues() {

        mNoTrip.setVisibility(View.GONE);
        mTrips.setVisibility(View.VISIBLE);

        current_name.setText(mCurrentTrip.getUserName());
        current_price.setText(String.format("%s %s", getResources().getString(R.string.currency), mCurrentTrip.getPrice()));

        String[] src = mCurrentTrip.getSourceLatLng().split(",");
        Location mSrc = new Location("");
        mSrc.setLatitude(Double.parseDouble(src[0]));
        mSrc.setLongitude(Double.parseDouble(src[1]));

        String[] dest = mCurrentTrip.getEndLatLng().split(",");
        Location mDest = new Location("");
        mDest.setLatitude(Double.parseDouble(dest[0]));
        mDest.setLongitude(Double.parseDouble(dest[1]));

        current_dist.setText(String.format(Locale.US, "%d km", CurrentRides.getDistance(mSrc, mDest)));

        current_src.setText(mCurrentTrip.getStartAddress());
        current_dest.setText(mCurrentTrip.getEndAddress());

        if (mCurrentTrip.getStatus().contains("Started")) {
            getActivity().stopService(new Intent(getActivity(), RideLocationUpdate.class));
            getActivity().startService(new Intent(getActivity(), RideLocationUpdate.class));
        }

        mTrips.setVisibility(View.VISIBLE);
    }

}
