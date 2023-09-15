package com.driver_hiring.driver.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PreferenceManager {

    private static final String PREFERENCE = "CabBooking";
    private static final String USER_ID = "UserId";
    private static final String USER_NAME = "UserName";

    private static final String DRIVER_IMAGE = "driverImage";
    private static final String DRIVER_NAME = "driverName";
    private static final String DRIVER_EMAIL = "driverEmail";

    private static final String DRIVER_TRIP = "tripId";
    private static final String DRIVER_WORKING = "isWorking";

    private static final String DRIVER_MOCK = "StartMocking";

    private static final String RIDE_STARTED = "Ride";
    private static final String RIDE_ID = "Ride_ID";

    public static void SavePreference(@NonNull Context context, @NonNull String userId
            , @NonNull String userName, @NonNull String image) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(USER_NAME, userName);
        editor.putString(DRIVER_IMAGE, image);
        editor.commit();
    }

    public static String getUserId(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_ID, "");
    }

    public static String getUserName(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_NAME, "");
    }

    public static void saveDetails(@NonNull Context context, @NonNull String name
            , @NonNull String image, @NonNull String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DRIVER_NAME, name);
        editor.putString(DRIVER_IMAGE, image);
        editor.putString(DRIVER_EMAIL, email);
        editor.commit();
    }

    public static String getDriverName(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DRIVER_NAME, "");
    }

    public static String getDriverImage(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DRIVER_IMAGE, "");
    }

    public static String getDriverEmail(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DRIVER_EMAIL, "");
    }

    public static void saveTripId(@NonNull Context context, @NonNull String tripID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DRIVER_TRIP, tripID);
        editor.commit();
    }

    public static String getDriverTrip(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DRIVER_TRIP, "");
    }

    public static boolean isWorking(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(DRIVER_WORKING, false);
    }

    public static void setIsWorking(@NonNull Context context, boolean isWorking) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DRIVER_WORKING, isWorking);
        editor.commit();
    }

    public static boolean isRideAvailable(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(RIDE_STARTED, false);
    }

    public static String getRideID(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(RIDE_ID, "");
    }

    public static void setRideStarted(@NonNull Context context, @NonNull String rideId, boolean ride) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RIDE_STARTED, ride);
        editor.putString(RIDE_ID, rideId);
        editor.commit();

    }

}
