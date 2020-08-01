package com.project.mpr;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class CalClosedNodes {

    public void getFirebaseData(){ //db 확인하는 코드 ->DB TEST용

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int i=1;i<=snapshot.getChildrenCount();i++){ //여기서 리스트 생성해서 저장하면 될 듯.
                    FirebaseDB mpr = snapshot.child("node"+i).getValue(FirebaseDB.class);
                    Log.d("db", "---FIREBASE--- Node id : "+"node"+i);
                    Log.d("db", "---FIREBASE--- latitude : "+mpr.getLatitude().doubleValue());
                    Log.d("db", "---FIREBASE--- longtitude : "+mpr.getLongtitude().doubleValue());
                    Log.d("db", "---FIREBASE--- dist : "+mpr.getDist().doubleValue());
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

    public void cal_five_latlng(final int num, LatLng end){//목적지에서 인접한 num개의 좌표 계산

        final HashMap<String, Object> childUpdates = new HashMap<>();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        final Location endLocation = new Location("endPoint");
        endLocation.setLatitude(end.latitude);
        endLocation.setLongitude(end.longitude);
        final Location destLocation = new Location("distPoint");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(int i=1;i<=snapshot.getChildrenCount();i++){//디비 전체 값 확인
                    FirebaseDB mpr = snapshot.child("node"+i).getValue(FirebaseDB.class);
                    destLocation.setLatitude(mpr.getLatitude().doubleValue());
                    destLocation.setLongitude(mpr.getLongtitude().doubleValue());
                    Double temp = (double)endLocation.distanceTo(destLocation);

                    //목적지와 노드들 간의 거리 계산후 값 저장
                    FirebaseDB firebaseDB = new FirebaseDB(temp,mpr.getLatitude().doubleValue(),mpr.getLongtitude().doubleValue());

                    Map<String, Object> userValue;
                    userValue = firebaseDB.toMap();

                    //firebase 수정
                    childUpdates.put("/node" + i, userValue);
                    mDatabase.updateChildren(childUpdates);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        };
        mDatabase.addValueEventListener(postListener);//db 수정 진행

        //firebase 정렬 : 목적지와 인접한 num개의 node 검색
        orderNodes(num);
    }

    public void orderNodes(int num){ //num만큼 오름차순으로 정렬
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.orderByChild("dist").limitToFirst(num).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { //오름차순 정렬하여 출력
                FirebaseDB fdb = snapshot.getValue(FirebaseDB.class);
                System.out.println("*****"+snapshot.getKey() + " 은 " + fdb.dist + "이다.*****");
                // -> 여기서 노드 정보(위도, 경도) 저장한 뒤 경유지 추가하는거 연결하기

        }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
