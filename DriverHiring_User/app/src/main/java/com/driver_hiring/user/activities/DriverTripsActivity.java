package com.driver_hiring.user.activities;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.driver_hiring.user.R;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.android.volley.VolleyLog.TAG;

public class DriverTripsActivity extends AppCompatActivity {
    private LinearLayout mDrivingList;
    private TextView mTextErrorView;

    private ProgressDialog mProgressDialog;
    private ArrayList<String> tripType, noOfTrip;
    private Map<String, String> typeValueMapper;
    private String[] types;
    private static int mChildCount = 8;
    private String DID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);

        getSupportActionBar().setTitle("Experience");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrivingList = (LinearLayout) findViewById(R.id.list_list);
        mTextErrorView = (TextView) findViewById(R.id.list_error);

        TextView mFirst = (TextView) findViewById(R.id.item_first);
        TextView mSecond = (TextView) findViewById(R.id.item_second);

        mFirst.setText("Trips");
        mSecond.setText("No Of Trips");

        mTextErrorView.setText(getResources().getString(R.string.exp_load_error));
        mTextErrorView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.experience, 0, 0);

        types = getResources().getStringArray(R.array.trips_type);

        typeValueMapper = new HashMap<String, String>();
        for (String type : types) {
            typeValueMapper.put(type, "0");
        }

        DID = getIntent().getStringExtra(DriverProfileActivity.DRIVER_ID);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetTripsTask().execute(DID);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private class GetTripsTask extends AsyncTask<String, JSONObject, String> {

        public GetTripsTask() {
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(DriverTripsActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Loading documents..");
                mProgressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = cabHiringAPI.DgetTrips(strings[0]);
                return new JSONParse().Parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DriverTripsActivity.this, pair.first, pair.second, false);
            } else {
                if (mTextErrorView.getVisibility() == View.VISIBLE)
                    mTextErrorView.setVisibility(View.GONE);

                if (mDrivingList.getVisibility() == View.GONE)
                    mDrivingList.setVisibility(View.VISIBLE);
                try {
                    JSONObject object = new JSONObject(s);
                    String exp = object.getString("status");
                    if (exp.compareTo("ok") == 0) {
                        JSONArray result = object.getJSONArray("Data");

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject jsonObject = result.getJSONObject(i);

                            typeValueMapper.remove(jsonObject.getString("data0"));
                            typeValueMapper.put(jsonObject.getString("data0")
                                    , jsonObject.getString("data1"));
                        }

                        setValues();

                    } else if (exp.compareTo("no") == 0) {
                        setValues();
                    } else {
                        setValues();
                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }
    }

    private void setValues() {

        Set<Map.Entry<String, String>> values = typeValueMapper.entrySet();
        tripType = new ArrayList<>();
        noOfTrip = new ArrayList<>();

        for (Map.Entry<String, String> items :
                values) {
            tripType.add(items.getKey());
            noOfTrip.add(items.getValue());
        }
        for (int i = mDrivingList.getChildCount(); i >= 6; i--) {
            View view = mDrivingList.getChildAt(i);
            mDrivingList.removeView(view);
        }

        for (int i = 0; i < mDrivingList.getChildCount(); i++) {
            View view = mDrivingList.getChildAt(i);
            TextView itemName = view.findViewById(R.id.item_first);
            EditText itemValues = view.findViewById(R.id.item_second);
            itemName.setText(tripType.get(i));
            itemValues.setText(noOfTrip.get(i));
            itemValues.setSelection(itemValues.getText().toString().length());
        }
    }

}
