package com.driver_hiring.driver;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.driver_hiring.driver.fragments.DocumentActivity;
import com.driver_hiring.driver.fragments.DrivingExpActivity;
import com.driver_hiring.driver.fragments.DriverTripsActivity;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.driver.helper.ImageHelper;
import com.driver_hiring.driver.helper.ImagePath_MarshMallow;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DriverProfileActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CODE = 111;
    public static final int GALLERY_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 876;
    public static final int ACTION_CHOOSE_IMAGE = 121;
    public static final String DRIVER_ID = "DriverId";
    private static final String TAG = "Update";

    //private EditText dName, dContact, dEmail, dDob, dAddress, dCity;
    //private ImageView patientImage;
    private EditText dName, dContact, dEmail, dDob, dAddress, dCity, dState, dPincode, dTotExp, dHoursPrice;
    private RadioButton dMaleBtn, dFemaleBtn;
    private LinearLayout mainLayout;
    private DriverModel driverModel;

    private Button sRegister;

    private ImageView personal, changeImage;
    private DatePickerDialog DateOfBirth;

    private Calendar SDate;
    private ProgressDialog progressDialog;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    private String UID = "";
    private boolean update = true;
    private Uri fileUri;
    private String pic = "";
    private ArrayList<String> cid, type;
    private boolean isEditTable = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getViewId();

        UID = PreferenceManager.getUserId(DriverProfileActivity.this);

        if (UID != null)
            new GetDriverDetails().execute(UID);

        dDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateOfBirth = new DatePickerDialog(DriverProfileActivity.this, R.style.DialogTheme, getDatePicker, SDate.get(Calendar.YEAR),
                        SDate.get(Calendar.MONTH), SDate.get(Calendar.DAY_OF_MONTH));
                DateOfBirth.getDatePicker().setMaxDate(SDate.getTimeInMillis());
                if (!DateOfBirth.isShowing()) {
                    DateOfBirth.show();
                }
            }
        });

        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pic = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverProfileActivity.this);
                builder.setTitle("Choose Action");
                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (weHavePermission()) {
                            startGallery();
                        } else {
                            requestPermission(GALLERY_PERMISSION_CODE);
                        }
                    }
                });

                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (weHavePermission()) {
                            try {
                                startCamera();
                            } catch (Exception e) {
                                Toast.makeText(DriverProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            requestPermission(PERMISSION_REQUEST_CODE);
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void startGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture")
                , ACTION_CHOOSE_IMAGE);
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        sRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditTable) {
                    isEditTable = true;
                    sRegister.setText("Update Details");
                    enableView();
                } else {
                    isEditTable = false;
                    if (validate()) {
                        disableView();
                        String imageInBase64 = "";

                        if (pic != null && pic.length() > 5) {
                            imageInBase64 = pic;
                        } else {
                            imageInBase64 = driverModel.getPhoto();
                        }

                        Log.d(TAG, String.format("%s %s, %s, %s\n,%s,%s,%s\n,%s, %s, %s\n,%s, %s"
                                , imageInBase64.length() + "", dName.getText().toString()
                                , dDob.getText().toString(), (dMaleBtn.isChecked() ? "Male" : "Female")
                                , dEmail.getText().toString(), dContact.getText().toString()
                                , dTotExp.getText().toString(), dAddress.getText().toString()
                                , dCity.getText().toString(), dState.getText().toString()
                                , dPincode.getText().toString(), dHoursPrice.getText().toString()));
                        //String did,String photo,String name,String dob,String gender,String email,String contact
                        // ,String totexp,String address,String city,String state,String pincode,String hourprice
                        new UpdateProfileTask().execute(
                                PreferenceManager.getUserId(DriverProfileActivity.this)
                                , imageInBase64, dName.getText().toString()
                                , dateFormat.format(SDate.getTimeInMillis()), (dMaleBtn.isChecked() ? "Male" : "Female")
                                , dEmail.getText().toString(), dContact.getText().toString()
                                , dTotExp.getText().toString(), dAddress.getText().toString()
                                , dCity.getText().toString(), dState.getText().toString()
                                , dPincode.getText().toString(), dHoursPrice.getText().toString());

                        sRegister.setText("Edit Details");
                    }
                }
            }
        });
    }

    DatePickerDialog.OnDateSetListener getDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            SDate.set(Calendar.YEAR, year);
            SDate.set(Calendar.MONTH, month);
            SDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            dDob.setText(dateFormat.format(SDate.getTimeInMillis()));
        }
    };

    private boolean validate() {
        boolean valid = true;
        View[] editText = new View[]{dName, dDob, dContact, dContact, dEmail, dEmail
                , dHoursPrice, dTotExp, dAddress, dCity, dState, dPincode};
        String[] messages = getResources().getStringArray(R.array.Edit_Validation);
        for (int position = 0; position < editText.length; position++) {
            Log.d(TAG, "validate: position " + position);
            if (position == 2) {
                if (!Patterns.PHONE.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(sRegister, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else if (position == 4) {
                if (!Patterns.EMAIL_ADDRESS.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(sRegister, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else {
                if (editText[position] instanceof EditText) {
                    if (((EditText) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(sRegister, messages[position], Snackbar.LENGTH_SHORT)
                                .show();
                        editText[position].requestFocus();
                        valid = false;
                        break;
                    }
                } else {
                    if (((TextView) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(sRegister, messages[position], Snackbar.LENGTH_SHORT)
                                .show();
                        editText[position].requestFocus();
                        valid = false;
                        break;
                    }
                }

            }
        }
        return valid;
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
        sRegister = (Button) findViewById(R.id.uRegisterButton);

        sRegister.setText("Edit Details");
//        sRegister.setVisibility(View.GONE);

        SDate = Calendar.getInstance();

        disableView();

    }

    private void enableView() {
        if (changeImage.getVisibility() == View.GONE)
            changeImage.setVisibility(View.VISIBLE);

        dName.setEnabled(true);
        dContact.setEnabled(true);
        dEmail.setEnabled(true);
        dDob.setEnabled(true);
        dMaleBtn.setEnabled(true);
        dFemaleBtn.setEnabled(true);
        dAddress.setEnabled(true);
        dCity.setEnabled(true);
        dState.setEnabled(true);
        dTotExp.setEnabled(true);
        dHoursPrice.setEnabled(true);
        personal.setEnabled(true);

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

        if (driverModel.getPhoto().compareTo("") != 0) {
            pic = driverModel.getPhoto();
            personal.setImageBitmap(getImage(driverModel.getPhoto()));
        } else {
            personal.setImageDrawable(getResources().getDrawable(R.drawable.ic_icon_user));
        }

        dName.setText(driverModel.getName());
        dContact.setText(driverModel.getContact());
        dEmail.setText(driverModel.getEmail());

        dDob.setText("");
        Log.d(TAG, "setValues: Length" + dDob.getText().toString().length());
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
        startActivity(new Intent(this, DriverTripsActivity.class));
    }

    public void onExperienceClick(View view) {
        startActivity(new Intent(this, DrivingExpActivity.class));
    }

    public void onDocumentClick(View view) {
        startActivity(new Intent(this, DocumentActivity.class));
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
                        Snackbar.make(sRegister, "No Such User Exists", Snackbar.LENGTH_SHORT).show();
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

    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(DriverProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(DriverProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                ContextCompat.checkSelfPermission(DriverProfileActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int PERMISSION_REQUEST_CODE) {
        ActivityCompat.requestPermissions(DriverProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermission(PERMISSION_REQUEST_CODE);
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery();
            } else {
                requestPermission(GALLERY_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            try {
                personal.setImageBitmap(null);
                Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri, DriverProfileActivity.this.getContentResolver());
                if (mBitmap != null) {
//                    img.setImageBitmap(mBitmap);
                    String getImageUrl;
                    getImageUrl = ImagePath_MarshMallow.getPath(DriverProfileActivity.this, fileUri);
                    Bitmap bt = RotateImg(getImageUrl, mBitmap);
                    personal.setImageBitmap(bt);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bt.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] bimg = stream.toByteArray();
                    pic = "";
                    pic = Base64.encodeToString(bimg, Base64.DEFAULT);
                } else {
                    pic = driverModel.getPhoto();
                    personal.setImageBitmap(getImage(driverModel.getPhoto()));
                }
            } catch (Exception e) {
                pic = driverModel.getPhoto();
                personal.setImageBitmap(getImage(driverModel.getPhoto()));
            }
        } else if (requestCode == ACTION_CHOOSE_IMAGE) {
            try {
                personal.setImageBitmap(null);
                Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
                if (mBitmap != null) {
//                    img.setImageBitmap(mBitmap);
                    ByteArrayOutputStream stream = null;
                    stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                    personal.setImageBitmap(mBitmap);
                    byte[] bimg = stream.toByteArray();
                    pic = Base64.encodeToString(bimg, Base64.DEFAULT);
                } else {
                    pic = driverModel.getPhoto();
                    personal.setImageBitmap(getImage(driverModel.getPhoto()));
                }
            } catch (Exception e) {
                pic = driverModel.getPhoto();
                personal.setImageBitmap(getImage(driverModel.getPhoto()));
            }
        }
    }

    public Bitmap RotateImg(String path, Bitmap bt) {
        try {
            int rotate = 0;
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                return Bitmap.createBitmap(bt, 0, 0, bt.getWidth(), bt.getHeight(), matrix, true);
            } else {
                return bt;
            }
        } catch (Exception e) {
            return bt;
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

    private class UpdateProfileTask extends AsyncTask<String, JSONObject, String> {

        public UpdateProfileTask() {
            progressDialog = new ProgressDialog(DriverProfileActivity.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Updating Profile..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = cabHiringAPI.DupdateProfile(strings[0], strings[1], strings[2], strings[3], strings[4]
                        , strings[5], strings[6], strings[7], strings[8], strings[9], strings[10]
                        , strings[11], strings[12]);
                return new JSONParse().Parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DriverProfileActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {

                        setValues();

                        Utility.ShowAlertDialog(DriverProfileActivity.this, "User Exists"
                                , "User already exists, Please try with another email", false);

                    } else if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(DriverProfileActivity.this
                                , "Profile Updated Successfully"
                                , Toast.LENGTH_LONG).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
