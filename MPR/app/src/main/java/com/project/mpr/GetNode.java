package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetNode extends Thread{
    private static String TAG="GetNode";
    public ArrayList<LatLng> list;
    String jsonData;
    //고도 리턴
    public ArrayList<LatLng> getNode(final LatLng start, final LatLng end) {
        list=new ArrayList<>();

        Thread thread=new Thread() {
            public void run() {
                HttpConnect h = new HttpConnect();
                String url = h.getDirectionURL(start, end);
                jsonData = h.httpConnection(url);
                jsonRead(jsonData);
                Log.d(TAG, "Node Size: "+list.size());

            }
        };
        thread.start();

        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        Route r=new Route();
        r.drawRoute(list);
        return list;
    }

    public void jsonRead(String json){
        try {
            JSONObject jsonObj = new JSONObject(json);
            String features=jsonObj.getString("features");
            JSONArray fArray=new JSONArray(features);
            for(int i=0;i<fArray.length();i++){
                JSONObject jObject=fArray.getJSONObject(i);
                JSONObject geometry=jObject.getJSONObject("geometry");
                String type=geometry.getString("type");
                if(type.equals("Point")) { // Point만 추출
                    String coordinates=geometry.getString("coordinates");
                    coordinates=coordinates.substring(1, coordinates.length()-1);
                    String[] latlng=coordinates.split(",");
//                    Log.d(TAG, latlng[1]+" "+latlng[0]);
                    list.add(new LatLng(Double.parseDouble(latlng[1]), Double.parseDouble(latlng[0])));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}