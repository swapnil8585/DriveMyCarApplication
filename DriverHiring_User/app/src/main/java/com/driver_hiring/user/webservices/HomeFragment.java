package com.driver_hiring.user.webservices;


import android.app.ProgressDialog;
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
import android.widget.TextView;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.helper.DriverModel;
import com.driver_hiring.user.trips.CurrentTrips;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private String DID = "";
    private TextView current_name, current_price, current_dist, current_details;
    private EditText current_src, current_dest;
    private DriverModel mCurrentTrip;
    private String tripSource, tripDest, sLatLng, dLatLng;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DID = PreferenceManager.getUserId(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        current_name = view.findViewById(R.id.current_trip_name);
        current_price = view.findViewById(R.id.current_trip_cost);
        current_dist = view.findViewById(R.id.current_trip_dist);
        current_details = view.findViewById(R.id.showTripsDetails);
        current_src = view.findViewById(R.id.current_trip_src);
        current_dest = view.findViewById(R.id.current_trip_dest);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        current_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetCurrentCabs().execute(DID, dateFormat.format(new Date().getTime()));
    }

    public class GetCurrentCabs extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";
        private ProgressDialog progressDialog;

        private GetCurrentCabs() {
            progressDialog = new ProgressDialog(getActivity());
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
                        // 0   1   2   3    4     5      6      7      8      9     10   11    12    13
                        //tid,uid,did,cid,source,dest,slatlng,dlatlng,status,price,date,time,uname,ucont

                        mCurrentTrip =
                                new DriverModel(
                                        js.getString("data2"), js.getString("data12")
                                        , js.getString("data13"), js.getString("data14")
                                        , "", "", js.getString("data15")
                                        , js.getString("data9"), "", "", ""
                                        , js.getString("data0"));

                        tripSource = js.getString("data4");
                        tripDest = js.getString("data5");
                        sLatLng = js.getString("data6");
                        dLatLng = js.getString("data7");

                        setValues();

                    } else {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
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
        current_name.setText(mCurrentTrip.getName());
        current_price.setText(mCurrentTrip.getPeekCost());
        String[] src = sLatLng.split(",");
        Location mSrc = new Location("");
        mSrc.setLatitude(Double.parseDouble(src[0]));
        mSrc.setLongitude(Double.parseDouble(src[1]));

        String[] dest = sLatLng.split(",");
        Location mDest = new Location("");
        mDest.setLatitude(Double.parseDouble(dest[0]));
        mDest.setLongitude(Double.parseDouble(dest[1]));

        current_dist.setText(String.format(Locale.US, "%d km", CurrentTrips.getDistance(mSrc, mDest)));

        current_src.setText(tripSource);
        current_dest.setText(tripDest);
    }

}
