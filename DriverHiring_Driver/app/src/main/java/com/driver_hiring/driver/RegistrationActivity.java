package com.driver_hiring.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RegistrationActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int READ_STORAGE_PERMISSION = 101;
    public static final int GALLERY_IMAGE_REQUEST = 102;
    public static final String FILE_NAME = "temp.jpg";
    public static final int CAMERA_REQUEST_CODE = 103;
    public static final int MAX_DIMENSION = 500;
    private static final String TAG = "Registration";

    private EditText edtName, edtContact, edtEmail, edtPrice, edtPass, edtAddress, edtCity, edtState, edtPincode, edtTotExperience;
    private TextView edtDOB;
    private CardView uDateOfBirth;
    private ImageView proImage;
    private RadioButton rdoMale, rdoFemale;
    private AppCompatButton reg_Submit;
    private String isImageChoosen = "";

    /*
     * Date Of Birth
     */
    private DatePickerDialog dOBPicker;
    private Calendar mCalender;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalender.set(Calendar.YEAR, year);
            mCalender.set(Calendar.MONTH, month);
            mCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            edtDOB.setText(dateFormat.format(mCalender.getTimeInMillis()));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getSupportActionBar().setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCalender = Calendar.getInstance();

        init();

    }

    private void init() {
        edtName = (EditText) findViewById(R.id.reg_personname);
        edtDOB = (TextView) findViewById(R.id.reg_dob);
        edtContact = (EditText) findViewById(R.id.reg_personcontact);
        edtEmail = (EditText) findViewById(R.id.reg_personemail);
        edtPrice = (EditText) findViewById(R.id.reg_price_hrs);
        edtPass = (EditText) findViewById(R.id.reg_password);

        edtAddress = (EditText) findViewById(R.id.reg_addressline1);
        edtCity = (EditText) findViewById(R.id.reg_addressline2);
        edtState = (EditText) findViewById(R.id.reg_addressline3);
        edtPincode = (EditText) findViewById(R.id.reg_addressline4);
        edtTotExperience = (EditText) findViewById(R.id.reg_tot_exp);

        proImage = (ImageView) findViewById(R.id.reg_profile_img);
        rdoMale = (RadioButton) findViewById(R.id.reg_male);
        rdoFemale = (RadioButton) findViewById(R.id.reg_female);
        reg_Submit = (AppCompatButton) findViewById(R.id.reg_submit);
    }

    @Override
    protected void onStart() {
        super.onStart();

        proImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageUploadClick(v);
            }
        });

        edtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dOBPicker == null) {
                    dOBPicker = new DatePickerDialog(RegistrationActivity.this, onDateSetListener,
                            mCalender.get(Calendar.YEAR), mCalender.get(Calendar.MONTH)
                            , mCalender.get(Calendar.DAY_OF_MONTH));

                    dOBPicker.show();
                } else {
                    if (!dOBPicker.isShowing()) {
                        dOBPicker.show();
                    }
                }
            }
        });

        reg_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (isImageChoosen != null && isImageChoosen.length() > 1) {
                        Log.d(TAG, "onClick: Validated");
                        //String photo,String name,String dob,String gender,String email,String contact
                        // ,String totexp,String address,String city,String state,String pincode,String hourprice
                        // ,String pass
                        Log.d(TAG, String.format("%s %s, %s, %s\n,%s,%s,%s\n,%s, %s, %s\n,%s, %s, %s"
                                , isImageChoosen.length() + "", edtName.getText().toString()
                                , edtDOB.getText().toString(), (rdoMale.isChecked() ? "Male" : "Female")
                                , edtEmail.getText().toString(), edtContact.getText().toString()
                                , edtTotExperience.getText().toString(), edtAddress.getText().toString()
                                , edtCity.getText().toString(), edtState.getText().toString()
                                , edtPincode.getText().toString(), edtPrice.getText().toString()
                                , edtPass.getText().toString()));

                        new RegistrationTask().execute(isImageChoosen, edtName.getText().toString()
                                , edtDOB.getText().toString(), (rdoMale.isChecked() ? "Male" : "Female")
                                , edtEmail.getText().toString(), edtContact.getText().toString()
                                , edtTotExperience.getText().toString(), edtAddress.getText().toString()
                                , edtCity.getText().toString(), edtState.getText().toString()
                                , edtPincode.getText().toString(), edtPrice.getText().toString()
                                , edtPass.getText().toString());
                    } else {
                        Snackbar.make(v, "Please Capture or Choose Image", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });


    }

    public void onImageUploadClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder
                .setMessage(R.string.dialog_select_prompt)
                .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RegistrationActivity.this.startGalleryChooser();
                    }
                })
                .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RegistrationActivity.this.startCamera();
                    }
                });
        builder.create().show();
    }

    public void startGalleryChooser() {
        isImageChoosen = "";
        if (ContextCompat.checkSelfPermission(RegistrationActivity.this
                , Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        } else {
            ActivityCompat.requestPermissions(RegistrationActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);
        }
    }

    public void startCamera() {
        isImageChoosen = "";
        if (ContextCompat.checkSelfPermission(RegistrationActivity.this
                , Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(RegistrationActivity.this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } else {
            ActivityCompat.requestPermissions(RegistrationActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    private boolean validate() {
        boolean valid = true;
        View[] editText = new View[]{edtName, edtDOB, edtContact, edtContact, edtEmail, edtEmail
                , edtPrice, edtTotExperience, edtPass, edtAddress, edtCity, edtState, edtPincode};
        String[] messages = getResources().getStringArray(R.array.Edit_Validation);
        for (int position = 0; position < editText.length; position++) {
            Log.d(TAG, "validate: position " + position);
            if (position == 2) {
                if (!Patterns.PHONE.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(reg_Submit, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else if (position == 4) {
                if (!Patterns.EMAIL_ADDRESS.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(reg_Submit, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else {
                if (editText[position] instanceof EditText) {
                    if (((EditText) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(reg_Submit, messages[position], Snackbar.LENGTH_SHORT)
                                .show();
                        editText[position].requestFocus();
                        valid = false;
                        break;
                    }
                } else {
                    if (((TextView) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(reg_Submit, messages[position], Snackbar.LENGTH_SHORT)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    ActivityCompat.requestPermissions(RegistrationActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
            }
            break;
            case READ_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryChooser();
                } else {
                    ActivityCompat.requestPermissions(RegistrationActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);

                }

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_IMAGE_REQUEST:
                    if (data != null) {
                        if (data.getData() != null) {
                            setImg(data.getData());
                        }
                    }
                    break;
                case CAMERA_REQUEST_CODE:
                    Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
                    if (photoUri != null) {
                        setImg(photoUri);
                    }
                    break;
            }
        } else {
            Log.d(TAG, "onActivityResult: No Image");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setImg(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);
                proImage.setImageBitmap(bitmap);
                isImageChoosen = Utility.encodeBitmap(bitmap);
                //upload directly
                //updateProfile();
            } catch (IOException e) {
                Log.d("TAG", "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "Selecting image failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("TAG", "Image picker gave us a null image.");
            Toast.makeText(this, "Error selecting an image  ", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public class RegistrationTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "RegisterTask";
        private ProgressDialog progressDialog;

        private RegistrationTask() {
            progressDialog = new ProgressDialog(RegistrationActivity.this);
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
                JSONObject json = api.Dregister(params[0], params[1], params[2], params[3], params[4], params[5],
                        params[6], params[7], params[8], params[9], params[10], params[11], params[12]);
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
                Utility.ShowAlertDialog(RegistrationActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {

                        Utility.ShowAlertDialog(RegistrationActivity.this, "User Exists"
                                , "User already exists, Please try with another email", false);

                    } else if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful" +
                                ", Please login to Continue", Toast.LENGTH_LONG).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RegistrationActivity.this.finish();
                            }
                        }, 100);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
