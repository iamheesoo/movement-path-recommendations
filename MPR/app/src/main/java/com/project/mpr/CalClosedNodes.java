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
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

    public void cal_five_latlng(LatLng end){//목적지에서 인접한 num개의 좌표 계산

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
        mDatabase.addValueEventListener(postListener);//db 수정 진행 : firebase에 dist값 채워 넣음
    }

    public LinkedList<LatLng> orderNodes(int num, final LinkedList<LatLng> list){ //num만큼 오름차순으로 정렬
        //list.clear();//왜 이거 하면 안돼 왜 왜 왜 왜..?
        /*
        * list 초기화 한 후 add하기
        * 처음에는 저장이 안됨....다음 터치부터 저장됨 즉, 정렬해서 검색한 뒤로 저장되는데 메소드 분리해서 저장하는거 따로 하던가 해야할듯
        * 처음 num개는 이전 노드들 중에서 정렬함 ->
        * 트랜잭션을 쓰던지 다른걸 쓰던지 해서 파이어베이스 업데이트 다 된 다음에 검색되게 하기..
        * */

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query numOfNode = mDatabase.orderByChild("dist").limitToFirst(num); //num개 데이터 쿼리로 받아옴
        numOfNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot :snapshot.getChildren()){
                    FirebaseDB mpr = dataSnapshot.getValue(FirebaseDB.class);
                    LatLng temp = new LatLng(mpr.latitude,mpr.longtitude);
                    list.add(temp);
                    System.out.println("*****리스트 사이즈*****"+list.size());
                    Log.d("COUNT 값 "+ dataSnapshot.getKey() +"번째", dataSnapshot.toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return list;
    }


    /*public void orderNodes(int num){ //num만큼 오름차순으로 정렬

        //ArrayList<LatLng>
        //nodeList = new ArrayList<>();
        //final LatLng latLng;
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference numOfNode = (DatabaseReference) mDatabase.orderByChild("dist").limitToFirst(num);

        mDatabase.orderByChild("dist").limitToFirst(num).addChildEventListener(new ChildEventListener() { //dist로 오름차순 정렬, 상위 num개만 검색
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { //오름차순 정렬하여 출력
                GetArrayList(snapshot);
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
            } });
    }*/

    public void GetArrayList(DataSnapshot snapshot){
        //ArrayList<LatLng> list = new ArrayList<>();

        FirebaseDB fdb = snapshot.getValue(FirebaseDB.class);
        System.out.println("목적지로부터 가까운 거리 - 키 : "+snapshot.getKey() +"/거리 : "+ fdb.dist + "/위도 :"+fdb.latitude+ "/경도 : "+fdb.longtitude);
        //list.add(new LatLng(fdb.latitude,fdb.longtitude));
        //nodeList.add(new LatLng(fdb.latitude,fdb.longtitude));
        //nodeList = list;
        //return list;
    }

    public void printList(ArrayList<LatLng> arr){
        System.out.println("@@@@@@@array List size@@@@@: "+arr.size());
        for(int i =0; i<arr.size();i++){
            System.out.println("@@@@@@@array List 내용들@@@@@: "+arr.get(i).toString());
        }
    }


}
