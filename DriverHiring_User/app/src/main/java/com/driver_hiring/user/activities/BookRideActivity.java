package com.driver_hiring.user.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.user.RideBookActivity;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.models.DriverModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class BookRideActivity extends AppCompatActivity {
    private static final String TAG = "BookRide";
    private ArrayList<String> mCabId, mCarModels, mCarBrands, mCarType, mSpinList;

    private ArrayList<String> mSelPlaces;
    private String[] places;

    private ArrayList<DriverModel> mDrivers;

    private ListView mDriverList;
    private TextView mDriverError, mPlaceGridView;
    private EditText mNoOfDays;
    private Spinner mCarsSpinner;
    private AppCompatButton mSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ride);

        getSupportActionBar().setTitle("Book Ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mDriverList = findViewById(R.id.past_trips);
        mDriverError = findViewById(R.id.text_past_trips);
        findViewById(R.id.pastDate).setVisibility(View.GONE);

        mNoOfDays = findViewById(R.id.ride_days);
        mPlaceGridView = findViewById(R.id.ride_place_types);
        mCarsSpinner = findViewById(R.id.ride_cars);
        mSearchButton = findViewById(R.id.search_drivers);

        places = getResources().getStringArray(R.array.trips_type);
        mSelPlaces = new ArrayList<String>();

        mPlaceGridView.setText(String.format(Locale.US
                , "Places Selected (%d)", mSelPlaces.size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetCars().execute(PreferenceManager.getUserId(BookRideActivity.this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCarType != null && mCarType.size() > 0) {
                    if (mCarsSpinner.getSelectedItemPosition() == 0) {
                        Snackbar.make(v, "Please, Choose Cabs", Snackbar.LENGTH_SHORT).show();
                    } else if (mSelPlaces.size() == 0) {
                        Snackbar.make(v, "Please, Choose at-least one from Places", Snackbar.LENGTH_SHORT).show();
                    } else if (mNoOfDays.getText().length() == 0) {
                        Snackbar.make(v, "Please, Enter number of days", Snackbar.LENGTH_SHORT).show();
                    } else {
                        ArrayList<String> places = new ArrayList<String>();
                        for (String string : mSelPlaces) {
                            if (string.compareTo("NA") != 0)
                                places.add(string);
                        }

                        new SearchDrivers(places).execute(PreferenceManager.getUserId(BookRideActivity.this)
                                , mCarType.get(mCarsSpinner.getSelectedItemPosition())
                                , mNoOfDays.getText().toString());

                    }
                } else {
                    new AlertDialog.Builder(BookRideActivity.this)
                            .setTitle("No Cars")
                            .setMessage("No Cars details found. Please add one")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }

            }
        });

        mPlaceGridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void showSelectionDialog() {
        boolean[] checked = new boolean[places.length];
        Arrays.fill(checked, false);

        for (String selected :
                mSelPlaces) {
            int index = Search(places, selected);
            Log.d(TAG, String.format("%s : %d", selected, index));
            if (index >= 0)
                checked[index] = true;
        }

        new AlertDialog.Builder(BookRideActivity.this)
                .setTitle("Pick Your Places")
                .setMultiChoiceItems(R.array.trips_type, mSelPlaces.size() > 0 ? checked : null
                        , new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                Log.d(TAG, "onClick: Which : " + which);
                                if (isChecked) {
                                    mSelPlaces.add(places[which]);
                                } else if (mSelPlaces.contains(places[which])) {
                                    mSelPlaces.remove(places[which]);
                                }
                            }
                        })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPlaceGridView.setText(String.format(Locale.US
                                , "Places Selected (%d)", mSelPlaces.size()));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private int Search(String[] places, String selected) {
        int position = -1;
        for (int i = 0; i < places.length; i++) {
            if (places[i].compareTo(selected) == 0) {
                position = i;
                break;
            }
        }
        return position;
    }

    private class GetCars extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;

        public GetCars() {
            progressDialog = new ProgressDialog(BookRideActivity.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
//            transactionList.setAdapter(null);
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
                Utility.ShowAlertDialog(BookRideActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");

                    if (ans.compareTo("ok") == 0) {

                        //cid,uid,brand,model,transmision,year,chasisno,carno,type,fuel
                        mCabId = new ArrayList<String>();
                        mCarBrands = new ArrayList<String>();
                        mCarModels = new ArrayList<String>();
                        mSpinList = new ArrayList<String>();
                        mCarType = new ArrayList<String>();

                        mCabId.add("NULL");
                        mCarBrands.add("NULL");
                        mCarModels.add("NULL");
                        mSpinList.add("Select Cars");
                        mCarType.add("NULL");

                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            mCabId.add(jdata.getString("data0"));
                            mCarBrands.add(jdata.getString("data2"));
                            mCarModels.add(jdata.getString("data3"));
                            mCarType.add(jdata.getString("data8"));

                            mSpinList.add(jdata.getString("data2") + " - " + jdata.getString("data3"));


                        }
                        if (mSpinList.size() > 0) {
                            ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(BookRideActivity.this
                                    , android.R.layout.simple_list_item_1, mSpinList);
                            mAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

                            mCarsSpinner.setAdapter(mAdapter);
                        }
                    } else if (ans.compareTo("false") == 0) {

                        new AlertDialog.Builder(BookRideActivity.this)
                                .setTitle("No Cars")
                                .setMessage("No Cars details found. Please add one")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: " + error);
                        Toast.makeText(BookRideActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BookRideActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class SearchDrivers extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;
        private ArrayList<String> place;

        public SearchDrivers(@NonNull ArrayList<String> places) {
            progressDialog = new ProgressDialog(BookRideActivity.this);
            progressDialog.setCancelable(false);
            this.place = places;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Finding Drivers,Please Wait..");
            progressDialog.show();
//            transactionList.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UsearchDrivers(params[0], params[1], place, params[2]);
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
                Utility.ShowAlertDialog(BookRideActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");

                    if (ans.compareTo("ok") == 0) {
                        mDrivers = new ArrayList<DriverModel>();

                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            mDrivers.add(new DriverModel(jdata.getString("data1")
                                    , jdata.getString("data2"), jdata.getString("data3")
                                    , jdata.getString("data4"), jdata.getString("data5")
                                    , jdata.getString("data6"), jdata.getString("data7")
                                    , jdata.getString("data8"), jdata.getString("data9")
                                    , jdata.getString("data10"), jdata.getString("data11")
                                    , jdata.getString("data12"), jdata.getString("data13")
                                    , jdata.getString("data0")));
                        }
                        if (mSpinList.size() > 0) {
                            if (mDriverError.getVisibility() == View.VISIBLE) {
                                mDriverError.setVisibility(View.GONE);
                            }
                            if (mDriverList.getVisibility() == View.GONE) {
                                mDriverList.setVisibility(View.VISIBLE);
                            }
                            Adapter adapter = new Adapter(BookRideActivity.this, mDrivers);
                            mDriverList.setAdapter(null);
                            mDriverList.setAdapter(adapter);
                        }
                    } else if (ans.compareTo("false") == 0) {

                        mDriverList.setAdapter(null);

                        if (mDriverError.getVisibility() == View.VISIBLE) {
                            mDriverError.setVisibility(View.GONE);
                        }
                        if (mDriverList.getVisibility() == View.GONE) {
                            mDriverList.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "onPostExecute: false");

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: " + error);
                        Toast.makeText(BookRideActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BookRideActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class Adapter extends ArrayAdapter<DriverModel> {
        private Context con;

        public Adapter(@NonNull Context context, ArrayList<DriverModel> a) {
            super(context, R.layout.transation_view_item, a);
            this.con = context;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MyViewHolder myViewHolder;
            if (convertView == null) {
                myViewHolder = new MyViewHolder();
                convertView = LayoutInflater.from(con).inflate(R.layout.item_drivers, null, true);
                myViewHolder.mItemName = convertView.findViewById(R.id.driver_name);
                myViewHolder.mItemContact = convertView.findViewById(R.id.driver_contact);
                myViewHolder.mItemHrsRate = convertView.findViewById(R.id.driver_hrs_rate);
                myViewHolder.mViewDetails = convertView.findViewById(R.id.driver_details);

                myViewHolder.mViewImage = convertView.findViewById(R.id.driver_image);
                myViewHolder.mRating = convertView.findViewById(R.id.driver_rate);
                myViewHolder.mBookRide = convertView.findViewById(R.id.driver_book);


                convertView.setTag(myViewHolder);
            } else {
                myViewHolder = (MyViewHolder) convertView.getTag();
            }

            if (mDrivers.get(position).getPhoto().length() > 20)
                myViewHolder.mViewImage.setImageBitmap(getImage(mDrivers.get(position).getPhoto()));

            myViewHolder.mItemName.setText(mDrivers.get(position).getName());
            myViewHolder.mItemContact.setText(mDrivers.get(position).getContact());
            myViewHolder.mItemHrsRate.setText(String.format("Rate %s %s/hrs", getResources().getString(R.string.currency)
                    , mDrivers.get(position).getHourprice()));
            if (mDrivers.get(position).getRating().length() > 0)
                myViewHolder.mRating.setRating(Float.parseFloat(mDrivers.get(position).getRating()));

            myViewHolder.mViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BookRideActivity.this, DriverProfileActivity.class);
                    intent.putExtra(RideBookActivity.DRIVER_ID, mDrivers.get(position).getDid());
                    intent.putExtra(RideBookActivity.DRIVER_NAME, mDrivers.get(position).getName());
                    intent.putExtra(RideBookActivity.DRIVER_PRICE, mDrivers.get(position).getHourprice());
                    intent.putExtra(RideBookActivity.DRIVER_RATING, mDrivers.get(position).getRating());
                    intent.putExtra(RideBookActivity.DRIVER_IMG, mDrivers.get(position).getPhoto());
                    intent.putExtra(RideBookActivity.CAB_ID, mCabId.get(mCarsSpinner.getSelectedItemPosition()));
                    intent.putExtra(RideBookActivity.RIDE_DAYS, mNoOfDays.getText().toString());
                    intent.putExtra(RideBookActivity.PLACES, getSelectedString());
                    startActivity(intent);
                }
            });

            myViewHolder.mBookRide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BookRideActivity.this, RideBookActivity.class);
                    intent.putExtra(RideBookActivity.DRIVER_ID, mDrivers.get(position).getDid());
                    intent.putExtra(RideBookActivity.DRIVER_NAME, mDrivers.get(position).getName());
                    intent.putExtra(RideBookActivity.DRIVER_PRICE, mDrivers.get(position).getHourprice());
                    intent.putExtra(RideBookActivity.DRIVER_RATING, mDrivers.get(position).getRating());
                    intent.putExtra(RideBookActivity.DRIVER_IMG, mDrivers.get(position).getPhoto());
                    intent.putExtra(RideBookActivity.CAB_ID, mCabId.get(mCarsSpinner.getSelectedItemPosition()));
                    intent.putExtra(RideBookActivity.RIDE_DAYS, mNoOfDays.getText().toString());
                    intent.putExtra(RideBookActivity.PLACES, getSelectedString());
                    startActivity(intent);
                }
            });


            return convertView;
        }

        private String getSelectedString() {
            String place = "";
            for (String places : mSelPlaces) {
                place = place.concat(places + ",");
            }
            return place;
        }

        private class MyViewHolder {
            private TextView mItemName, mItemContact, mItemHrsRate;
            private ImageView mViewDetails, mViewImage;
            private AppCompatButton mBookRide;
            private RatingBar mRating;
        }

        private Bitmap getImage(String Simage) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] imageBytes = baos.toByteArray();
            imageBytes = Base64.decode(Simage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }
    }
}
