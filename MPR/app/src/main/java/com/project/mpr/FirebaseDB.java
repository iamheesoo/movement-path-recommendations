package com.project.mpr;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FirebaseDB { //firebase node 객체 생성용 클래스
    Double latitude;
    Double longtitude;

    public FirebaseDB(){};//기본생성자

    public FirebaseDB(Double lat, Double lon){
        this.latitude = lat;
        this.longtitude = lon;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongtitude(){
        return  longtitude;
    }

    public void setLatitude(Double lat){
        this.latitude = lat;
    }

    public void  setLongtitude(Double lon){
        this.longtitude = lon;
    }


}
