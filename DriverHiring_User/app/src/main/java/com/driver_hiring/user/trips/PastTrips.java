package com.driver_hiring.user.trips;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.RestAPI;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PastTrips extends Fragment {
    private String UID = "";
    private ListView mPastTrips;
    private TextView mUpcomingText;

    private ArrayList<CustomModel> pPastTrips;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UID = PreferenceManager.getUserId(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_trips, container, false);
        mPastTrips = view.findViewById(R.id.past_trips);
        mUpcomingText = view.findViewById(R.id.text_past_trips);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetPassHistory().execute(UID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPastTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomModel customModel = pPastTrips.get(position);
                ShowDetails(customModel.getSource(), customModel.getDest()
                        , customModel.getReview().compareTo("NA") == 0 ? "No Review" : customModel.getReview());
            }
        });
    }

    public class GetPassHistory extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetCurrentCabs";
        private ProgressDialog progressDialog;

        private GetPassHistory() {
            progressDialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUpcomingText.setVisibility(View.VISIBLE);
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mPastTrips.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getPasttrips(params[0]);
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

                        mUpcomingText.setText("You don't have any past rides");

                    } else if (StatusValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");

                        pPastTrips = new ArrayList<CustomModel>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject js = jsonArray.getJSONObject(i);

                            pPastTrips.add(new CustomModel(
                                    js.getString("data0"), js.getString("data1"), js.getString("data2"),
                                    js.getString("data3"), js.getString("data4"), js.getString("data5"),
                                    js.getString("data6"), js.getString("data7"), js.getString("data8"),
                                    js.getString("data9"), js.getString("data10"), js.getString("data11"),
                                    js.getString("data12"), js.getString("data13")));
                        }

                        mUpcomingText.setText("");
                        mUpcomingText.setVisibility(View.GONE);

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

    private class PastAdapter extends ArrayAdapter<CustomModel> {
        private Context appContext;
        private ArrayList<CustomModel> aPastTrips;

        public PastAdapter(@NonNull Context context, int resource, @NonNull ArrayList<CustomModel> trips) {
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
            CustomModel customModel = aPastTrips.get(position);
            viewHolder.vSource.setText(customModel.getSource());
            viewHolder.vDestination.setText(customModel.getDest());
            viewHolder.vTripDate.setText(customModel.getDate());
            viewHolder.vTripCost.setText(String.format("%s %s"
                    , getResources().getString(R.string.currency), customModel.getPrice()));
            if (customModel.getRating().compareTo("NA") == 0) {
                viewHolder.vRating.setRating(0.0f);
            } else
                viewHolder.vRating.setRating(Float.parseFloat(customModel.getRating()));
            return convertView;
        }

        private class ViewHolder {
            TextView vSource, vDestination, vTripDate, vTripCost;
            RatingBar vRating;
        }
    }

    private class CustomModel {
        String tid, uid, did, cid, source, dest, slatlng, dlatlng, status, price, date, time, rating, review;

        public CustomModel(String tid, String uid, String did, String cid, String source, String dest, String slatlng, String dlatlng, String status
                , String price, String date, String time, String rating, String review) {
            this.tid = tid;
            this.uid = uid;
            this.did = did;
            this.cid = cid;
            this.source = source;
            this.dest = dest;
            this.slatlng = slatlng;
            this.dlatlng = dlatlng;
            this.status = status;
            this.price = price;
            this.date = date;
            this.time = time;
            this.rating = rating;
            this.review = review;
        }

        public String getTid() {
            return tid;
        }

        public String getUid() {
            return uid;
        }

        public String getDid() {
            return did;
        }

        public String getCid() {
            return cid;
        }

        public String getSource() {
            return source;
        }

        public String getDest() {
            return dest;
        }

        public String getSlatlng() {
            return slatlng;
        }

        public String getDlatlng() {
            return dlatlng;
        }

        public String getStatus() {
            return status;
        }

        public String getPrice() {
            return price;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getRating() {
            return rating;
        }

        public String getReview() {
            return review;
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
