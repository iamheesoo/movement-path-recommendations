package com.project.mpr;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class CalNodes {
    ArrayList<NodeAndDist> resultList = new ArrayList<>();//결과를 저장할 리스트
    ArrayList<LatLng> solutionList = new ArrayList<>();

    public void calDist(final int num, LatLng end){//목적지에서 인접한 num개의 좌표 계산
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        final Location endLocation = new Location("endPoint");
        endLocation.setLatitude(end.latitude);
        endLocation.setLongitude(end.longitude);
        final Location destLocation = new Location("distPoint");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final Thread thread=new Thread() {
                    public void run() {
                        for(int i=1;i<=snapshot.getChildrenCount();i++){//디비 전체 값 확인
                            FirebaseDB mpr = snapshot.child("node"+i).getValue(FirebaseDB.class);
                            destLocation.setLatitude(mpr.getLatitude().doubleValue());
                            destLocation.setLongitude(mpr.getLongtitude().doubleValue());
                            Double temp = (double)endLocation.distanceTo(destLocation);

                            resultList.add(new NodeAndDist(snapshot.child("node"+i).getKey(),temp));//list에 추가
                        }
                    }
                };
                thread.start();
                try{
                    thread.join(); // 쓰레드 종료
                    Log.d("sort list", " "+resultList.size());
                    Collections.sort(resultList);

                    //printList(resultList);//
                    getNumNode(num,resultList);

                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    public void getNumNode(final int num, final ArrayList<NodeAndDist> arr){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final Thread thread=new Thread() {
                    public void run() {
                        for(int i=0; i<num;i++){
                            //리스트에서 node검색
                            FirebaseDB mpr = snapshot.child(arr.get(i).node).getValue(FirebaseDB.class);
                    /*Log.d("firebase", "---FIREBASE--- Node id : "+arr.get(i).node);
                    Log.d("firebase", "---FIREBASE--- latitude : "+mpr.getLatitude().doubleValue());
                    Log.d("firebase", "---FIREBASE--- longtitude : "+mpr.getLongtitude().doubleValue());*/
                            //파이어베이스에서 위,경도 찾아 리스트에 저장
                            solutionList.add(new LatLng(mpr.getLatitude().doubleValue(),mpr.getLongtitude().doubleValue()));
                        }
                    }
                };
                thread.start();
                try{
                    thread.join(); // 쓰레드 종료 후 list 리턴
                    Log.d("get solution list", ""+solutionList.size());
                    //printnodes(solutionList);
                    /**
                    * 여기서 solutionList 전달하면 됩니다
                    * */

                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(postListener);//데이터베이스 읽기
    }


    public void printList(ArrayList<NodeAndDist> arr){
        Log.d(TAG, "printList() : "+arr.size());
        for(int i=0;i<arr.size();i++) {
            Log.d(TAG, arr.get(i).node + ", " + arr.get(i).dist + "\n");
        }
    }

    public void printnodes(ArrayList<LatLng> arr){
        Log.d(TAG, "인접 노드 출력 : "+arr.size());
        for(int i=0;i<arr.size();i++) {
            Log.d(TAG, arr.get(i).latitude + ", " + arr.get(i).longitude + "\n");
        }
    }
}
