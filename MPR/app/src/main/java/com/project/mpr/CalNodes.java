package com.project.mpr;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.text.PrecomputedText;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class CalNodes extends Thread{
    private static final String TAG="CalNodes";
    ArrayList<NodeAndDist> destList = new ArrayList<>();
    ArrayList<NodeAndDist> sourceList = new ArrayList<>();
    ArrayList<LatLng> solutionList = new ArrayList<>();//인접 노드 결과를 저장할 리스트
    ArrayList<SolRoute> solRouteArrayList=new ArrayList<>();
    GetNode getNode;
    int receive_kacl=0;

    public void calDist(final int num, final LatLng start, final LatLng end, final GoogleMap gMap){//목적지에서 인접한 num개의 좌표 계산
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        final Location souLocation = new Location("startPoint");
        souLocation.setLatitude(start.latitude);
        souLocation.setLongitude(start.longitude);

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

                            Double sourceTemp = (double)souLocation.distanceTo(destLocation); //출발지로부터 거리
                            Double destTemp = (double)endLocation.distanceTo(destLocation); //목적지로부터 거리

                            sourceList.add(new NodeAndDist(snapshot.child("node"+i).getKey(),sourceTemp)); //sourceList에 추가
                            destList.add(new NodeAndDist(snapshot.child("node"+i).getKey(),destTemp));//destList에 추가
                        }
                    }
                };
                thread.start();
                try{
                    thread.join(); // 쓰레드 종료
                    Log.d("sort list", " "+destList.size());

                    Collections.sort(sourceList);
                    Collections.sort(destList);

                    //printList(sourceList);//
                    //printList(destList);//

                    getNumNode(num,sourceList,destList,start,end,gMap);

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

    public void getNumNode(final int num,final ArrayList<NodeAndDist> sourcrArr,final ArrayList<NodeAndDist> destArr,final LatLng start,final LatLng end, final GoogleMap gMap){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange()");
                  final Thread thread=new Thread() {
                    public void run() {
                        for(int i=0; i<(num/2);i++){
                            //리스트에서 node검색
                            FirebaseDB mpr1 = snapshot.child(destArr.get(i).node).getValue(FirebaseDB.class);
                            FirebaseDB mpr2 = snapshot.child(sourcrArr.get(i).node).getValue(FirebaseDB.class);
                    /*Log.d("firebase", "---FIREBASE--- Node id : "+arr.get(i).node);
                    Log.d("firebase", "---FIREBASE--- latitude : "+mpr.getLatitude().doubleValue());
                    Log.d("firebase", "---FIREBASE--- longtitude : "+mpr.getLongtitude().doubleValue());*/
                            //파이어베이스에서 위,경도 찾아 리스트에 저장
                            solutionList.add(new LatLng(mpr1.getLatitude().doubleValue(),mpr1.getLongtitude().doubleValue()));
                            solutionList.add(new LatLng(mpr2.getLatitude().doubleValue(),mpr2.getLongtitude().doubleValue()));

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
                    getNode = new GetNode();
                    getNode.nodeList = getSolutionList(); //리스트 전달
                    ArrayList<SolRoute> solRoutes = getNode.getNode(start, end);
                    //ArrayList<ArrayList<LatLngAlt>> resultList=getNode.getNode(start, end);
                    /**
                    * 그려지는 경로 선택한 칼로리와 시간정보로 제한하기
                    * */
                    Log.d("Cal_TEST", "----------칼로리받아옴?----------- : " +receive_kacl);

                    drawRoute(checkKacl(solRoutes,receive_kacl),gMap);


                    /**
                     * test : add marker
                     */
                    MarkerOptions node = new MarkerOptions();
                    node.title("node 좌표");
                    node.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    for(int k=0;k<num;k++){
                        node.snippet("위도 : "+solutionList.get(k).latitude+ "경도 : "+solutionList.get(k).longitude);
                        node.position(solutionList.get(k));
                        gMap.addMarker(node);
                    }



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

    /*public ArrayList<LatLng> startThread(final int num, final LatLng end){
        final Thread thread=new Thread() {
            public void run() {
                calDist(num,end);
            }
        };
        thread.start();
        try{
            thread.join(); //쓰레드 종료 기다리기
            return getSolutionList();

        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return getSolutionList();
    }*/

    public ArrayList<LatLng> getSolutionList() {
        return solutionList;
    }

    int[] polyColor={Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.BLACK};
    public void drawRoute(final ArrayList<SolRoute> resultList, GoogleMap gMap){ // 맵에 경로 그리기
        /**
         * problem
         * ArrayList<ArrayList<LatLngAlt>>
         * 경로가 overlap되는 부분이 있으면 width가 작은 것이 가려짐
         * 맵에는 경로 하나만 띄울 수 있도록 함, 밑에 경로 리스트 중 하나를 선택 시 그 경로를 보여주는 식으로 변경
         */
        Log.d(TAG,"drawRoute()");

        Polyline[] polylines=new Polyline[resultList.size()];
        for(int i=0;i<resultList.size();i++){
            ArrayList<LatLngAlt> list=resultList.get(i).routeNodes;
            ArrayList<LatLng> polyList=new ArrayList<>();
            for(LatLngAlt node:list)
                polyList.add(new LatLng(node.latitude, node.longitude));

            polylines[i]=gMap.addPolyline(new PolylineOptions()
                    .addAll(polyList)
                    .color(polyColor[i])
                    .width(16-3*i)
                    .clickable(true)//add clickable
            );
        }

        gMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){
            public void onPolylineClick(Polyline polyline) {
                int strokeColor = polyline.getColor() ^ 0x0000CC00;
                polyline.setColor(strokeColor);//클릭 시 색상 변경
                /**
                * polyLine.getID()대신에 해당 폴리라인 리스트의 시간, 칼로리 내용으로 변경하기
                * */
                Log.d(TAG, "시간, 칼로리 출력: "+polyline.getId());
                //polyline.setWidth(120);
                //polyline.setTag(polyline.getId());
                //polyline.setPoints(solutionList);

            }
        });

    }

    public ArrayList<SolRoute> checkKacl(ArrayList<SolRoute> solRoutes,double kcal){
        /**
         * 사용자가 섭취한 칼로리를 소모할 수 있는 경로만 저장
         * */
        ArrayList<SolRoute> result = new ArrayList<>();
        for(int i=0;i<solRoutes.size();i++){
            if(solRoutes.get(i).calories>kcal){
                result.add(solRoutes.get(i));
            }
        }
        return result;
    }

}
