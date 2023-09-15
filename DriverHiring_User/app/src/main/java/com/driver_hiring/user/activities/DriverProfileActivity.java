package com.driver_hiring.user.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.driver_hiring.user.R;
import com.driver_hiring.user.RideBookActivity;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DriverProfileActivity extends AppCompatActivity {
    public static final String DRIVER_ID = "DriverId";
    private static final String TAG = "Update";

    //private EditText dName, dContact, dEmail, dDob, dAddress, dCity;
    //private ImageView patientImage;
    private EditText dName, dContact, dEmail, dDob, dAddress, dCity, dState, dPincode, dTotExp, dHoursPrice;
    private RadioButton dMaleBtn, dFemaleBtn;
    private LinearLayout mainLayout;
    private DriverModel driverModel;

    private Button mBookRide;

    private ImageView personal, changeImage;

    private Calendar SDate;
    private ProgressDialog progressDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private String driverID, carsId, mPlaceTypes, driverName, driverImg, driverDays, driverPrice, driverRating;

    private String UID = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getViewId();

        UID = getIntent().getStringExtra(DRIVER_ID);

        if (UID != null)
            new GetDriverDetails().execute(UID);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mBookRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverProfileActivity.this, RideBookActivity.class);
                intent.putExtra(RideBookActivity.DRIVER_ID, driverModel.getDriverId());
                intent.putExtra(RideBookActivity.DRIVER_NAME, driverModel.getName());
                intent.putExtra(RideBookActivity.DRIVER_PRICE, driverModel.getHourPrice());
                intent.putExtra(RideBookActivity.DRIVER_RATING, driverRating);
                intent.putExtra(RideBookActivity.DRIVER_IMG, driverModel.getPhoto());
                intent.putExtra(RideBookActivity.CAB_ID, carsId);
                intent.putExtra(RideBookActivity.RIDE_DAYS, driverDays);
                intent.putExtra(RideBookActivity.PLACES, mPlaceTypes);
                startActivity(intent);
            }
        });
    }

    private void getViewId() {

        mainLayout = findViewById(R.id.mainContainer);
        //General Student Entry View ID's
        dName = (EditText) findViewById(R.id.uRegName);
        dContact = (EditText) findViewById(R.id.uRegContact);
        dEmail = (EditText) findViewById(R.id.uRegEmail);
        dDob = (EditText) findViewById(R.id.uRegDob);
        dMaleBtn = (RadioButton) findViewById(R.id.pro_male);
        dFemaleBtn = (RadioButton) findViewById(R.id.pro_female);
        dAddress = (EditText) findViewById(R.id.uRegAddr);
        dCity = (EditText) findViewById(R.id.uRegCity);
        dState = (EditText) findViewById(R.id.uRegState);
        dPincode = (EditText) findViewById(R.id.uRegPin);

        dTotExp = (EditText) findViewById(R.id.uRegTotExp);
        dHoursPrice = (EditText) findViewById(R.id.uRegHrsChg);

        personal = (ImageView) findViewById(R.id.driverProImage);
        changeImage = (ImageView) findViewById(R.id.editImage);
        mBookRide = (Button) findViewById(R.id.uRegisterButton);

        mBookRide.setText("Book Ride");
//        mBookRide.setVisibility(View.GONE);

        SDate = Calendar.getInstance();

        disableView();

    }

    private void disableView() {

        if (changeImage.getVisibility() == View.VISIBLE)
            changeImage.setVisibility(View.GONE);

        dName.setEnabled(false);
        dContact.setEnabled(false);
        dEmail.setEnabled(false);
        dDob.setEnabled(false);
        dMaleBtn.setEnabled(true);
        dFemaleBtn.setEnabled(true);
        dAddress.setEnabled(false);
        dCity.setEnabled(false);
        dState.setEnabled(false);
        dHoursPrice.setEnabled(false);
        dTotExp.setEnabled(false);
        personal.setEnabled(false);

    }

    private void setValues() {

        driverName = getIntent().getStringExtra(RideBookActivity.DRIVER_NAME);
        driverImg = getIntent().getStringExtra(RideBookActivity.DRIVER_IMG);
        driverRating = getIntent().getStringExtra(RideBookActivity.DRIVER_RATING);
        driverPrice = getIntent().getStringExtra(RideBookActivity.DRIVER_PRICE);
        driverDays = getIntent().getStringExtra(RideBookActivity.RIDE_DAYS);

        driverID = getIntent().getStringExtra(DRIVER_ID);
        mPlaceTypes = getIntent().getStringExtra(RideBookActivity.PLACES);
        carsId = getIntent().getStringExtra(RideBookActivity.CAB_ID);


        if (driverModel.getPhoto().compareTo("") != 0) {
            personal.setImageBitmap(getImage(driverModel.getPhoto()));
        }

        dName.setText(driverModel.getName());
        dContact.setText(driverModel.getContact());
        dEmail.setText(driverModel.getEmail());
        dDob.setText("Date of Birth : " + driverModel.getDOB());
        if (driverModel.getGender().compareTo("Male") == 0) {
            dMaleBtn.setChecked(true);
        } else {
            dFemaleBtn.setChecked(true);
        }
        dAddress.setText(driverModel.getAddress());
        dCity.setText(driverModel.getCity());
        dState.setText(driverModel.getState());
        dPincode.setText(driverModel.getPincode());

        dHoursPrice.setText(driverModel.getHourPrice());
        dTotExp.setText(driverModel.getTotExperience());

        String[] dob = driverModel.getDOB().split("/");
        SDate.set(Integer.parseInt(dob[0]), Integer.parseInt(dob[1]), Integer.parseInt(dob[2]));

        progressDialog.setMessage("");
        progressDialog.dismiss();

        if (mainLayout.getVisibility() == View.GONE) {
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getImage(String Simage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(Simage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public void onTripClick(View view) {
        Intent intent = new Intent(this, DriverTripsActivity.class);
        intent.putExtra(DRIVER_ID, driverModel.getDriverId());
        startActivity(intent);
    }

    public void onExperienceClick(View view) {
        Intent intent = new Intent(this, DrivingExpActivity.class);
        intent.putExtra(DRIVER_ID, driverModel.getDriverId());
        startActivity(intent);
    }

    public void onDocumentClick(View view) {
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.putExtra(DRIVER_ID, driverModel.getDriverId());
        startActivity(intent);
    }

    public class GetDriverDetails extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "Details";

        private GetDriverDetails() {
            progressDialog = new ProgressDialog(DriverProfileActivity.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            if (mainLayout.getVisibility() == View.VISIBLE) {
                mainLayout.setVisibility(View.GONE);
            }

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            CabHiringAPI restAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = restAPI.DgetProfile(strings[0]);
                JSONParse jsonparse = new JSONParse();
                answer = jsonparse.Parse(jsonObject);
            } catch (Exception e) {
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String exp = "";
            Log.d(TAG, "onPostBack : " + s);
            if (Utility.checkConnection(s)) {
                progressDialog.setMessage("");
                progressDialog.dismiss();

                if (mainLayout.getVisibility() == View.GONE) {
                    mainLayout.setVisibility(View.VISIBLE);
                }
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DriverProfileActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject object = new JSONObject(s);
                    exp = object.getString("status");
                    if (exp.compareTo("ok") == 0) {

                        JSONArray result = object.getJSONArray("Data");
                        JSONObject res = result.getJSONObject(0);

                        // 0     1   2     3     4     5        6        7        8      9     10       11      12
                        //did, photo,name,dob,gender, email, contact, totexp, address, city, state, pincode, hourprice

                        //String driverId, String name, String email, String contact, String DOB, String photo
                        // totExp, String address, String city, String state, String pincode, String hourPrice
                        driverModel = new DriverModel(
                                UID,
                                res.getString("data2"),
                                res.getString("data5"),
                                res.getString("data6"),
                                res.getString("data3"),
                                res.getString("data1"),
                                res.getString("data7"),
                                res.getString("data8"),
                                res.getString("data9"),
                                res.getString("data10"),
                                res.getString("data11"),
                                res.getString("data12"),
                                res.getString("data4"));

                        setValues();

                    } else if (exp.compareTo("no") == 0) {
                        progressDialog.setMessage("");
                        progressDialog.dismiss();

                        if (mainLayout.getVisibility() == View.GONE) {
                            mainLayout.setVisibility(View.VISIBLE);
                        }
                        Snackbar.make(mBookRide, "No Such User Exists", Snackbar.LENGTH_SHORT).show();
                    } else {
                        progressDialog.setMessage("");
                        progressDialog.dismiss();

                        if (mainLayout.getVisibility() == View.GONE) {
                            mainLayout.setVisibility(View.VISIBLE);
                        }
                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    progressDialog.setMessage("");
                    progressDialog.dismiss();

                    if (mainLayout.getVisibility() == View.GONE) {
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    public class DriverModel {

        //totexp, address, city, state, pincode, hourprice
        String DriverId, Name, Email, Contact, DOB, Photo, TotExperience, Address, City, State, Pincode, HourPrice, Gender;

        public DriverModel(String driverId, String name, String email, String contact, String DOB, String photo
                , String totExp, String address, String city, String state, String pincode, String hourPrice, String gender) {
            setDriverId(driverId);
            setName(name);
            setEmail(email);
            setContact(contact);
            setDOB(DOB);
            setPhoto(photo);
            setTotExperience(totExp);
            setAddress(address);
            setCity(city);
            setState(state);
            setPincode(pincode);
            setHourPrice(hourPrice);
            setGender(gender);

        }

        public String getDriverId() {
            return DriverId;
        }

        public void setDriverId(String driverId) {
            DriverId = driverId;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String email) {
            Email = email;
        }

        public String getContact() {
            return Contact;
        }

        public void setContact(String contact) {
            Contact = contact;
        }

        public String getDOB() {
            return DOB;
        }

        public void setDOB(String DOB) {
            this.DOB = DOB;
        }

        public String getPhoto() {
            return Photo;
        }

        public void setPhoto(String photo) {
            Photo = photo;
        }

        public String getTotExperience() {
            return TotExperience;
        }

        public void setTotExperience(String totExperience) {
            TotExperience = totExperience;
        }

        public String getAddress() {
            return Address;
        }

        public void setAddress(String address) {
            Address = address;
        }

        public String getCity() {
            return City;
        }

        public void setCity(String city) {
            City = city;
        }

        public String getState() {
            return State;
        }

        public void setState(String state) {
            State = state;
        }

        public String getPincode() {
            return Pincode;
        }

        public void setPincode(String pincode) {
            Pincode = pincode;
        }

        public String getHourPrice() {
            return HourPrice;
        }

        public void setHourPrice(String hourPrice) {
            HourPrice = hourPrice;
        }

        public String getGender() {
            return Gender;
        }

        public void setGender(String gender) {
            Gender = gender;
        }
    }

}
