package com.driver_hiring.user.models;

public class CarsModel{
    private String cid,uid,brand,model,transmision,year,chasisno,carno,type,fuel;

    public CarsModel(String cid, String uid, String brand, String model, String transmision
            , String year, String chasisno, String carno, String type, String fuel) {
        this.cid = cid;
        this.uid = uid;
        this.brand = brand;
        this.model = model;
        this.transmision = transmision;
        this.year = year;
        this.chasisno = chasisno;
        this.carno = carno;
        this.type = type;
        this.fuel = fuel;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTransmision() {
        return transmision;
    }

    public void setTransmision(String transmision) {
        this.transmision = transmision;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getChasisno() {
        return chasisno;
    }

    public void setChasisno(String chasisno) {
        this.chasisno = chasisno;
    }

    public String getCarno() {
        return carno;
    }

    public void setCarno(String carno) {
        this.carno = carno;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }
}
