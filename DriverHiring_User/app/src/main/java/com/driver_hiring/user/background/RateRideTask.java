package com.driver_hiring.user.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class RateRideTask extends AsyncTask<String, JSONObject, String> {
    private static final String TAG = "RideRating";
    private WeakReference<Context> contextWeakReference;
    private ProgressDialog progressDialog;

    public RateRideTask(@NonNull Context context) {
        this.contextWeakReference = new WeakReference<Context>(context);
        progressDialog = new ProgressDialog(context);
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
        String a = "back";
        CabHiringAPI api = new CabHiringAPI();
        try {
            JSONObject json = api.UrateRider(strings[0], strings[1]);
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
            Utility.ShowAlertDialog(contextWeakReference.get(), pair.first, pair.second, false);
        } else {

            try {
                JSONObject json = new JSONObject(s);
                String StatusValue = json.getString("status");

                if (StatusValue.compareTo("already") == 0) {
                    Toast.makeText(contextWeakReference.get(), "You have already rated this Ride"
                            , Toast.LENGTH_SHORT).show();
                } else if (StatusValue.compareTo("ok") == 0) {
                    Toast.makeText(contextWeakReference.get(), "Thanks, Your Rating has been Submitted"
                            , Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
