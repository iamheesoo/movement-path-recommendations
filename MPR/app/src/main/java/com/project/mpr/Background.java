package com.project.mpr;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

public class Background extends AsyncTask<LatLng, Void, Void> {

    @Override
    protected Void doInBackground(LatLng... point) {
        GetAltitude g=new GetAltitude();
        g.getAltitude(point[0]);
        return null;
    }

}

