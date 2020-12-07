package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

public class HttpConnect {//google-map : 고도 api, t-map : 도보 경로 api
    private static final String TAG="HttpConnect";
    String MY_GOOGLE_API="FILL_YOUR_KEY";
    String MY_TMAP_API="FILL_YOUR_KEY";
    HashSet<String> hashSet=new HashSet<>();

    public String getAltitudeURL(LatLng point){
        return "https://maps.googleapis.com/maps/api/elevation/json?locations="+point.latitude+ ","+point.longitude+"&key="+MY_GOOGLE_API;
    }
    public String getDirectionURL(LatLng start, LatLng end){
        return "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&appKey="+MY_TMAP_API+
                "&startX="+start.longitude+"&startY="+start.latitude+"&endX="+end.longitude+"&endY="+end.latitude+
                "&startName=출발지&endName=도착지&reqCoordType:%22WGS84GEO%22&resCoordType:%22WGS84GEO%22&passList=";
    }

    public String httpConnection(String u) {
        URL url = null;
        HttpURLConnection conn = null;
        String jsonData = "";
        BufferedReader br = null;
        StringBuffer sb = null;
        String returnText = "";

        try {
            url = new URL(u);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            sb = new StringBuffer();

            while ((jsonData = br.readLine()) != null) {
                sb.append(jsonData);
            }

            returnText = sb.toString();
//            Log.d(TAG, u);
//            Log.d(TAG, returnText);

        } catch (FileNotFoundException e){
            Log.d(TAG, "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!returnText.equals("") && !isSameData(returnText))  // FileNotFound가 아니고 새 값이라면
            return returnText;
        else return "";
    }

    public boolean isSameData(String data){ // 중복되는 json이면 true 리턴
        if(hashSet.contains(data)) return true;
        else {
            hashSet.add(data);
            return false;
        }
    }
}
