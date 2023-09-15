package com.driver_hiring.user.models;

public class DriverModel {
    private String did, photo, name, dob, gender, email, contact, totexp, address, city, state, pincode, hourprice, rating;

    public DriverModel(String did, String photo, String name, String dob, String gender, String email
            , String contact, String totexp, String address, String city, String state, String pincode
            , String hourprice, String rating) {
        this.did = did;
        this.photo = photo;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.contact = contact;
        this.totexp = totexp;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.hourprice = hourprice;
        this.rating = rating;
    }

    public String getDid() {
        return did;
    }

    public String getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getContact() {
        return contact;
    }

    public String getTotexp() {
        return totexp;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }

    public String getHourprice() {
        return hourprice;
    }

    public String getRating() {
        return rating;
    }
}
