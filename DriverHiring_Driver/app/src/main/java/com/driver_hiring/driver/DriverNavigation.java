package com.driver_hiring.driver;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.driver_hiring.driver.background.NotificationService;
import com.driver_hiring.driver.fragments.HomeFragment;
import com.driver_hiring.driver.fragments.TransactionFragment;
import com.driver_hiring.driver.rides.CurrentRides;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.rides.Rides;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.RestAPI;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverNavigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int REQUEST_CHECK_SETTINGS = 1001;
    public static final int PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        CircleImageView dDriverImage = headerView.findViewById(R.id.driverImage);
        TextView dDriverName = headerView.findViewById(R.id.driverName);
        TextView driverEmail = headerView.findViewById(R.id.driverEmail);

        Log.d("NAVI", "onCreate: Image : " + PreferenceManager
                .getDriverImage(DriverNavigation.this));

        if (PreferenceManager.getDriverImage(DriverNavigation.this).compareTo("") != 0) {
            dDriverImage.setImageBitmap(CurrentRides
                    .getImageBitmap(PreferenceManager
                            .getDriverImage(DriverNavigation.this)));
        } else {
            dDriverImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_icon_user));
        }
        dDriverName.setText(PreferenceManager
                .getUserName(DriverNavigation.this));
        driverEmail.setText("");

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(new Intent(DriverNavigation.this, DriverProfileActivity.class));
            }
        });


        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        loadFragment(R.id.nav_home);

        //        if (ActivityCompat.checkSelfPermission(DriverNavigation.this
//                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(DriverNavigation.this
//                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            showGPSDialog();
//        } else {
//            ActivityCompat.requestPermissions(DriverNavigation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
//                    , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
//        }

    }

    private void showGPSDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off


        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(DriverNavigation.this)
                        .checkLocationSettings(builder.build());
        result
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        if (!new DriverService().serviceIsRunningInForeground(DriverNavigation.this)) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                startForegroundService(new Intent(DriverNavigation.this, DriverService.class));
//                            } else {
//                                startService(new Intent(DriverNavigation.this, DriverService.class));
//                            }
//                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            try {
                                ((ResolvableApiException) e).startResolutionForResult(DriverNavigation.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }

                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                showGPSDialog();

            } else {
                // Permission was denied or request was cancelled
                ActivityCompat.requestPermissions(DriverNavigation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.driver_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            changePassword();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        loadFragment(id);
        return true;
    }

    private void loadFragment(int id) {
        Fragment fragment = null;
        if (id == R.id.nav_home) {
            getSupportActionBar().setTitle("Home");
            fragment = new HomeFragment();
        } else if (id == R.id.nav_history) {
            getSupportActionBar().setTitle("History");
            fragment = new Rides();
        } else if (id == R.id.nav_transactions) {
            getSupportActionBar().setTitle("Transaction");
            fragment = new TransactionFragment();
        } else if (id == R.id.nav_logout) {
            performLogout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
        }
    }

    private void performLogout() {

        PreferenceManager.SavePreference(getApplicationContext(), "", "", "");
        PreferenceManager.setRideStarted(getApplicationContext(), "", false);

        stopService(new Intent(DriverNavigation.this, NotificationService.class));

        Intent intent = new Intent(DriverNavigation.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void changePassword() {
        final Dialog dialog = new Dialog(DriverNavigation.this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_changepassword);

        //Initializing View
        //EditText
        final EditText old_password = dialog.findViewById(R.id.foldPassword);
        final EditText new_password = dialog.findViewById(R.id.fnewPassword);

        //TextView
        TextView submit = dialog.findViewById(R.id.fosubmit_btn);
        TextView cancel = dialog.findViewById(R.id.focancel_btn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (old_password.getText().toString().length() == 0) {
                    Snackbar snack = Snackbar.make(v, "Please Enter Your Old Password", Snackbar.LENGTH_SHORT);
                    View vs = snack.getView();
                    TextView txt = (TextView) vs.findViewById(com.google.android.material.R.id.snackbar_text);
                    txt.setTextColor(Color.WHITE);
                    snack.show();
                    old_password.requestFocus();
                } else if (new_password.getText().toString().length() == 0) {
                    Snackbar snack = Snackbar.make(v, "Please Enter Your New Password", Snackbar.LENGTH_SHORT);
                    View vs = snack.getView();
                    TextView txt = (TextView) vs.findViewById(com.google.android.material.R.id.snackbar_text);
                    txt.setTextColor(Color.WHITE);
                    snack.show();
                    new_password.requestFocus();
                } else {
                    String uid = PreferenceManager.getUserId(DriverNavigation.this);
                    new ChangePassword().execute(uid, old_password.getText().toString(), new_password.getText().toString());
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private class ChangePassword extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;

        private ChangePassword() {
            progressDialog = new ProgressDialog(DriverNavigation.this);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            try {
                JSONObject jsonObject = restAPI.DChangePassword(strings[0], strings[1], strings[2]);
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
            progressDialog.dismiss();
            Log.d("ChangePassword", s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DriverNavigation.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("true") == 0) {

                        PreferenceManager.SavePreference(DriverNavigation.this, "", "", "");
                        final Intent intent = new Intent(DriverNavigation.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);


                        AlertDialog.Builder ad = new AlertDialog.Builder(DriverNavigation.this);
                        ad.setTitle("Success");
                        ad.setMessage("Your Password Has Been Changed, Please Login to Continue");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                startActivity(intent);
                                finish();

                            }
                        });
                        ad.show();

                        //Toast.makeText(ChangePasswordActivity.this, "Successful " + "\n" + json.getString("data0"), Toast.LENGTH_SHORT).show();

                    } else if (StatusValue.compareTo("false") == 0) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(DriverNavigation.this);
                        ad.setTitle("Incorrect Credentials");
                        ad.setMessage("You Have entered an incorrect password, Please enter your password");

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

    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(this, "Stopping Service", Toast.LENGTH_SHORT).show();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (new DriverService().serviceIsRunningInForeground(DriverNavigation.this)) {
//                stopService(new Intent(DriverNavigation.this, DriverService.class));
//            }
//        } else {
//            stopService(new Intent(DriverNavigation.this, DriverService.class));
//        }

    }
}
