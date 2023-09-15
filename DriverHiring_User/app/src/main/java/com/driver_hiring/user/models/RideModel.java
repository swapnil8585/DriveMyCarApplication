package com.driver_hiring.user.models;

import android.os.Parcel;
import android.os.Parcelable;

public class RideModel implements Parcelable {
    String rideID, userID, userName, cabID, Status, Price, Ratings, startDate, startTime
            , startAddress, sourceLatLng, endDate, endTime, endAddress, endLatLng, totalDays
            , totalhHours, placetype, photo;

    public RideModel(String rideID, String userID, String userName, String cabID, String status, String price
            , String ratings, String startDate, String startTime, String startAddress, String sourceLatLng
            , String endDate, String endTime, String endAddress, String endLatLng, String totalDays
            , String totalhHours, String placetype, String photo) {
        super();
        this.rideID = rideID;
        this.userID = userID;
        this.userName = userName;
        this.cabID = cabID;
        this.Status = status;
        this.Price = price;
        this.Ratings = ratings;
        this.startDate = startDate;
        this.startTime = startTime;
        this.startAddress = startAddress;
        this.sourceLatLng = sourceLatLng;
        this.endDate = endDate;
        this.endTime = endTime;
        this.endAddress = endAddress;
        this.endLatLng = endLatLng;
        this.totalDays = totalDays;
        this.totalhHours = totalhHours;
        this.placetype = placetype;
        this.photo = photo;
    }

    protected RideModel(Parcel in) {
        rideID = in.readString();
        userID = in.readString();
        userName = in.readString();
        cabID = in.readString();
        Status = in.readString();
        Price = in.readString();
        Ratings = in.readString();
        startDate = in.readString();
        startTime = in.readString();
        startAddress = in.readString();
        sourceLatLng = in.readString();
        endDate = in.readString();
        endTime = in.readString();
        endAddress = in.readString();
        endLatLng = in.readString();
        totalDays = in.readString();
        totalhHours = in.readString();
        placetype = in.readString();
        photo = in.readString();

    }

    public static final Creator<RideModel> CREATOR = new Creator<RideModel>() {
        @Override
        public RideModel createFromParcel(Parcel in) {
            return new RideModel(in);
        }

        @Override
        public RideModel[] newArray(int size) {
            return new RideModel[size];
        }
    };

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCabID() {
        return cabID;
    }

    public void setCabID(String cabID) {
        this.cabID = cabID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getRatings() {
        return Ratings;
    }

    public void setRatings(String ratings) {
        Ratings = ratings;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getSourceLatLng() {
        return sourceLatLng;
    }

    public void setSourceLatLng(String sourceLatLng) {
        this.sourceLatLng = sourceLatLng;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getEndLatLng() {
        return endLatLng;
    }

    public void setEndLatLng(String endLatLng) {
        this.endLatLng = endLatLng;
    }

    public String getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(String totalDays) {
        this.totalDays = totalDays;
    }

    public String getTotalhHours() {
        return totalhHours;
    }

    public void setTotalhHours(String totalhHours) {
        this.totalhHours = totalhHours;
    }

    public String getPlacetype() {
        return placetype;
    }

    public void setPlacetype(String placetype) {
        this.placetype = placetype;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rideID);
        dest.writeString(userID);
        dest.writeString(userName);
        dest.writeString(cabID);
        dest.writeString(Status);
        dest.writeString(Price);
        dest.writeString(Ratings);
        dest.writeString(startDate);
        dest.writeString(startTime);
        dest.writeString(startAddress);
        dest.writeString(sourceLatLng);
        dest.writeString(endDate);
        dest.writeString(endTime);
        dest.writeString(endAddress);
        dest.writeString(endLatLng);
        dest.writeString(totalDays);
        dest.writeString(totalhHours);
        dest.writeString(placetype);
        dest.writeString(photo);
    }
}
