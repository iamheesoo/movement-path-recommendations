package com.project.mpr;

import android.content.ContentValues;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
//수정 필요
public class getAlt {

    public static void getAltitude(Double lat, Double lon){
        URL url;
        {
            try {
                url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations="+lat+","+lon+"&key=AIzaSyDbtoRX-sfO3iCcIdxyApzYFTa2oCU9gcI");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.d("LOG", "응답코드 : " + con.getResponseCode());
                Log.d("LOG", "응답메세지 : " + con.getResponseMessage());
                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8")); //캐릭터셋 설정
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if(sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
                Log.d("LOG", "경도로 변환 : " + sb.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}

