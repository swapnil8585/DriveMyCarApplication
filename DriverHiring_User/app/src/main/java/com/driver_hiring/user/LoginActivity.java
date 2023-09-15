package com.driver_hiring.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.driver_hiring.user.background.NotificationService;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {
    private TextView txtRegister;
    private Button btnLogin;
    private EditText edtEmail, edtPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String UserIdPref = PreferenceManager.getUserId(LoginActivity.this);

        if (UserIdPref.compareTo("") != 0) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopService(new Intent(LoginActivity.this, NotificationService.class));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForegroundService(new Intent(LoginActivity.this, NotificationService.class));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startService(new Intent(LoginActivity.this, NotificationService.class));
            }

            Intent intent = new Intent(LoginActivity.this, CabNavigationActivity.class);
            startActivity(intent);
            finish();

        } else {
            setContentView(R.layout.login_layout);

            edtEmail = (EditText) findViewById(R.id.userid);
            edtPass = (EditText) findViewById(R.id.pass);
            btnLogin = (Button) findViewById(R.id.login);
            txtRegister = (TextView) findViewById(R.id.textRegister);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    new LoginTask().execute(edtEmail.getText().toString().trim()
                            , edtPass.getText().toString().trim());
                }
            }
        });
    }

    public void onBackPressed() {
        finish();
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
                JSONObject json = api.Ulogin(params[0], params[1]);
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
                                , js.getString("data1"));

//                        stopService(new Intent(LoginActivity.this, NotificationService.class));
//                        startService(new Intent(LoginActivity.this, NotificationService.class));

                        Intent intent = new Intent(LoginActivity.this, CabNavigationActivity.class);
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
