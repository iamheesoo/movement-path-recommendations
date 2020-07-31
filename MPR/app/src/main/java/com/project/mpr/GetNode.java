package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetNode extends Thread{
    private static String TAG="GetNode";
    public ArrayList<LatLngAlt> list;
    String jsonData;

    public ArrayList<LatLngAlt> getNode(final LatLng start, final LatLng end) {
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
            thread.join(); // 쓰레드 종료 후 list 리턴
            GetAltitude ga=new GetAltitude(); // 고도 받아오기
            ga.setAltitude(list);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return list;
    }

    public void jsonRead(String json){ // 파싱
        try {
            JSONObject jsonObj = new JSONObject(json);
            String features=jsonObj.getString("features");
            JSONArray fArray=new JSONArray(features);
            for(int i=0;i<fArray.length();i++){
                JSONObject jObject=fArray.getJSONObject(i);
                JSONObject geometry=jObject.getJSONObject("geometry");
                String type=geometry.getString("type");
                if(type.equals("LineString")){
                    String coordinates=geometry.getString("coordinates");
                    coordinates=coordinates.replaceAll("[\\[\\]]", "");// 대괄호 삭제
                    String[] latlng=coordinates.split(",");
                    for(int j=0;j<latlng.length;j+=2)
                        list.add(new LatLngAlt(Double.parseDouble(latlng[j+1]), Double.parseDouble(latlng[j]), 0));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void printNode(ArrayList<LatLngAlt> list){ // 노드들 리스트 값 출력
        Log.d(TAG, "printNode()");
        for(LatLngAlt point:list)
            Log.d(TAG, point.latitude+", "+point.longitude+", "+point.altitude+"\n");
    }
}