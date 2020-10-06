package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GetAltitude {

    private JSONArray jsonArray;

    public double getAltitude(LatLng point){ //고도 받아오기
        HttpConnect h=new HttpConnect();
        String url=h.getAltitudeURL(point);
        String jsonData=h.httpConnection(url);
        return jsonRead(jsonData);
    }

    public void setAltitude(ArrayList<LatLngAlt> list){ // LatLngAlt에 고도 저장
        Background background=new Background();
        background.execute(list);
    }

    private double jsonRead(String str) { // 고도 파싱
        double alt=0;
        try {
            JSONObject jsonObj = new JSONObject(str);
            jsonArray = (JSONArray) jsonObj.get("results");
            JSONObject temp = (JSONObject) jsonArray.get(0);
//            Log.d("LOG", "고도 : " +temp.getString("elevation"));
            alt=Double.parseDouble(temp.getString("elevation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return alt;
    }

}
