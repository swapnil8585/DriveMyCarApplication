package com.driver_hiring.user.fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.models.RideModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RidesFragment extends Fragment {
    private String UID = "";
    private ListView mAllTrips;
    private TextView mAllText, chooseDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private Calendar mCurrentCalender;
    private ArrayList<RideModel> pPastTrips;

    private DatePickerDialog datePickerDialog;


    public RidesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UID = PreferenceManager.getUserId(getActivity());
        mCurrentCalender = Calendar.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_trips, container, false);
        mAllTrips = view.findViewById(R.id.past_trips);
        mAllText = view.findViewById(R.id.text_past_trips);
        chooseDate = view.findViewById(R.id.pastDate);
        chooseDate.setText(String.format("Date : %s", dateFormat.format(mCurrentCalender.getTime())));

        new GetRides().execute(UID, dateFormat.format(mCurrentCalender.getTime()), "All");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(getActivity(), dateDateListener
                        , mCurrentCalender.get(Calendar.YEAR)
                        , mCurrentCalender.get(Calendar.MONTH)
                        , mCurrentCalender.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(new java.util.Date().getTime());
                if (!datePickerDialog.isShowing()) {
                    datePickerDialog.show();
                }
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCurrentCalender.set(Calendar.YEAR, year);
            mCurrentCalender.set(Calendar.MONTH, month);
            mCurrentCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            chooseDate.setText(String.format("Date : %s", dateFormat.format(mCurrentCalender.getTime())));

            new GetRides().execute(UID, dateFormat.format(mCurrentCalender.getTime()), "All");
        }
    };

    public class GetRides extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetDriverCurrent";
        private ProgressDialog progressDialog;

        private GetRides() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mAllTrips.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
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

            if (progressDialog.isShowing())
                progressDialog.dismiss();
            Log.d(TAG, String.format("All: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        mAllText.setVisibility(View.VISIBLE);
                        mAllText.setText("You don't have any rides");

                    } else if (StatusValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");

                        pPastTrips = new ArrayList<RideModel>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject js = jsonArray.getJSONObject(i);

                            pPastTrips.add(new RideModel(
                                    js.getString("data0"), js.getString("data1"), js.getString("data2"),
                                    js.getString("data3"), js.getString("data4"), js.getString("data5"),
                                    js.getString("data6"), js.getString("data8"), js.getString("data9"),
                                    js.getString("data10"), js.getString("data11"), js.getString("data12"),
                                    js.getString("data13"), js.getString("data14"), js.getString("data15"),
                                    js.getString("data16"), js.getString("data17"), js.getString("data18"), ""));
                        }

                        mAllText.setText("");
                        mAllText.setVisibility(View.GONE);

                        Adapter adapter = new Adapter(getActivity(), R.layout.item_past_trips, pPastTrips);
                        mAllTrips.setAdapter(adapter);

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

    private class Adapter extends ArrayAdapter<RideModel> {
        private Context appContext;
        private ArrayList<RideModel> aPastTrips;

        public Adapter(@NonNull Context context, int resource, @NonNull ArrayList<RideModel> trips) {
            super(context, resource, trips);
            this.appContext = context;
            this.aPastTrips = trips;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(appContext).inflate(R.layout.item_past_trips, parent, false);
                viewHolder = new ViewHolder();

                viewHolder.vSource = convertView.findViewById(R.id.trip_source);
                viewHolder.vDestination = convertView.findViewById(R.id.trip_destination);
                viewHolder.vTripDate = convertView.findViewById(R.id.trip_date);
                viewHolder.vTripCost = convertView.findViewById(R.id.trip_cost);
                viewHolder.vRating = convertView.findViewById(R.id.trip_rating);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            RideModel customModel = aPastTrips.get(position);
            viewHolder.vSource.setText(customModel.getStartAddress());
            viewHolder.vDestination.setText(customModel.getEndAddress());
            viewHolder.vTripDate.setText(customModel.getStartDate());
            viewHolder.vTripCost.setText(String.format("%s %s"
                    , getResources().getString(R.string.currency), customModel.getPrice()));
            if (customModel.getRatings().compareTo("NA") == 0) {
                viewHolder.vRating.setRating(0.0f);
            } else
                viewHolder.vRating.setRating(Float.parseFloat(customModel.getRatings()));
            return convertView;
        }

        private class ViewHolder {
            TextView vSource, vDestination, vTripDate, vTripCost;
            RatingBar vRating;
        }
    }
}
