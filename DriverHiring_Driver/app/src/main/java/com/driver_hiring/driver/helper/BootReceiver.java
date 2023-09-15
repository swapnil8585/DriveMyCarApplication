package com.driver_hiring.driver.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.driver_hiring.driver.DriverService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                if (PreferenceManager.getUserId(context).compareTo("") != 0) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        context.stopService(new Intent(context, NotificationService.class));
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        context.startForegroundService(new Intent(context, NotificationService.class));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        context.startService(new Intent(context, NotificationService.class));
                    }

                }
            }
        }

    }
}
