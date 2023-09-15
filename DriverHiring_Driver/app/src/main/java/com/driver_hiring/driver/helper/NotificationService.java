package com.driver_hiring.driver.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.driver_hiring.driver.R;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.RestAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final String TAG = "NotificationLog";
    private static final String TAG_NOTIFICATION = "CabBooking";
    private String pid = "";

    private Timer timer;
    private TimerTask timerTask;
    private Handler hand = new Handler();

    boolean checkFlag = true;
    //nid,tid,sid,title,message
    private ArrayList<String> tid, title1, message;

    public NotificationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pid = PreferenceManager.getUserId(NotificationService.this);

        timer = new Timer();
        checkNotifcation();
        timer.schedule(timerTask, 0, 5000);
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
                            new GetNotification().execute(pid);
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
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.getNotification(strings[0]);
                JSONParse jp = new JSONParse();
                answer = jp.Parse(jsonObject);

            } catch (Exception e) {
//                e.printStackTrace();
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.contains("Unable to resolve host")) {
                checkFlag = true;
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    Log.d("NotificationLog", s);
                    if (ans.compareTo("ok") == 0) {
                        tid = new ArrayList<String>();
                        title1 = new ArrayList<String>();
                        message = new ArrayList<String>();
                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            //Tid,Title,Message
                            tid.add(jdata.getString("data0"));
                            title1.add(jdata.getString("data1"));
                            message.add(jdata.getString("data2"));
                        }

                        if (title1.size() > 0) {
                            showNotification(title1.get(0), message.get(0));
                        }

                    } else if (ans.compareTo("no") == 0) {
                        checkFlag = true;
                    } else if (ans.compareTo("error") == 0) {
                        checkFlag = true;
                        String error = json.getString("Data");
                        Log.d(TAG, String.format("Error - %s", error));

//                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    checkFlag = true;
                    e.printStackTrace();
                    Log.d(TAG, String.format("Exception - %s", e.getMessage()));
                }
            }
        }
    }

    private void showNotification(String title, String content) {
        int randomValue = new Random().nextInt(10000) + 1;

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(NotificationService.this, CHANNEL_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true);

        Notification n = builder.build();
        nm.notify(TAG_NOTIFICATION, randomValue, n);

        checkFlag = true;
    }

    public static String CHANNEL_ID = "ChildChannel";

    public static void createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
