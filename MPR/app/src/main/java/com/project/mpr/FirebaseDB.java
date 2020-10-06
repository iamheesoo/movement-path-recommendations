package com.project.mpr;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FirebaseDB { //firebase node 객체 생성용 클래스
    Double dist;
    Double latitude;
    Double longtitude;

    public FirebaseDB(){};//기본생성자

    public FirebaseDB(Double dist, Double lat, Double lon){
        this.dist = dist;
        this.latitude = lat;
        this.longtitude = lon;
    }

    public FirebaseDB(Double dist){
        this.dist = dist;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongtitude(){
        return  longtitude;
    }

    public Double getDist(){
        return  dist;
    }

    public void setLatitude(Double lat){
        this.latitude = lat;
    }

    public void  setLongtitude(Double lon){
        this.longtitude = lon;
    }

    public void setDist(Double dist) {
        this.dist = dist;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("dist", dist);
        result.put("latitude", latitude);
        result.put("longtitude", longtitude);

        return result;
    }







}
