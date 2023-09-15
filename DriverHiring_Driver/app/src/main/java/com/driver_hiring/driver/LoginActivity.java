package com.driver_hiring.driver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.driver_hiring.driver.background.NotificationService;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    public static final int PERMISSION_CODE = 100;
    private Button btnLogin;
    private EditText edtEmail, edtPass;
    private TextView txtRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String UserIdPref = PreferenceManager.getUserId(LoginActivity.this);

        getSupportActionBar().setTitle("Login");

        if (UserIdPref.compareTo("") != 0) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopService(new Intent(LoginActivity.this, NotificationService.class));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForegroundService(new Intent(LoginActivity.this, NotificationService.class));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startService(new Intent(LoginActivity.this, NotificationService.class));
            }

            Intent intent = new Intent(LoginActivity.this, DriverNavigation.class);
            startActivity(intent);
            finish();

        } else {
            setContentView(R.layout.login_layout);

            edtEmail = (EditText) findViewById(R.id.userid);
            edtPass = (EditText) findViewById(R.id.pass);
            btnLogin = (Button) findViewById(R.id.login);
            txtRegistration = (TextView) findViewById(R.id.login_regpage);
            String regText = getResources().getString(R.string.registration_hint);
            SpannableString spannableString = new SpannableString(regText);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), regText.indexOf('S'), regText.length(), 0);
            spannableString.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark))
                    , regText.indexOf('S'), regText.length(), 0);
            txtRegistration.setText(spannableString, TextView.BufferType.SPANNABLE);


        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wehavePermission()) {
                    if (edtEmail.getText().toString().equals("")) {
                        Snackbar.make(btnLogin, "Email is required", Snackbar.LENGTH_SHORT).show();
                        edtEmail.requestFocus();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                        Snackbar.make(btnLogin, "Invalid Email Address", Snackbar.LENGTH_SHORT).show();
                        edtEmail.requestFocus();
                    } else if (edtPass.getText().toString().equals("")) {
                        Snackbar.make(btnLogin, "Password is required", Snackbar.LENGTH_SHORT).show();
                        edtPass.requestFocus();
                    } else {
                        new LoginTask().execute(edtEmail.getText().toString(), edtPass.getText().toString());
                    }
                } else {
                    requestPermission();
                }
            }
        });

        txtRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }

    private boolean wehavePermission() {
        return (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnLogin.performClick();
            } else {
                requestPermission();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class LoginTask extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "RegisterTask";
        private ProgressDialog progressDialog;

        private LoginTask() {
            progressDialog = new ProgressDialog(LoginActivity.this);
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
                JSONObject json = api.Dlogin(params[0], params[1]);
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
                Utility.ShowAlertDialog(LoginActivity.this, pair.first, pair.second, false);
            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("false") == 0) {

                        Utility.ShowAlertDialog(LoginActivity.this, "Invalid Credentials"
                                , "You have entered incorrect credentials", false);
                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject js = jsonArray.getJSONObject(0);

                        PreferenceManager.SavePreference(LoginActivity.this
                                , js.getString("data0")
                                , js.getString("data1"), js.getString("data2"));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            stopService(new Intent(LoginActivity.this, NotificationService.class));
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            startForegroundService(new Intent(LoginActivity.this, NotificationService.class));
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startService(new Intent(LoginActivity.this, NotificationService.class));
                        }

                        Intent intent = new Intent(LoginActivity.this, DriverNavigation.class);
                        startActivity(intent);
                        finish();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
