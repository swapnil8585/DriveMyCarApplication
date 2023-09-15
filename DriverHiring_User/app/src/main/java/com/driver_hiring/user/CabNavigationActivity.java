package com.driver_hiring.user;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.driver_hiring.user.background.NotificationService;
import com.driver_hiring.user.background.RateRideTask;
import com.driver_hiring.user.fragments.CarsFragment;
import com.driver_hiring.user.fragments.ProfileFragment;
import com.driver_hiring.user.fragments.Rides;
import com.driver_hiring.user.fragments.HomeFragment;
import com.driver_hiring.user.fragments.TransactionFragment;
import com.driver_hiring.user.helper.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.RestAPI;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class CabNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String NOTIFICATION_TEXT = "NotificationText";
    public static String NOTIFICATION_RID = "NotificationBundle";
    public static String NOTIFICATION_Boolean = "NotificationBoolean";
    public static final String BROADCAST = "LoadCurrent";
    private static final String TAG = "CabNavigation";
    private NavigationView navigationView;

    int[][] states = new int[][]{
            new int[]{android.R.attr.state_enabled}, // enabled
            new int[]{-android.R.attr.state_enabled}, // disabled
            new int[]{-android.R.attr.state_checked}, // unchecked
            new int[]{android.R.attr.state_pressed}  // pressed
    };

    int[] colors = new int[]{
            R.color.colorButton,
            R.color.colorButton,
            R.color.colorButton,
            R.color.colorButton
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(CabNavigationActivity.this);
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        checkNotification();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(new ColorStateList(states, colors));
        navigationView.setCheckedItem(R.id.nav_home);

        loadFragments(R.id.nav_home);
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
        getMenuInflater().inflate(R.menu.cab_navigation, menu);
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
            return true;
        } else if (id == R.id.action_logout) {
            changePassword();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        loadFragments(item.getItemId());
        return true;
    }

    private void loadFragments(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                fragment = new HomeFragment();
                break;
            case R.id.nav_profile:
                getSupportActionBar().setTitle("My Profile");
                fragment = new ProfileFragment();
                break;
            case R.id.nav_cars:
                getSupportActionBar().setTitle("Cars");
                fragment = new CarsFragment();
                break;
            case R.id.nav_history:
                getSupportActionBar().setTitle("My Rides");
                fragment = new Rides();
                break;
            case R.id.nav_transactions:
                getSupportActionBar().setTitle("My Transactions");
                fragment = new TransactionFragment();
                break;
            case R.id.nav_logout:
                performLogout();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commitAllowingStateLoss();
        }
    }

    private void checkNotification() {
        Intent intent = getIntent();
        String content = intent.getStringExtra(NOTIFICATION_TEXT);
        String contentRateId = intent.getStringExtra(NOTIFICATION_RID);
        if (content != null) {
            if (content.contains("Finished")) {
                showRatingDialog(contentRateId);
            } else {
                Log.d(TAG, "checkNotification: Content : " + content);
            }
        } else {
            Log.d(TAG, "checkNotification: Content is Null");
        }
    }

    private void showRatingDialog(@NonNull final String rideID) {
        final View rating = LayoutInflater.from(CabNavigationActivity.this).inflate(R.layout.notification_rating
                , null, false);
        final AppCompatRatingBar ratingBar = rating.findViewById(R.id.driverRating);

        new AlertDialog.Builder(CabNavigationActivity.this)
                .setCancelable(false)
                .setTitle("Rate this Ride")
                .setMessage("Please rate what was your experience with this ride in Scale from 1 to 5 stars" +
                        ", 5 being extremely satisfied")
                .setView(rating)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new RateRideTask(CabNavigationActivity.this).execute(rideID
                                , ratingBar.getRating() + "");
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void performLogout() {

        PreferenceManager.SavePreference(CabNavigationActivity.this, "", "");

        stopService(new Intent(CabNavigationActivity.this, NotificationService.class));

        final Intent intent = new Intent(CabNavigationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void changePassword() {
        final Dialog dialog = new Dialog(CabNavigationActivity.this);
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
                } else {
                    String uid = PreferenceManager.getUserId(CabNavigationActivity.this);
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
            progressDialog = new ProgressDialog(CabNavigationActivity.this);
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
            progressDialog.dismiss();
            Log.d("ChangePassword", s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(CabNavigationActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("true") == 0) {

                        PreferenceManager.SavePreference(CabNavigationActivity.this, "", "");
                        final Intent intent = new Intent(CabNavigationActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);


                        AlertDialog.Builder ad = new AlertDialog.Builder(CabNavigationActivity.this);
                        ad.setTitle("Success");
                        ad.setMessage("Your Password Has Been Changed, Please Login Again");
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
                        AlertDialog.Builder ad = new AlertDialog.Builder(CabNavigationActivity.this);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.d(TAG, "onReceive: ");
                navigationView.setCheckedItem(R.id.nav_history);
                loadFragments(R.id.nav_history);
            }
        }
    };
}
