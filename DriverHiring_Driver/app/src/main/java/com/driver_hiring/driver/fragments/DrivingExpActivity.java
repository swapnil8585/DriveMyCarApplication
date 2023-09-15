package com.driver_hiring.driver.fragments;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.driver.R;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.android.volley.VolleyLog.TAG;

public class DrivingExpActivity extends AppCompatActivity {
    private LinearLayout mDrivingList;
    private AppCompatButton mAddExperience;
    private TextView mTextErrorView;

    private ProgressDialog mProgressDialog;
    private ArrayList<String> carType, monthsExp;
    private Map<String, String> typeValueMapper;
    private String[] types;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);

        getSupportActionBar().setTitle("Experience");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrivingList = (LinearLayout) findViewById(R.id.list_list);
        mAddExperience = (AppCompatButton) findViewById(R.id.list_add);
        mTextErrorView = (TextView) findViewById(R.id.list_error);

        mTextErrorView.setText(getResources().getString(R.string.exp_load_error));
        mTextErrorView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.experience, 0, 0);

        types = getResources().getStringArray(R.array.car_type);

        typeValueMapper = new HashMap<String, String>();
        for (String type : types) {
            typeValueMapper.put(type, "0");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetExperienceTask().execute(PreferenceManager.getUserId(DrivingExpActivity.this));
    }

    @Override
    public void onStart() {
        super.onStart();

        mAddExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddExperienceTask(carType, monthsExp).execute(PreferenceManager.getUserId(DrivingExpActivity.this));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private class GetExperienceTask extends AsyncTask<String, JSONObject, String> {

        public GetExperienceTask() {
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(DrivingExpActivity.this);
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
                JSONObject jsonObject = cabHiringAPI.DgetExperience(strings[0]);
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
            Log.d(TAG, "onPostExecute: ");
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DrivingExpActivity.this, pair.first, pair.second, false);
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
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }
    }

    private void setValues() {

        Set<Map.Entry<String, String>> values = typeValueMapper.entrySet();
        carType = new ArrayList<>();
        monthsExp = new ArrayList<>();

        for (Map.Entry<String, String> items :
                values) {
            carType.add(items.getKey());
            monthsExp.add(items.getValue());
        }

        for (int i = 0; i < mDrivingList.getChildCount(); i++) {
            View view = mDrivingList.getChildAt(i);
            TextView itemName = view.findViewById(R.id.item_first);
            EditText itemValues = view.findViewById(R.id.item_second);
            itemName.setText(carType.get(i));
            itemValues.setText(monthsExp.get(i));
            itemValues.setSelection(itemValues.getText().toString().length());
        }

        setTextWatcher();
    }

    private void setTextWatcher() {
        for (int i = 0; i < mDrivingList.getChildCount(); i++) {
            View view = mDrivingList.getChildAt(i);
            EditText itemValues = view.findViewById(R.id.item_second);
            final int finalI = i;
            itemValues.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().compareTo(monthsExp.get(finalI)) != 0) {
                        monthsExp.set(finalI, s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private class AddExperienceTask extends AsyncTask<String, JSONObject, String> {
        private ArrayList<String> type, experience;

        @SafeVarargs
        public AddExperienceTask(@NonNull ArrayList<String>... type) {
            this.type = type[0];
            this.experience = type[1];

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Updating details..");
                mProgressDialog.show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = cabHiringAPI.DaddExperience(strings[0], type, experience);
                return new JSONParse().Parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DrivingExpActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject object = new JSONObject(s);
                    String exp = object.getString("status");
                    if (exp.compareTo("true") == 0) {
                        new GetExperienceTask().execute(PreferenceManager.getUserId(DrivingExpActivity.this));
                    } else {
                        if (mProgressDialog != null && mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(DrivingExpActivity.this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }
    }

}
