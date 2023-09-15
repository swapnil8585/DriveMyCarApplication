package com.driver_hiring.user.helper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

public class PreferenceManager {

    private static final String PREFERENCE = "CabBooking";
    private static final String USER_ID = "UserId";
    private static final String USER_NAME = "UserName";


    public static void SavePreference(@NonNull Context context, @NonNull String userId, @NonNull String userName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor  = sharedPreferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public static String getUserId(@NonNull Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_ID, "");
    }

    public static String getUserName(@NonNull Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME, "");
    }
}
