package com.project.mpr;

import java.util.ArrayList;

public class SolRoute {
    ArrayList<LatLngAlt> routeNodes;
    double meter;
    double time;
    double calories;

    public SolRoute(ArrayList<LatLngAlt> routeNodes, double meter, double time, double calories){
        this.routeNodes = routeNodes;
        this.meter = meter;
        this.time = time;
        this.calories = calories;
    }

}
