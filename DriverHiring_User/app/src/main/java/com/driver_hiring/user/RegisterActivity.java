package com.driver_hiring.user;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;

import com.driver_hiring.user.webservices.CabHiringAPI;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "Register";
    private TextView txtLogin;
    private Button btnRegister;
    private EditText edtName, edtPhone, edtEmail, edtPass, edtAddress, edtCity, edtState, edtPincode;
    private TextView edtDOB;
    private RadioButton rdoMale, rdoFemale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCalender = Calendar.getInstance();

        initViews();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btnSignUp);
        edtName = findViewById(R.id.regName);
        edtDOB = (TextView) findViewById(R.id.reg_dob);
        edtPhone = findViewById(R.id.regPhone);
        edtEmail = findViewById(R.id.regEmail);
        edtPass = findViewById(R.id.regPass);
        edtAddress = (EditText) findViewById(R.id.reg_addressline1);
        edtCity = (EditText) findViewById(R.id.reg_addressline2);
        edtState = (EditText) findViewById(R.id.reg_addressline3);
        edtPincode = (EditText) findViewById(R.id.reg_addressline4);
        rdoMale = (RadioButton) findViewById(R.id.reg_male);
        rdoFemale = (RadioButton) findViewById(R.id.reg_female);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        edtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dOBPicker == null) {
                    dOBPicker = new DatePickerDialog(RegisterActivity.this, onDateSetListener,
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

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    //String name,String dob,String gender,String email,String contact,String address
                    // ,String city,String state,String pincode,String pass
                    Log.d(TAG, String.format("Name : %s\nDOB : %s\nGender : %s\nEmail : %s\nContact : %s" +
                                    "\nAddress : %s\nCity : %s\nState : %s\nPincode : %s\nPass %s", edtName.getText().toString(), edtDOB.getText().toString()
                            , rdoMale.isChecked() ? "Male" : "Female", edtEmail.getText().toString()
                            , edtPhone.getText().toString(), edtAddress.getText().toString()
                            , edtCity.getText().toString(), edtState.getText().toString()
                            , edtPincode.getText().toString(), edtPass.getText().toString()));
                    new RegisterTask().execute(edtName.getText().toString(), edtDOB.getText().toString()
                            , rdoMale.isChecked() ? "Male" : "Female", edtEmail.getText().toString().trim()
                            , edtPhone.getText().toString(), edtAddress.getText().toString()
                            , edtCity.getText().toString(), edtState.getText().toString()
                            , edtPincode.getText().toString(), edtPass.getText().toString().trim());
                }
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        View[] editText = new View[]{edtName, edtPhone, edtPhone, edtEmail, edtEmail, edtDOB, edtAddress, edtCity
                , edtState, edtPincode, edtPass};
        String[] messages = getResources().getStringArray(R.array.Edit_Validation);
        for (int position = 0; position < editText.length; position++) {
            Log.d(TAG, "validate: position " + position);
            if (position == 2) {
                if (!Patterns.PHONE.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(btnRegister, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else if (position == 4) {
                if (!Patterns.EMAIL_ADDRESS.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(btnRegister, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else {
                if (editText[position] instanceof EditText) {
                    if (((EditText) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(btnRegister, messages[position], Snackbar.LENGTH_SHORT)
                                .show();
                        editText[position].requestFocus();
                        valid = false;
                        break;
                    }
                } else {
                    if (((TextView) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(btnRegister, messages[position], Snackbar.LENGTH_SHORT)
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

    private DatePickerDialog dOBPicker;
    private Calendar mCalender;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalender.set(Calendar.YEAR, year);
            mCalender.set(Calendar.MONTH, month);
            mCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            edtDOB.setText(dateFormat.format(mCalender.getTimeInMillis()));
        }
    };


    public class RegisterTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "RegisterTask";
        private ProgressDialog progressDialog;

        private RegisterTask() {
            progressDialog = new ProgressDialog(RegisterActivity.this);
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
                JSONObject json = api.Uregister(params[0], params[1], params[2], params[3], params[4], params[5]
                        , params[6], params[7], params[8], params[9]);
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
                Utility.ShowAlertDialog(RegisterActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {
                        Utility.ShowAlertDialog(RegisterActivity.this, "Already"
                                , "A user already exists with same email", false);

                    } else if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(RegisterActivity.this, "Registered Successfully" +
                                ", Kindly Login to Continue", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                        }, 100);
                    }

                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
}
