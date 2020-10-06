package com.project.mpr;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Background extends AsyncTask<ArrayList<LatLngAlt>, Void, ArrayList<LatLngAlt>> {
    // doInBackground 파라미터, onProgressUpdate 파라미터, doIn~리턴값&onPostExecute 파라미터

    private static String TAG="Background";

    @Override
    protected ArrayList<LatLngAlt> doInBackground(ArrayList<LatLngAlt>... list){ // 고도 받아와서 저장
        Log.d(TAG, "doInBackground()");
        GetAltitude g=new GetAltitude();
        for(LatLngAlt point:list[0]){
            LatLng temp=new LatLng(point.latitude, point.longitude);
            point.altitude=g.getAltitude(temp);
        }
        return list[0];
    }

    @Override
    protected void onPostExecute(ArrayList<LatLngAlt> list) { // 제대로 저장되었는지 프린트하여 체크
        Log.d(TAG, "onPostExecute()");
//        GetNode g=new GetNode();
//        g.printNode(list);
    }

}

