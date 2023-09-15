package com.driver_hiring.user.helper;

public class DriverModel {

    //did,name,contact,photo,carmodel,carno,curloc,peakcost,nonpcost,avgrating,dist(in kms),tripid
    String DriverId, Name, Contact, Photo, CarModel, CarNo, currentLocation, PeekCost, NorCost, rating, distance,tripId;

    public DriverModel(String driverId, String name, String contact, String photo
            , String carModel, String carNo, String location, String peekCost, String norCost, String Rating
            ,String distance, String TripId) {
        setDriverId(driverId);
        setName(name);
        setContact(contact);
        setPhoto(photo);
        setCarModel(carModel);
        setCarNo(carNo);
        setCurrentLocation(location);
        setPeekCost(peekCost);
        setNorCost(norCost);
        setRating(Rating);
        setDistance(distance);
        setTripId(TripId);

    }

    public String getDriverId() {
        return DriverId;
    }

    public void setDriverId(String driverId) {
        DriverId = driverId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getCarModel() {
        return CarModel;
    }

    public void setCarModel(String carModel) {
        CarModel = carModel;
    }

    public String getCarNo() {
        return CarNo;
    }

    public void setCarNo(String carNo) {
        CarNo = carNo;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getPeekCost() {
        return PeekCost;
    }

    public void setPeekCost(String peekCost) {
        PeekCost = peekCost;
    }

    public String getNorCost() {
        return NorCost;
    }

    public void setNorCost(String norCost) {
        NorCost = norCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
