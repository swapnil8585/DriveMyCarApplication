package com.driver_hiring.user.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.user.CurrentRideActivity;
import com.driver_hiring.user.activities.BookRideActivity;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.models.RideModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HomeFragment extends Fragment {
    private TextView mHomeNoTrips, mHomeUserName;
    private AppCompatButton mBookRide, mShowRide;
    private CardView mCurrentRideView;
    private TextView current_name, current_price, current_dist;
    private EditText current_src, current_dest;
    private ProgressDialog mProgressDialog;
    private RideModel mCurrentTrip;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    public HomeFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        current_name = view.findViewById(R.id.current_trip_name);
        current_price = view.findViewById(R.id.current_trip_cost);
        current_dist = view.findViewById(R.id.current_trip_dist);
        current_src = view.findViewById(R.id.current_trip_src);
        current_dest = view.findViewById(R.id.current_trip_dest);
        mHomeUserName = view.findViewById(R.id.home_user_name);

        mHomeNoTrips = view.findViewById(R.id.home_no_rides);
        mBookRide = view.findViewById(R.id.home_book_ride);
        mShowRide = view.findViewById(R.id.show_current_ride);
        mCurrentRideView = view.findViewById(R.id.current_rides);

        mHomeUserName.setText("Welcome, " + PreferenceManager.getUserName(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetCurrentCabs().execute(PreferenceManager.getUserId(getActivity())
                , dateFormat.format(new Date().getTime()), "Current");
    }

    @Override
    public void onStart() {
        super.onStart();

        mBookRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BookRideActivity.class));
            }
        });

        mHomeNoTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHomeNoTrips.setVisibility(View.GONE);
            }
        });

        mShowRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mRideString = new Gson().toJson(mCurrentTrip);

                Bundle bundle = new Bundle();
                bundle.putString(CurrentRideActivity.RIDE_DETAILS, mRideString);
                Log.d("HOME", "onClick: " + mRideString);
                Intent intent = new Intent(getActivity(), CurrentRideActivity.class);
                intent.putExtra(CurrentRideActivity.RIDE_DETAILS, bundle);
                startActivity(intent);
            }
        });

    }

    public class GetCurrentCabs extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";


        private GetCurrentCabs() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Please Wait");
                mProgressDialog.show();
            }
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
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            Log.d(TAG, String.format("Current: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        mHomeNoTrips.setVisibility(View.VISIBLE);
                        mCurrentRideView.setVisibility(View.GONE);

//                        Utility.ShowAlertDialog(getActivity(), "No Current Booking"
//                                , "You don't have any trips", false);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");

                        JSONObject js = jsonArray.getJSONObject(0);

                        //Rid,Did,DName,Cid,Status,Price,Rid,sdate,stime,sadd,slatlng,edate,etime,eadd,elatlng,
                        //totdays,tothours,placetype

                        mCurrentTrip =
                                new RideModel(
                                        js.getString("data0"), js.getString("data1"), js.getString("data2"),
                                        js.getString("data3"), js.getString("data4"), js.getString("data5"),
                                        "", js.getString("data7"), js.getString("data8"),
                                        js.getString("data9"), js.getString("data10"), js.getString("data11"),
                                        js.getString("data12"), js.getString("data13"), js.getString("data14"),
                                        js.getString("data15"), js.getString("data15"), js.getString("data17")
                                        , js.getString("data18"));

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
        mHomeNoTrips.setVisibility(View.GONE);
        mCurrentRideView.setVisibility(View.VISIBLE);

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

        current_dist.setText(String.format(Locale.US, "%d km", getDistance(mSrc, mDest)));

        current_src.setText(mCurrentTrip.getStartAddress());
        current_dest.setText(mCurrentTrip.getEndAddress());

//        mTrips.setVisibility(View.VISIBLE);
    }

    private int getDistance(Location mSrc, Location mDest) {
        return (int) (mSrc.distanceTo(mDest) / 1000);
    }
}
