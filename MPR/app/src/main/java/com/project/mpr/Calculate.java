package com.project.mpr;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Calculate extends Service {
    private static final String TAG="Calculate";
    ArrayList<NodeAndDist> destList;
    ArrayList<NodeAndDist> sourceList;
    ArrayList<LatLng> solutionList;//인접 노드 결과를 저장할 리스트
    final static LatLng[] cross= new LatLng[]{new LatLng(36.363844, 127.345056),
            new LatLng(36.365990, 127.345360),
            new LatLng(36.366643, 127.343216),
            new LatLng(36.367809, 127.341435),
            new LatLng(36.368880, 127.341553),
            new LatLng(36.368785, 127.342572),
            new LatLng(36.368612, 127.344053),
            new LatLng(36.368431, 127.345748),
            new LatLng(36.369278, 127.345920),
            new LatLng(36.369356, 127.346124),
            new LatLng(36.368828, 127.352275),
            new LatLng(36.369778, 127.347179),
            new LatLng(36.369856, 127.341061),
            new LatLng(36.370320, 127.342863),
            new LatLng(36.370432, 127.344054),
            new LatLng(36.372704, 127.346028),
            new LatLng(36.375468, 127.344215),
            new LatLng(36.375718, 127.343346),
            new LatLng(36.369508, 127.341186)
    };

    @Override
    public void onCreate() {
        super.onCreate();
        destList = new ArrayList<>();
        sourceList= new ArrayList<>();
        solutionList = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LatLng start=(LatLng)intent.getExtras().get("start");
        LatLng end=(LatLng)intent.getExtras().get("end");
        int num=intent.getExtras().getInt("num");

        calDist(start, end); // 출발지 도착지랑 노드들간의 거리 계산
        getNumNode(num, sourceList, destList, start, end); // 경유지 num개 뽑힘
        stopService(intent);

        //solutionList를 Route한테 전달
        Intent intent1=new Intent("calculate");
        intent1.putExtra("nodeList", solutionList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void calDist(final LatLng start, final LatLng end){

        final Location souLocation = new Location("startPoint");
        souLocation.setLatitude(start.latitude);
        souLocation.setLongitude(start.longitude);

        final Location endLocation = new Location("endPoint");
        endLocation.setLatitude(end.latitude);
        endLocation.setLongitude(end.longitude);

        final Location distLocation = new Location("distPoint");

        for(int i=0;i<cross.length;i++){ // 출발 도착지 기준 가까운 노드들 계산
            distLocation.setLatitude(cross[i].latitude);
            distLocation.setLongitude(cross[i].longitude);
            double sourceTemp = (double)souLocation.distanceTo(distLocation); //출발지로부터 거리
            double destTemp = (double)endLocation.distanceTo(distLocation); //목적지로부터 거리

            sourceList.add(new NodeAndDist(i, sourceTemp)); //sourceList에 추가
            destList.add(new NodeAndDist(i, destTemp));//destList에 추가

        }

        Collections.sort(sourceList);
        Collections.sort(destList);

    }
    public void getNumNode(final int num,final ArrayList<NodeAndDist> srcArr,final ArrayList<NodeAndDist> destArr,final LatLng start,final LatLng end){
        Log.i(TAG, "getNumNode()");
        for(int i=0; i<(num/2);i++){
            // 갯수만큼 뽑기
            int idx=destArr.get(i).index;
            solutionList.add(cross[idx]);

            int idx1=srcArr.get(i).index;
            solutionList.add(cross[idx1]);
        }

    }
    private void print(ArrayList<NodeAndDist> list){
        Log.i(TAG, "print()");
        for(NodeAndDist node:list){
            Log.i(TAG, node.index+" "+node.dist);
        }
    }
}
