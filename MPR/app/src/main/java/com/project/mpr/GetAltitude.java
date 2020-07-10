package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetAltitude {

    private ArrayList<String>altitudeList;
    private JSONArray jsonArray;

    //고도 리턴
    public double getAltitude(LatLng point){
        HttpConnect h=new HttpConnect();
        String url=h.getAltitudeURL(point);
        String jsonData=h.httpConnection(url);
        return jsonRead(jsonData);
    }

    //고도 로그로 출력
    private double jsonRead(String str) {
        double alt=0;
        try {
            JSONObject jsonObj = new JSONObject(str);
            jsonArray = (JSONArray) jsonObj.get("results");
            JSONObject temp = (JSONObject) jsonArray.get(0);
            Log.d("LOG", "고도 : " +temp.getString("elevation"));
            alt=Double.parseDouble(temp.getString("elevation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alt;
    }


}
