package com.driver_hiring.user.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.Fragment;

import com.driver_hiring.user.CabNavigationActivity;
import com.driver_hiring.user.background.RateRideTask;
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

public class PastRides extends Fragment {
    private String UID = "";
    private ListView mPastTrips;
    private TextView mPastText, chooseDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private Calendar mCurrentCalender;
    private ArrayList<RideModel> pPastTrips;

    private DatePickerDialog datePickerDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UID = PreferenceManager.getUserId(getActivity());
        mCurrentCalender = Calendar.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_trips, container, false);
        mPastTrips = view.findViewById(R.id.past_trips);
        mPastText = view.findViewById(R.id.text_past_trips);
        chooseDate = view.findViewById(R.id.pastDate);
        chooseDate.setText(String.format("Date : %s", dateFormat.format(mCurrentCalender.getTime())));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetPassHistory().execute(UID, dateFormat.format(mCurrentCalender.getTime()), "Previous");
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

        mPastTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RideModel customModel = pPastTrips.get(position);
                ShowDetails(customModel.getStartAddress(), customModel.getEndAddress()
                        , customModel.getPlacetype().compareTo("NA") == 0 ? "No Review" : customModel.getPlacetype());
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

            new GetPassHistory().execute(UID, dateFormat.format(mCurrentCalender.getTime()), "Previous");
        }
    };

    public class GetPassHistory extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetDriverCurrent";
        private ProgressDialog progressDialog;

        private GetPassHistory() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mPastTrips.setAdapter(null);
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
            Log.d(TAG, String.format("Past: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        mPastText.setVisibility(View.VISIBLE);
                        mPastText.setText("You don't have any past rides");

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

                        mPastText.setText("");
                        mPastText.setVisibility(View.GONE);

                        PastAdapter pastAdapter = new PastAdapter(getActivity(), R.layout.item_past_trips, pPastTrips);
                        mPastTrips.setAdapter(pastAdapter);

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

    private class PastAdapter extends ArrayAdapter<RideModel> {
        private Context appContext;
        private ArrayList<RideModel> aPastTrips;

        public PastAdapter(@NonNull Context context, int resource, @NonNull ArrayList<RideModel> trips) {
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
                viewHolder.rateButton = convertView.findViewById(R.id.item_rate_trip);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final RideModel customModel = aPastTrips.get(position);

            viewHolder.vSource.setText(customModel.getStartAddress());
            viewHolder.vDestination.setText(customModel.getEndAddress());
            viewHolder.vTripDate.setText(customModel.getStartDate());
            viewHolder.vTripCost.setText(String.format("%s %s"
                    , getResources().getString(R.string.currency), customModel.getPrice()));
            if (customModel.getRatings().compareTo("") == 0) {
                viewHolder.rateButton.setVisibility(View.VISIBLE);
                viewHolder.vRating.setRating(0.0f);
            } else
                viewHolder.vRating.setRating(Float.parseFloat(customModel.getRatings()));

            viewHolder.rateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRatingDialog(customModel.getRideID());
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView vSource, vDestination, vTripDate, vTripCost;
            RatingBar vRating;
            AppCompatButton rateButton;
        }

        private void showRatingDialog(@NonNull final String rideID) {
            final View rating = LayoutInflater.from(getActivity()).inflate(R.layout.notification_rating
                    , null, false);
            final AppCompatRatingBar ratingBar = rating.findViewById(R.id.driverRating);

            new AlertDialog.Builder(getActivity())
                    .setCancelable(false)
                    .setTitle("Rate this Ride")
                    .setMessage("Please rate what was your experience with this ride in Scale from 1 to 5 stars" +
                            ", 5 being extremely satisfied")
                    .setView(rating)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new RateRideTask(getActivity()).execute(rideID
                                    , ratingBar.getRating() + "");
                        }
                    })
                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void ShowDetails(@NonNull String Src, @NonNull String Dest, @NonNull String Review) {
        final Dialog mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.past_trip_details);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        TextView src = mDialog.findViewById(R.id.pTripsSrc);
        TextView dest = mDialog.findViewById(R.id.pTripsDest);
        TextView review = mDialog.findViewById(R.id.pTripReview);
        Button mBtn = mDialog.findViewById(R.id.details_ok);

        src.setText(String.format("Source : \n%s", Src));
        dest.setText(String.format("Destination : \n%s", Dest));
        review.setText(String.format("Review : \n%s", Review));

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog.isShowing())
                    mDialog.dismiss();
            }
        });

        mDialog.show();
    }

}
