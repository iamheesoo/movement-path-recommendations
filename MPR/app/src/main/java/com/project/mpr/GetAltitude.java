package com.project.mpr;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetAltitude {

    private ArrayList<String>altitudeList;
    private JSONArray jsonArray;

    String MY_API="AIzaSyDbtoRX-sfO3iCcIdxyApzYFTa2oCU9gcI";
    public String httpConnection(Double lat, Double lng) {
        URL url = null;
        HttpURLConnection conn = null;
        String jsonData = "";
        BufferedReader br = null;
        StringBuffer sb = null;
        String returnText = "";

        try {
            url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations=" + lat + "," + lng + "&key=" + MY_API);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.connect();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            sb = new StringBuffer();

            while ((jsonData = br.readLine()) != null) {
                sb.append(jsonData);
            }
            returnText = sb.toString();
            jsonRead(returnText); //json parsing
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnText;
    }

    //고도 로그로 출력
    private void jsonRead(String str) {
        try {
            JSONObject jsonObj = new JSONObject(str);
            jsonArray = (JSONArray) jsonObj.get("results");
            JSONObject temp = (JSONObject) jsonArray.get(0);
            Log.d("LOG", "고도 : " +temp.getString("elevation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
