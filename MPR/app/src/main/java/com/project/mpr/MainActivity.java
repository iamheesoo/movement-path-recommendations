package com.project.mpr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static LinkedList<LatLng> nearNodes=new LinkedList<>();
    public static ArrayList<NodeAndDist> nodeDistarrayList = new ArrayList<>();//

    private GoogleMap gMap;
    int count_marker = 0;
    LatLng start, end;
    private final String TAG = "MainActivity";
    private View mLayout;
    EditText editText;
    static int userTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout=findViewById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //xml에서 Button클릭 시 editText값 저장하기
        editText=(EditText)findViewById(R.id.editText);
    }
    public void onButtonClick(View v){
        String str=editText.getText().toString();
        userTime=Integer.parseInt(str);
        Log.i(TAG, "userTime "+userTime);
    }

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    public void checkPermission(){

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        checkPermission();
        LatLng school = new LatLng(36.362978, 127.344807); //충남대학교 정문
//        gMap.moveCamera(CameraUpdateFactory.newLatLng(school));
//        gMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(school,15));
//        Log.d("!", school.latitude+" "+school.longitude);
        //핀 찍기
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng point) {
                //

                if(count_marker==0){//출발지 좌표
                    start=point;
                }
                else if(count_marker==1){//목적지 좌표
                    end=point;
                }
                if(count_marker==2){
                    /**
                    * count_marker >=2 이상일 경우,
                     * 아래의 else가 실행되어 계속 마커가 찍힘
                     * * */

                    count_marker++;
                    Log.d("____TEST____", "두 번 이상 클릭하면 안돼요~"+count_marker);
                    //Log.d(TAG, start.latitude+" "+ end.latitude);

                    // 경유지 받아오기
                   CalNodes calnode = new CalNodes();
                   /**
                    * 여기서 그리기까지 다 함
                    * num <=5
                   * */
                   calnode.calDist(5,start,end,gMap);

                    //t-map api 호출 : 출발지->도착지 경로 좌표 구함
                    /*GetNode g=new GetNode();
                    ArrayList<ArrayList<LatLngAlt>> resultList=g.getNode(start, end); // 경로 노드 받아오기 (고도 포함)
                    drawRoute(resultList); // 경로 그리기*/

                    //중간 좌표 계산하기
                    CalClosedNodes c = new CalClosedNodes();
                    LatLng midXY = c.cal_middle_latlng(start,end);

                    //중간 좌표 찍기
                    /*MarkerOptions midM = new MarkerOptions();
                    midM.title("중간 값 좌표");
                    midM.snippet("위도 : "+midXY.latitude+ "경도 : "+midXY.longitude);
                    midM.position(midXY);
                    gMap.addMarker(midM);*/


                   // c.cal_five_latlng(end,nearNodes); //DB수정
                    //c.orderNodes(2,nearNodes);//2개 경유지
                    //c.printlinkedList(nearNodes);
                    //c.printList(c.resultList);
                    //2개 경유지
                    //System.out.println("리스트야 제대로 들어갔니?"+c.orderNodes(2,nearNodes).size());
                    //c.printList(nearNodes);//내용 확인


                }else{
                    MarkerOptions mOptions = new MarkerOptions();
                    // 마커 타이틀
                    mOptions.title("마커 좌표");
                    final Double latitude = point.latitude; // 위도
                    final Double longitude = point.longitude; // 경도
                    // 마커의 스니펫(간단한 텍스트) 설정
                    mOptions.snippet("위도 : "+latitude.toString() + "경도 : "+longitude.toString());
                    // LatLng: 위도 경도 쌍을 나타냄
                    mOptions.position(new LatLng(latitude, longitude));
                    // 마커(핀) 추가
                    gMap.addMarker(mOptions);

//                    gMap.moveCamera(CameraUpdateFactory.newLatLng(point));//마커 위치로 카메라 이동

                    count_marker++;
                    Log.d("LOG", "위도 : " + latitude+",경도 : " + longitude + ",터치횟수 :" + count_marker);

                }

            }
        });

    }

//    int[] polyColor={Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.BLACK};
//    public void drawRoute(ArrayList<ArrayList<LatLngAlt>> resultList){ // 맵에 경로 그리기
//        /**
//         * problem
//         * 경로가 overlap되는 부분이 있으면 width가 작은 것이 가려짐
//         * 맵에는 경로 하나만 띄울 수 있도록 함, 밑에 경로 리스트 중 하나를 선택 시 그 경로를 보여주는 식으로 변경
//         */
//        Log.d(TAG,"drawRoute()");
//        Polyline[] polylines=new Polyline[resultList.size()];
//
//        for(int i=0;i<resultList.size();i++){
//            ArrayList<LatLngAlt> list=resultList.get(i);
//            for(int j=0;j<list.size()-1;j++){
//                LatLngAlt src=list.get(j);
//                LatLngAlt dest=list.get(j+1);
//                polylines[i]=gMap.addPolyline(
//                        new PolylineOptions().add(
//                                new LatLng(src.latitude, src.longitude),
//                                new LatLng(dest.latitude, dest.longitude)
//                        ).width(10-2*i).color(polyColor[i])/*.geodesic(true)*/
//                );
////                polylines[i].setZIndex((float) Math.pow(100,i));
//
//            }
//        }
//
//    }
}

