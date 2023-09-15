package com.driver_hiring.user.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import com.driver_hiring.user.R;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.RestAPI;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    /* @employee_code and @user_password is to Email and Password From User Respectively*/
    private EditText old_password, new_password;

    /* @user_login is Login Button.*/
    private TextView submit;

    //Loading Dialog
    private Dialog mDialog;
    //Shared Preference
    private SharedPreferences sharedPref;

    //String to Store Employee Code and Employee ID
    private String emp_code = "", emp_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        //Initializing Shared Preference
        sharedPref = getSharedPreferences("SiteAttendance_User", Context.MODE_PRIVATE);
        emp_code = sharedPref.getString("EmployeeCode", "");
        emp_id = sharedPref.getString("EmployeeId", "");

        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Initializing Loading Dialog
        mDialog = new Dialog(ChangePasswordActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        mDialog.setContentView(R.layout.loading_dailog);
        mDialog.setCancelable(false);

        //Initializing View
        //EditText
        old_password = findViewById(R.id.foldPassword);
        new_password = findViewById(R.id.fnewPassword);

        //TextView
        submit = findViewById(R.id.fosubmit_btn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndChange(v);
            }
        });

    }

    private void validateAndChange(View v) {
        String pattern1 = "^(?=.*\\d).{6,}$";
        Pattern pattern = Pattern.compile(pattern1);

        if (old_password.getText().toString().length() == 0) {
            Snackbar snack = Snackbar.make(v, "Please Enter Your Old Password", Snackbar.LENGTH_SHORT);
            View vs = snack.getView();
            TextView txt = (TextView) vs.findViewById(R.id.snackbar_text);
            txt.setTextColor(Color.WHITE);
            snack.show();
            old_password.requestFocus();
        } else if (new_password.getText().toString().length() == 0) {
            Snackbar snack = Snackbar.make(v, "Please Enter Your New Password", Snackbar.LENGTH_SHORT);
            View vs = snack.getView();
            TextView txt = (TextView) vs.findViewById(R.id.snackbar_text);
            txt.setTextColor(Color.WHITE);
            snack.show();
            new_password.requestFocus();
        } else if (!Pattern.matches(pattern1, new_password.getText().toString())) {
            final Snackbar snack = Snackbar.make(v, "Password Should Have Minimum 6 Characters with A Number", Snackbar.LENGTH_LONG);
            View vs = snack.getView();
            TextView txt = (TextView) vs.findViewById(R.id.snackbar_text);
            txt.setTextColor(Color.WHITE);
            snack.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new_password.requestFocus();
                    snack.dismiss();
                }
            });
            snack.setActionTextColor(getResources().getColor(R.color.colorAccent));
            snack.show();
            new_password.requestFocus();
        } else {
            new ChangePassword().execute(old_password.getText().toString(), new_password.getText().toString(), emp_id);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ChangePassword extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            try {
                JSONObject jsonObject = restAPI.ChangePassword(strings[0], strings[1], strings[2]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception e) {
                response = e.getMessage();
                e.printStackTrace();

            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("ChangePassword", s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(ChangePasswordActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("true") == 0) {

                        AlertDialog.Builder ad = new AlertDialog.Builder(ChangePasswordActivity.this);
                        ad.setTitle("Success");
                        ad.setMessage("Your Password Has Been Changed");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();

                            }
                        });
                        ad.show();

                        //Toast.makeText(ChangePasswordActivity.this, "Successful " + "\n" + json.getString("data0"), Toast.LENGTH_SHORT).show();

                    } else if (StatusValue.compareTo("false") == 0) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(ChangePasswordActivity.this);
                        ad.setTitle("Incorrect Password");
                        ad.setMessage("You Have Entered an Incorrect Old Password, Please Enter Your Correct Old Password");

                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("ChangePassword", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("ChangePassword", e.getMessage());
                }
            }
        }
    }
}
