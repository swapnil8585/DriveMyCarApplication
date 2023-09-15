package com.driver_hiring.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.EditText;

import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

public class CabDetailsActivity extends AppCompatActivity {
    public static final String CAB_ID = "Cab_ID";
    private static final String TAG = "CabDetails";
    private String mCab_ID;
    private CabModel cabModel;

    private ProgressDialog progressDialog;
    private EditText mEditCBrand, mEditCModel, mEditCTrans, mEditCYear, mEditCarNo, mEditC_Chasis, mEditCType, mEditCFuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_details);

        getSupportActionBar().setTitle("Cab Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCab_ID = getIntent().getStringExtra(CAB_ID);

        initViews();

        new GetCabDetails().execute(mCab_ID);

    }

    private void initViews() {
        mEditCBrand = findViewById(R.id.cab_brand);
        mEditCModel = findViewById(R.id.cab_model);
        mEditCTrans = findViewById(R.id.cab_trans);
        mEditCYear = findViewById(R.id.cab_years);
        mEditCarNo = findViewById(R.id.cab_number);
        mEditC_Chasis = findViewById(R.id.cab_chasis_no);
        mEditCType = findViewById(R.id.cab_type);
        mEditCFuel = findViewById(R.id.cab_fuel);
    }

    private void setValues() {
        mEditCBrand.setText(cabModel.getCarBrand());
        mEditCModel.setText(cabModel.getCarModel());
        mEditCTrans.setText(cabModel.getCarTransmision());
        mEditCYear.setText(cabModel.getCarYear());
        mEditCarNo.setText(cabModel.getCarNo());
        mEditC_Chasis.setText(cabModel.getCarChasisNo());
        mEditCType.setText(cabModel.getCarType());
        mEditCFuel.setText(cabModel.getCarFuel());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private class GetCabDetails extends AsyncTask<String, JSONObject, String> {

        public GetCabDetails() {
            progressDialog = new ProgressDialog(CabDetailsActivity.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait");
            progressDialog.dismiss();

        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                return new JSONParse().Parse(cabHiringAPI.DgetCarDetails(strings[0]));
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.setMessage("");
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(CabDetailsActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject object = new JSONObject(s);
                    String exp = object.getString("status");
                    if (exp.compareTo("ok") == 0) {

                        //
                        JSONArray result = object.getJSONArray("Data");
                        JSONObject res = result.getJSONObject(0);

                        cabModel = new CabModel(res.getString("data0"), res.getString("data1")
                                , res.getString("data2"), res.getString("data3")
                                , res.getString("data4"), res.getString("data5")
                                , res.getString("data6"), res.getString("data7")
                                , res.getString("data8"), res.getString("data9"));

                        setValues();

                    } else if (exp.compareTo("no") == 0) {

                        Utility.ShowAlertDialog(getApplicationContext(), "No Cab !"
                                , "Could not find cab details, Try again", false);

                    } else {

                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }
    }

    public class CabModel {
        String cabId, userId, carBrand, carModel, carTransmision, carYear, carChasisNo, carNo, carType, carFuel;

        public CabModel(String cabId, String userId, String carBrand, String carModel, String carTransmision
                , String carYear, String carChasisNo, String carNo, String carType, String carFuel) {
            this.cabId = cabId;
            this.userId = userId;
            this.carBrand = carBrand;
            this.carModel = carModel;
            this.carTransmision = carTransmision;
            this.carYear = carYear;
            this.carChasisNo = carChasisNo;
            this.carNo = carNo;
            this.carType = carType;
            this.carFuel = carFuel;
        }

        public String getCabId() {
            return cabId;
        }

        public void setCabId(String cabId) {
            this.cabId = cabId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCarBrand() {
            return carBrand;
        }

        public void setCarBrand(String carBrand) {
            this.carBrand = carBrand;
        }

        public String getCarModel() {
            return carModel;
        }

        public void setCarModel(String carModel) {
            this.carModel = carModel;
        }

        public String getCarTransmision() {
            return carTransmision;
        }

        public void setCarTransmision(String carTransmision) {
            this.carTransmision = carTransmision;
        }

        public String getCarYear() {
            return carYear;
        }

        public void setCarYear(String carYear) {
            this.carYear = carYear;
        }

        public String getCarChasisNo() {
            return carChasisNo;
        }

        public void setCarChasisNo(String carChasisNo) {
            this.carChasisNo = carChasisNo;
        }

        public String getCarNo() {
            return carNo;
        }

        public void setCarNo(String carNo) {
            this.carNo = carNo;
        }

        public String getCarType() {
            return carType;
        }

        public void setCarType(String carType) {
            this.carType = carType;
        }

        public String getCarFuel() {
            return carFuel;
        }

        public void setCarFuel(String carFuel) {
            this.carFuel = carFuel;
        }
    }
}
