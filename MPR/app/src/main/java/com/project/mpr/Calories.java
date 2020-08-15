package com.project.mpr;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

public class Calories {
    private static final String TAG="Calories";
    public double getCalories(ArrayList<LatLngAlt> list, int m, int t){
        double v=m/t/60.0; // 속력
        double horizontalVO2=0;
        double verticalVO2=0;
        int weight=50;
        for(int i=0;i<list.size()-1;i+=2){
            Location src=new Location("src");
            Location dest=new Location("dest");
            src.setLatitude(list.get(i).latitude);
            src.setLongitude(list.get(i).longitude);
            dest.setLatitude(list.get(i+1).latitude);
            dest.setLongitude(list.get(i+1).longitude);
            double distance=src.distanceTo(dest);
            double time=distance/v;
            double slope=Math.abs(list.get(i).altitude-list.get(i+1).altitude);
            if(slope==0) // 수평적 요소
                horizontalVO2+=0.1*v;
            else // 수직적 요소
                verticalVO2+=1.8*v*slope;
        }
        double totalVO2=(horizontalVO2+verticalVO2)*weight/1000;
//        Log.i(TAG, "totalVO2: "+totalVO2);
        double kcal=totalVO2*5.0*m;
        Log.i(TAG, "kcal: "+kcal);
        return kcal;
    }
}
