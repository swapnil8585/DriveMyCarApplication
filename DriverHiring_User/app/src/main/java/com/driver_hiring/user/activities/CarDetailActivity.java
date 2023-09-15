package com.driver_hiring.user.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.driver_hiring.user.R;
import com.driver_hiring.user.models.CarsModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CarDetailActivity extends AppCompatActivity {
    public static final String CAR_DETAILS = "CarDetails";
    private EditText mEditBrand, mEditModel, mEditRegNo, mEditChasis, mEditYear;
    private Spinner mCarType, mCarTrans, mCarFuel;
    private AppCompatButton mAddCar;
    private CarsModel mCarsDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        getSupportActionBar().setTitle("Car Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initViews();

        setValues();


    }

    private void setValues() {
        Bundle bundle;
        if (getIntent().getExtras() != null) {
            bundle = getIntent().getBundleExtra(CAR_DETAILS);
            String mRideString = bundle.getString(CAR_DETAILS);
            Log.d("MAIN", "onCreate: " + mRideString);
            mCarsDetails = (CarsModel) new Gson().fromJson(mRideString, CarsModel.class);
        }

        mEditBrand.setText(mCarsDetails.getBrand());
        mEditModel.setText(mCarsDetails.getModel());
        mEditRegNo.setText(mCarsDetails.getCarno());
        mEditChasis.setText(mCarsDetails.getChasisno());
        mEditYear.setText(mCarsDetails.getYear());

        List<String> items = Arrays.asList(getResources().getStringArray(R.array.Car_Type));
        mCarType.setSelection(items.indexOf(mCarsDetails.getType()));

        items = Arrays.asList(getResources().getStringArray(R.array.Transmission_Type));
        mCarTrans.setSelection(items.indexOf(mCarsDetails.getTransmision()));

        items = Arrays.asList(getResources().getStringArray(R.array.Fuel_Type));
        mCarFuel.setSelection(items.indexOf(mCarsDetails.getFuel()));

        setEnabled(false);
    }

    private void setEnabled(boolean enabled) {
        mEditBrand.setEnabled(enabled);
        mEditModel.setEnabled(enabled);
        mEditRegNo.setEnabled(enabled);
        mEditChasis.setEnabled(enabled);
        mEditYear.setEnabled(enabled);

        mCarType.setEnabled(enabled);
        mCarTrans.setEnabled(enabled);
        mCarFuel.setEnabled(enabled);
    }

    private void initViews() {
        mEditBrand = findViewById(R.id.car_brand);
        mEditModel = findViewById(R.id.car_model);
        mEditRegNo = findViewById(R.id.car_reg);
        mEditChasis = findViewById(R.id.car_chasis_no);
        mEditYear = findViewById(R.id.car_year);

        mCarType = findViewById(R.id.car_model_type);
        mCarTrans = findViewById(R.id.car_trans_type);
        mCarFuel = findViewById(R.id.car_fuel_type);

        mAddCar = findViewById(R.id.car_add_car);
        mAddCar.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_delete) {
            deleteCar();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCar() {
        new AlertDialog.Builder(CarDetailActivity.this)
                .setTitle("Delete !")
                .setMessage("Are you sure you want to delete this car and its details?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        new DeleteCarTask().execute(mCarsDetails.getCid());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_menu, menu);
        return true;
    }

    public class DeleteCarTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "AddCarTask";
        private ProgressDialog progressDialog;

        private DeleteCarTask() {
            progressDialog = new ProgressDialog(CarDetailActivity.this);
            progressDialog.setCancelable(false);

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
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UdeleteCar(params[0]);
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
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(CarDetailActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(CarDetailActivity.this, "Car removed successfully"
                                , Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    } else {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }

                } catch (Exception e) {
                    Toast.makeText(CarDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
}
