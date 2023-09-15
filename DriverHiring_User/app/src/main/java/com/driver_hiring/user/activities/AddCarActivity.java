package com.driver_hiring.user.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

public class AddCarActivity extends AppCompatActivity {
    private static final String TAG = "Add_Car";

    private EditText mEditBrand, mEditModel, mEditRegNo, mEditChasis, mEditYear;
    private Spinner mCarType, mCarTrans, mCarFuel;
    private AppCompatButton mAddCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        getSupportActionBar().setTitle("Add Car");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initViews();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate(mEditBrand, mEditModel, mEditRegNo, mEditChasis, mEditYear)) {
                    if (mCarType.getSelectedItemPosition() == 0) {
                        Snackbar.make(v, "Please choose car type", Snackbar.LENGTH_SHORT).show();
                    } else if (mCarTrans.getSelectedItemPosition() == 0) {
                        Snackbar.make(v, "Please choose transmission type", Snackbar.LENGTH_SHORT).show();
                    } else if (mCarFuel.getSelectedItemPosition() == 0) {
                        Snackbar.make(v, "Please choose car's fuel type", Snackbar.LENGTH_SHORT).show();
                    } else {
                        //string uid, string brand, string model, string transmission, string year
                        // , string chasisno, string carno, string type, string fuel
                        Log.d(TAG, String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
                                PreferenceManager.getUserId(AddCarActivity.this)
                                , mEditBrand.getText().toString(), mEditModel.getText().toString()
                                , mCarTrans.getSelectedItem().toString(), mEditYear.getText().toString()
                                , mEditChasis.getText().toString(), mEditRegNo.getText().toString()
                                , mCarType.getSelectedItem().toString(), mCarFuel.getSelectedItem().toString()));
                        new AddCarTask().execute(PreferenceManager.getUserId(AddCarActivity.this)
                                , mEditBrand.getText().toString(), mEditModel.getText().toString()
                                , mCarTrans.getSelectedItem().toString(), mEditYear.getText().toString()
                                , mEditChasis.getText().toString(), mEditRegNo.getText().toString()
                                , mCarType.getSelectedItem().toString(), mCarFuel.getSelectedItem().toString());
                    }
                }
            }
        });
    }

    private boolean validate(EditText... editTexts) {
        String format = "Please, enter %s";
        String[] msg = new String[]{"brand name", "model name", "registration number", "chasis number"
                , "model year"};
        for (int i = 0; i < editTexts.length; i++) {
            if (editTexts[i].getText().toString().length() == 0) {
                Snackbar.make(mAddCar, String.format(format, msg[i]), Snackbar.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
        return true;
    }

    public class AddCarTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "AddCarTask";
        private ProgressDialog progressDialog;

        private AddCarTask() {
            progressDialog = new ProgressDialog(AddCarActivity.this);
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
                JSONObject json = api.UaddCar(params[0], params[1], params[2], params[3], params[4], params[5]
                        , params[6], params[7], params[8]);
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
                Utility.ShowAlertDialog(AddCarActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {
                        Utility.ShowAlertDialog(AddCarActivity.this, "Already"
                                , "A car is already with same details", false);

                    } else if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(AddCarActivity.this, "Car added successfully"
                                , Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                } catch (Exception e) {
                    Toast.makeText(AddCarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
}
