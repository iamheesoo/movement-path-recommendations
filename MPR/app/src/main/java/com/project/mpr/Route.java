package com.project.mpr;

import android.graphics.Color;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Route extends AppCompatActivity {
    private static String TAG="ROUTE";
    public void drawRoute(ArrayList<LatLng> list){
        GoogleMap gMap= ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync();
        Log.d(TAG,"drawRoute()");
        for(int i=0;i<list.size()-1;i++){
            LatLng src=list.get(i);
            LatLng dest=list.get(i+1);
            Polyline line=gMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude, dest.longitude)
                    ).width(2).color(Color.BLUE)
            );
        }
    }
}
