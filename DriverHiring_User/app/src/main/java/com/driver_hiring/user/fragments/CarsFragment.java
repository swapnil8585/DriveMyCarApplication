package com.driver_hiring.user.fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.driver_hiring.user.activities.AddCarActivity;
import com.driver_hiring.user.activities.CarDetailActivity;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.models.CarsModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarsFragment extends Fragment {
    private static final String TAG = "CarsFragment";
    private ListView mCarsList;
    private TextView mCarsError;
    private String driverId = "";
    private ArrayList<CarsModel> mUserCars;
    private FloatingActionButton mAddButton;


    public CarsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driverId = PreferenceManager.getUserId(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_past_trips, container, false);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.pastDate).setVisibility(View.GONE);
        mCarsList = (ListView) view.findViewById(R.id.past_trips);
        mCarsError = (TextView) view.findViewById(R.id.text_past_trips);
        mAddButton = view.findViewById(R.id.add_item);

        mCarsList.setPadding(5, 5, 5, 5);
        mCarsList.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        mCarsList.setVisibility(View.GONE);

        mAddButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onStart() {
        super.onStart();

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddCarActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetCars().execute(driverId);
    }

    private class GetCars extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;

        public GetCars() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mCarsList.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UgetCar(params[0]);
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
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");

                    if (ans.compareTo("ok") == 0) {

                        //cid,uid,brand,model,transmision,year,chasisno,carno,type,fuel

                        mUserCars = new ArrayList<CarsModel>();

                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            mUserCars.add(new CarsModel(jdata.getString("data0"), jdata.getString("data1")
                                    , jdata.getString("data2"), jdata.getString("data3")
                                    , jdata.getString("data4"), jdata.getString("data5")
                                    , jdata.getString("data6"), jdata.getString("data7")
                                    , jdata.getString("data8"), jdata.getString("data9")));


                        }
                        if (mUserCars.size() > 0) {
                            if (mCarsError.getVisibility() == View.VISIBLE) {
                                mCarsError.setVisibility(View.GONE);
                            }
                            if (mCarsList.getVisibility() == View.GONE) {
                                mCarsList.setVisibility(View.VISIBLE);
                            }
                            Adapter adapter = new Adapter(getActivity(), mUserCars);
                            mCarsList.setAdapter(null);
                            mCarsList.setAdapter(adapter);
                        }
                    } else if (ans.compareTo("false") == 0) {

                        mCarsList.setAdapter(null);
                        mCarsError.setText("No car's found.");

                        if (mCarsError.getVisibility() == View.GONE) {
                            mCarsError.setVisibility(View.VISIBLE);
                        }

                        if (mCarsList.getVisibility() == View.VISIBLE) {
                            mCarsList.setVisibility(View.GONE);
                        }

                    } else if (ans.compareTo("mCarsError") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class Adapter extends ArrayAdapter<CarsModel> {
        private Context con;

        public Adapter(@NonNull Context context, ArrayList<CarsModel> a) {
            super(context, R.layout.transation_view_item, a);
            this.con = context;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MyViewHolder myViewHolder;
            if (convertView == null) {
                myViewHolder = new MyViewHolder();
                convertView = LayoutInflater.from(con).inflate(R.layout.item_cars, null, true);
                myViewHolder.mItemType = convertView.findViewById(R.id.item_type);
                myViewHolder.mItemBrand = convertView.findViewById(R.id.item_brand);
                myViewHolder.mItemModel = convertView.findViewById(R.id.item_model);
                myViewHolder.mViewDetails = convertView.findViewById(R.id.item_details);
                convertView.setTag(myViewHolder);
            } else {
                myViewHolder = (MyViewHolder) convertView.getTag();
            }

            myViewHolder.mItemType.setText(mUserCars.get(position).getType());
            myViewHolder.mItemBrand.setText(mUserCars.get(position).getBrand());
            myViewHolder.mItemModel.setText(mUserCars.get(position).getModel());
            myViewHolder.mViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mRideString = new Gson().toJson(mUserCars.get(position));

                    Bundle bundle = new Bundle();
                    bundle.putString(CarDetailActivity.CAR_DETAILS, mRideString);
                    Log.d("HOME", "onClick: " + mRideString);
                    Intent intent = new Intent(getActivity(), CarDetailActivity.class);
                    intent.putExtra(CarDetailActivity.CAR_DETAILS, bundle);
                    startActivity(intent);
                }
            });

            return convertView;
        }

        private class MyViewHolder {
            private TextView mItemType, mItemBrand, mItemModel;
            private ImageView mViewDetails;
        }
    }


}
