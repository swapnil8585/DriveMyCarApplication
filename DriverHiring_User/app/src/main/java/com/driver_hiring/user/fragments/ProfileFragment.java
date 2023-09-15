package com.driver_hiring.user.fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.models.UserModel;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ProfileFragment extends Fragment {
    private static final String TAG = "Profile";
    private String UID = "";
    private Button btnUpdate;
    private TextView textDOB;
    private RadioButton rdoMale, rdoFemale;
    private EditText edtName, edtPhone, edtEmail, edtAddress, edtCity, edtState, edtPincode;
    private UserModel mUserProfile;

    private static ProgressDialog mProgressDialog;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalender = Calendar.getInstance();
        UID = PreferenceManager.getUserId(getActivity());
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
    }

    private void initViews(View view) {
        btnUpdate = view.findViewById(R.id.btnSignUp);
        edtName = view.findViewById(R.id.regName);
        textDOB = (TextView) view.findViewById(R.id.reg_dob);
        edtPhone = view.findViewById(R.id.regPhone);
        edtEmail = view.findViewById(R.id.regEmail);
        edtAddress = (EditText) view.findViewById(R.id.reg_addressline1);
        edtCity = (EditText) view.findViewById(R.id.reg_addressline2);
        edtState = (EditText) view.findViewById(R.id.reg_addressline3);
        edtPincode = (EditText) view.findViewById(R.id.reg_addressline4);
        rdoMale = (RadioButton) view.findViewById(R.id.reg_male);
        rdoFemale = (RadioButton) view.findViewById(R.id.reg_female);

        view.findViewById(R.id.regPass).setVisibility(View.GONE);

        btnUpdate.setText("Update");
    }

    private void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetProfile(getActivity(), ProfileFragment.this).execute(UID);
    }

    @Override
    public void onStart() {
        super.onStart();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    //String uid,String name,String email,String contact
                    //String uid,String name,String dob,String gender,String email,String contact
                    // ,String address,String city,String state,String pincode
                    new UpdateProfile(getActivity(), ProfileFragment.this).execute(
                            PreferenceManager.getUserId(getActivity()), edtName.getText().toString()
                            , dateFormat.format(mCalender.getTimeInMillis()), rdoMale.isChecked() ? "Male" : "Female"
                            , edtEmail.getText().toString().trim(), edtPhone.getText().toString()
                            , edtAddress.getText().toString(), edtCity.getText().toString()
                            , edtState.getText().toString(), edtPincode.getText().toString());
                }
            }
        });

        textDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dOBPicker == null) {
                    dOBPicker = new DatePickerDialog(getActivity(), onDateSetListener,
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
    }

    private boolean validate() {
        boolean valid = true;
        View[] editText = new View[]{edtName, edtPhone, edtPhone, edtEmail, edtEmail, textDOB, edtAddress, edtCity
                , edtState, edtPincode};
        String[] messages = getResources().getStringArray(R.array.Edit_Validation);
        for (int position = 0; position < editText.length; position++) {
            Log.d(TAG, "validate: position " + position);
            if (position == 2) {
                if (!Patterns.PHONE.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(btnUpdate, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else if (position == 4) {
                if (!Patterns.EMAIL_ADDRESS.matcher(((EditText) editText[position]).getText().toString()).matches()) {
                    Snackbar.make(btnUpdate, messages[position], Snackbar.LENGTH_SHORT)
                            .show();
                    editText[position].requestFocus();
                    valid = false;
                    break;
                }
            } else {
                if (editText[position] instanceof EditText) {
                    if (((EditText) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(btnUpdate, messages[position], Snackbar.LENGTH_SHORT)
                                .show();
                        editText[position].requestFocus();
                        valid = false;
                        break;
                    }
                } else {
                    if (((TextView) editText[position]).getText().toString().length() == 0) {
                        Snackbar.make(btnUpdate, messages[position], Snackbar.LENGTH_SHORT)
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

            textDOB.setText(dateFormat.format(mCalender.getTimeInMillis()));
        }
    };

    private static class GetProfile extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "GetProfile";
        private WeakReference<Context> appContext;
        private ProfileFragment profileFragment;

        private GetProfile(@NonNull Context context, @NonNull ProfileFragment fragment) {
            this.appContext = new WeakReference<>(context);
            this.profileFragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.setMessage("Please Wait");
                mProgressDialog.show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UgetProfile(strings[0]);
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

            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(appContext.get(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no") == 0) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Log.d(TAG, "onPostExecute: Profile Not Found");
                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");
                        JSONObject js = jsonArray.getJSONObject(0);

                        profileFragment.mUserProfile = new UserModel(js.getString("data0")
                                , js.getString("data1"), js.getString("data2")
                                , js.getString("data3"), js.getString("data4")
                                , js.getString("data5"), js.getString("data6")
                                , js.getString("data7"), js.getString("data8")
                                , js.getString("data9"));

                        profileFragment.setValues();
                    } else {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }

                } catch (Exception e) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
    }

    private static class UpdateProfile extends AsyncTask<String, JSONObject, String> {
        private static final String TAG = "UdpateProfile";
        private WeakReference<Context> appContext;
        private ProfileFragment profileFragment;

        private UpdateProfile(@NonNull Context context, @NonNull ProfileFragment fragment) {
            this.appContext = new WeakReference<>(context);
            this.profileFragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Please Wait");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UupdateProfile(strings[0], strings[1], strings[2], strings[3]
                        , strings[4], strings[5], strings[6], strings[7], strings[8], strings[9]);
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
            mProgressDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(appContext.get(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("already") == 0) {
                        profileFragment.clearViews();
                        profileFragment.setValues();
                        Utility.ShowAlertDialog(appContext.get(), "User Exists !"
                                , "A user already exists with same email, Please choose different Email"
                                , false);
                    } else if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(appContext.get(), "Details updated successfully", Toast.LENGTH_SHORT).show();
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

    private void clearViews() {
        edtEmail.setText("");
        edtName.setText("");
        edtPhone.setText("");
        rdoMale.setChecked(false);
        rdoFemale.setChecked(false);

        edtAddress.setText("");
        edtCity.setText("");
        edtState.setText("");

        edtPincode.setText("");
        textDOB.setText("");
    }

    private void setValues() {
        edtEmail.setText(mUserProfile.getEmail());
        edtName.setText(mUserProfile.getName());
        edtPhone.setText(mUserProfile.getContact());

        if (mUserProfile.getGender().compareTo("Male") == 0) {
            rdoMale.setChecked(true);
        } else {
            rdoFemale.setChecked(true);
        }

        edtAddress.setText(mUserProfile.getAddress());
        edtCity.setText(mUserProfile.getCity());
        edtState.setText(mUserProfile.getState());

        edtPincode.setText(mUserProfile.getPincode());


        edtEmail.setSelection(edtEmail.length());
        edtName.setSelection(edtName.length());
        edtPhone.setSelection(edtPhone.length());

        edtAddress.setSelection(edtAddress.length());
        edtCity.setSelection(edtCity.length());
        edtState.setSelection(edtState.length());

        hideKeyBoard(edtEmail);

        mCalender = getCalender(mUserProfile.getDob());
        textDOB.setText("Date of Birth : " + dateFormat.format(mCalender.getTimeInMillis()));

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    private Calendar getCalender(String startDate) {
        String[] dates = startDate.split("/");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
        return calendar;
    }

}
