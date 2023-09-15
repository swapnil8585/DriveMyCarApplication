package com.driver_hiring.user.background;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.driver_hiring.user.CabNavigationActivity;
import com.driver_hiring.user.LoginActivity;
import com.driver_hiring.user.R;
import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();
    private static final String TAG_NOTIFICATION = "CabHiring";
    public static final int NOTIFICATION_ID = 123;
    private String content, title, rideId, did = "";

    private Timer timer;
    private TimerTask timerTask;
    private Handler hand = new Handler();
    boolean checkFlag = true;

    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private WifiManager wm;
    private WifiManager.WifiLock wlock;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: OnCreate");

        startServices();

        timer = new Timer();
        checkNotifcation();
        timer.schedule(timerTask, 0, 5000);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NevonCalling:");
            wl.acquire();
        }

        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            wlock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "NevonCalling:");
            wlock.acquire();
        }
    }

    public NotificationService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void checkNotifcation() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                hand.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkFlag) {
                            if (PreferenceManager.getUserId(getApplicationContext()).compareTo("") != 0) {
                                new GetNotification().execute(PreferenceManager.getUserId(NotificationService.this));
                            }
                        }
                    }
                });


            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GetNotification extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkFlag = false;
//            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            CabHiringAPI restAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = restAPI.getNotification(strings[0]);
                JSONParse jp = new JSONParse();
                answer = jp.Parse(jsonObject);
            } catch (Exception e) {
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);
            if (s.contains("Unable to resolve host")) {
                checkFlag = true;
                Log.d(TAG, "onPostExecute: No Internet Available");
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("ok") == 0) {
                        //Title,Message
                        JSONArray jarray = json.getJSONArray("Data");
                        JSONObject jdata = jarray.getJSONObject(0);
                        title = jdata.getString("data0");
                        content = jdata.getString("data1");
                        rideId = jdata.getString("data2");

                        showNotification(title, content, rideId);

                    } else if (ans.compareTo("no") == 0) {
                        checkFlag = true;
                        Log.d(TAG, "onPostExecute: No");

                    } else if (ans.compareTo("error") == 0) {
                        checkFlag = true;
                        String error = json.getString("Data");
                        Log.d(TAG, "onPostExecute: Error - " + error);

                    }
                } catch (Exception e) {
                    checkFlag = true;
                    Log.d(TAG, "onPostExecute: Catch - " + e.getMessage());
                }
            }
        }
    }

    private void showNotification(String title, String content, String rideId) {
        int randomValue = new Random().nextInt(10000) + 1;

        Intent notificationIntent = new Intent(this, CabNavigationActivity.class);
        notificationIntent.putExtra(CabNavigationActivity.NOTIFICATION_TEXT, content);
        notificationIntent.putExtra(CabNavigationActivity.NOTIFICATION_RID, rideId);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.d("Content", content);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, randomValue, notificationIntent, 0);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this
                , CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setAutoCancel(true);

        Notification n = builder.build();
        nm.notify(TAG_NOTIFICATION, randomValue, n);

        checkFlag = true;
    }

    @Override
    public void onDestroy() {

        if (wl != null) {
            wl.release();
        }
        if (wlock != null) {
            wlock.release();
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        super.onDestroy();
    }

    private void startServices() {
        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel(getApplicationContext(), CHANNEL_ID);

        Notification notification = new NotificationCompat
                .Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public static String CHANNEL_ID = "Nevon_Calling";

    public static void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
//                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }

}
