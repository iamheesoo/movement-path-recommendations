package com.project.mpr;

import android.Manifest;
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


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    int count_marker = 0;
    LatLng start, end;
    private final String TAG = "MainActivity";
    private View mLayout;
    EditText editText;
    int userTime;

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
    public GoogleMap getMap(){
        return gMap;
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
        Log.d("!", school.latitude+" "+school.longitude);

        //핀 찍기
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng point) {

                if(count_marker==0){//출발지 좌표
                    start=point;
                }
                else if(count_marker==1){//목적지 좌표
                    end=point;
                }
                if(count_marker>=2){
                    count_marker++;
                    Log.d("____TEST____", "두 번 이상 클릭하면 안돼요~"+count_marker);
                    //Log.d(TAG, start.latitude+" "+ end.latitude);

                    //t-map api 호출 : 출발지->도착지 경로 좌표 구함
                    GetNode g=new GetNode();
                    ArrayList<LatLng> list=g.getNode(start, end); // 경로 노드 받아오기
                    drawRoute(list); // 경로 그리기

                    //중간 좌표 계산하기
                    CalClosedNodes c = new CalClosedNodes();
                    LatLng midXY = c.cal_middle_latlng(start,end);
                    //c.getFirebaseData();//------FIREBASE TEST-------
                    c.cal_five_latlng(1,end);


                    //중간 좌표 찍기
                    MarkerOptions midM = new MarkerOptions();
                    midM.title("중간 값 좌표");
                    midM.snippet("위도 : "+midXY.latitude+ "경도 : "+midXY.longitude);
                    midM.position(midXY);
                    gMap.addMarker(midM);

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

                    //고도 받아오기
                    Background background = new Background();
                    background.execute(point);
                }

            }
        });

    }
    public void drawRoute(ArrayList<LatLng> list){ // 맵에 경로 그리기
        Log.d(TAG,"drawRoute()");
        for(int i=0;i<list.size()-1;i++){
            LatLng src=list.get(i);
            LatLng dest=list.get(i+1);
            Polyline line=gMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude, dest.longitude)
                    ).width(5).color(Color.BLUE).geodesic(true)
            );
        }

    }
}

