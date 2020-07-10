package com.project.mpr;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    int count_marker=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng school = new LatLng(36.362978, 127.344807); //충남대학교 정문

        gMap.moveCamera(CameraUpdateFactory.newLatLng(school));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //핀 찍기
            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(final LatLng point) {
                    if(count_marker>=2){
                        Log.d("____TEST____", "두 번 이상 클릭하면 안돼요~"+count_marker);
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
                        count_marker++;
                        Log.d("LOG", "위도 : " + latitude+",경도 : " + longitude + ",터치횟수 :" + count_marker);

                        // 고도 받아오기
                        final GetAltitude g=new GetAltitude();
                        new Thread(){
                            public void run(){
                                g.getAltitude(point);
                            }
                        }.start();
                    }

                }
            });




    }


    }

