package com.driver_hiring.user.fragments;

import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
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
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UpcomingRides extends Fragment {
    private String UID = "";
    private ListView mUpcomingTrips;
    private TextView mUpcomingText;
    private ArrayList<RideModel> pPastTrips;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UID = PreferenceManager.getUserId(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_trips, container, false);
        view.findViewById(R.id.pastDate).setVisibility(View.GONE);
        mUpcomingTrips = view.findViewById(R.id.past_trips);
        mUpcomingText = view.findViewById(R.id.text_past_trips);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetUpomingTrips().execute(UID, dateFormat.format(new Date().getTime()), "Upcoming");
    }

    @Override
    public void onStart() {
        super.onStart();

        mUpcomingTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                CustomModel customModel = pPastTrips.get(position);
//                ShowDetails(customModel.getSource(), customModel.getDest()
//                        , customModel.getContact().compareTo("NA") == 0 ? "No Review" : customModel.getContact());
            }
        });
    }

    public class GetUpomingTrips extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetDriverCurrent";
        private ProgressDialog progressDialog;

        private GetUpomingTrips() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUpcomingText.setVisibility(View.VISIBLE);
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mUpcomingTrips.setAdapter(null);
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
            Log.d(TAG, String.format("Current: %s", s));

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {

                        mUpcomingText.setText("You don't have any upcoming rides");

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

                        mUpcomingText.setText("");
                        mUpcomingText.setVisibility(View.GONE);

                        ListAdapter upcomingAdapter = new ListAdapter(getActivity(), R.layout.item_past_trips, pPastTrips);
                        mUpcomingTrips.setAdapter(upcomingAdapter);

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

    private class ListAdapter extends ArrayAdapter<RideModel> {
        private static final String TAG = "List";
        private Context appContext;
        private ArrayList<RideModel> aPastTrips;

        public ListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<RideModel> trips) {
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
                viewHolder.vRideDate = convertView.findViewById(R.id.trip_date);
                viewHolder.vRideCost = convertView.findViewById(R.id.trip_cost);
                viewHolder.vRating = convertView.findViewById(R.id.trip_rating);
                viewHolder.vRideStatus = convertView.findViewById(R.id.trip_staus);
                viewHolder.vRideStart = convertView.findViewById(R.id.item_start_trip);
                viewHolder.vRideCancelButton = convertView.findViewById(R.id.item_cancel_trip);
                viewHolder.vRideUserName = convertView.findViewById(R.id.item_user_name);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final RideModel customModel = aPastTrips.get(position);

            viewHolder.vRideUserName.setVisibility(View.VISIBLE);

            if (checkForStartDate(getCalender(customModel.getStartDate()))) {
                viewHolder.vRideStart.setVisibility(View.VISIBLE);
            }

            if (getDateDifference(getCalender(customModel.getStartDate())) > 3) {
                viewHolder.vRideCancelButton.setVisibility(View.VISIBLE);
            }

            viewHolder.vRideUserName.setText(String.format("Name : %s", customModel.getUserName()));
            viewHolder.vSource.setText(customModel.getStartAddress());
            viewHolder.vDestination.setText(customModel.getEndAddress());
            viewHolder.vRideDate.setText(customModel.getStartDate());
            viewHolder.vRideCost.setText(String.format("%s %s"
                    , getResources().getString(R.string.currency), customModel.getPrice()));
            viewHolder.vRideStatus.setText(String.format("Status : %s", customModel.getStatus()));

            viewHolder.vRideStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(appContext)
                            .setTitle("Start Ride !")
                            .setMessage("Are you sure you want to start this Ride?")
                            .setPositiveButton("Yes, Start Ride", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ChangeStatus().execute(customModel.getRideID(), "Started", "Driver");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

            viewHolder.vRideCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(appContext)
                            .setTitle("Cancel Ride !")
                            .setMessage("Are you sure you want to Cancel this Ride?")
                            .setPositiveButton("Yes, Cancel Ride", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ChangeStatus().execute(customModel.getRideID(), "Cancelled", "Driver");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            return convertView;
        }

        private long getDateDifference(Calendar calender) {
            Date mCurrentDate = Calendar.getInstance().getTime();
            Date mRideDate = calender.getTime();
            Log.d(TAG, String.format("Current Date : %s \n Start Date : %s"
                    , dateFormat.format(mCurrentDate.getTime())
                    , dateFormat.format(mRideDate.getTime())));
            long diff = Math.abs(mRideDate.getTime() - mCurrentDate.getTime());
            Log.d(TAG, "getDateDifference: " + TimeUnit.MILLISECONDS.toDays(diff));
            return TimeUnit.MILLISECONDS.toDays(diff);
        }

        private boolean checkForStartDate(@NonNull Calendar mItemDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return mItemDate.getTime().equals(calendar.getTime());
        }

        private class ViewHolder {
            TextView vSource, vDestination, vRideDate, vRideCost, vRideStatus, vRideUserName;
            AppCompatButton vRideStart, vRideCancelButton;
            RatingBar vRating;
        }
    }

    private Calendar getCalender(String startDate) {
        String[] dates = startDate.split("/");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
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

    private class ChangeStatus extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "ChangeStatus";
        private String Message = "";
        private ProgressDialog progressDialog;

        public ChangeStatus() {
            progressDialog = new ProgressDialog(getActivity());
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
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {

                        Toast.makeText(getActivity(), "Trip Cancelled", Toast.LENGTH_SHORT).show();

                        new GetUpomingTrips().execute(UID, dateFormat.format(new Date().getTime()));

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


}
