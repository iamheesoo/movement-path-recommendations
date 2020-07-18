package com.project.mpr;

import com.google.android.gms.maps.model.LatLng;

public class CalClosedNodes {

    public LatLng cal_middle_latlng(LatLng start, LatLng end){//출발지, 목적지의 중간 좌표 계산
        Double start_x, start_y, end_x, end_y, mid_x, mid_y;
        start_x = start.latitude; //출발지 위도
        start_y = start.longitude; //출발지 경도
        end_x = end.latitude;
        end_y = end.longitude;
        mid_x = (start_x + end_x)/2;
        mid_y = (start_y + end_y)/2;

        LatLng middle_latlng = new LatLng(mid_x,mid_y);
        return middle_latlng;
    }

    /*public void cal_five_latlng(){//중간 지점에서 인접한 5개의 좌표 계산

    }*/

}
