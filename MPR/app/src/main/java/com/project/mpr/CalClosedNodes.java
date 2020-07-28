package com.project.mpr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


public class CalClosedNodes {

    public void getFirebaseData(){

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(int i=1;i<=snapshot.getChildrenCount();i++){ //여기서 리스트 생성해서 저장하면 될 듯.
                    FirebaseDB mpr = snapshot.child("node"+i).getValue(FirebaseDB.class);
                    Log.d("db", "---FIREBASE--- Node id : "+"node"+i);
                    Log.d("db", "---FIREBASE--- latitude : "+mpr.getLatitude());
                    Log.d("db", "---FIREBASE--- longtitude : "+mpr.getLongtitude());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference myRef = mDatabase.getRoot();
        mDatabase.addValueEventListener(postListener);//데이터베이스 읽기

    }

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
