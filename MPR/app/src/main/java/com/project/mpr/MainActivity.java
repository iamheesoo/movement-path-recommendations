package com.project.mpr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private BroadcastReceiver calendarReceiver, calculateReceiver, routeReceiver;
    int count_marker = 0;
    LatLng start, end;
    private final String TAG = "MainActivity";
    private View mLayout;
    static Context mContext;
    double times;
    double calorie;

    ArrayList<LatLng> nodeList;
    ArrayList<SolRoute> solutionList;


    private ProgressDialog waitDialog;
    Handler mHandler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout=findViewById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext=getApplicationContext();

        checkPermission();

        calendarReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive()");
                times=intent.getDoubleExtra("times", -1);
                Log.i(TAG, "times: "+times);
            }
        };
        calculateReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive()");
                nodeList= (ArrayList<LatLng>) intent.getSerializableExtra("nodeList");
                Log.i(TAG, "nodeList size: "+nodeList.size());

                // 경유지 그리기
                drawCross(nodeList);

                Intent intentRoute=new Intent(getApplicationContext(), Route.class);
                intentRoute.putExtra("start", start);
                intentRoute.putExtra("end", end);
                intentRoute.putExtra("num", 4);
                intentRoute.putExtra("nodeList", nodeList);
                startService(intentRoute);
            }
        };
        routeReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive()");
                solutionList=(ArrayList<SolRoute>)intent.getSerializableExtra("solutionList");
                Log.i(TAG, "solutionList size: "+solutionList.size());

                mHandler.removeCallbacks(runnable);
                waitDialog.cancel();
                if(solutionList.size()!=0) {
                    ArrayList<SolRoute> listforDraw = checkKcal(solutionList, calorie);
                    drawRoute(listforDraw);

                    HorizontalScrollView horizontalScrollView;
                    horizontalScrollView=(HorizontalScrollView)findViewById(R.id.routeList);
                    horizontalScrollView.setVisibility(View.VISIBLE);

                   LinearLayout linearLayout;
                    linearLayout=(LinearLayout)findViewById(R.id.linear1);


                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
                    lp.gravity=Gravity.CENTER;
                    int c=0;
                    for(SolRoute item : listforDraw){
                        TextView tv = new TextView(mContext);  // 새로 추가할 textView 생성
                        tv.setId(c);
                        tv.setText("경로 "+(++c)+" \n"+item.time+" 초 \n"+item.meter+" m \n"+item.calories+" kcal \n");  // textView에 내용 추가
                        tv.setTextSize(15);
                        tv.setTextColor(Color.WHITE);
                        tv.setLayoutParams(lp);  // textView layout 설정
                        tv.setGravity(Gravity.CENTER);  // textView layout 설정
                        tv.setPadding(20, 0, 20, 0);
                        tv.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                drawOneRoute(v.getId()+"");
                            }

                        });
                        linearLayout.addView(tv); // 기존 linearLayout에 textView 추가
                    }


                }
                else{
                    Toast.makeText(getApplicationContext(), "추천 경로 없음", Toast.LENGTH_LONG).show();
                }
            }
        };

        waitDialog=new ProgressDialog(this);
        waitDialog.setMessage("loading...");
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mHandler=new Handler(Looper.getMainLooper());
        runnable=new Runnable() {
            @Override
            public void run() {
                waitDialog.show();
            }
        };

    }


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CALENDAR};  // 외부 저장소
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    public void checkPermission(){
        Log.i(TAG, "checkPermission()");
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int hasReadCalendarPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
                && hasReadCalendarPermission == PackageManager.PERMISSION_GRANTED ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
        }
        else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치, 캘린더 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("확인", new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                                ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                            }
                        }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

    }
    public void registerReceiver(){
        // 캘린더 가져오기
        Intent intent=new Intent(getApplicationContext(), Calendar.class);
        startService(intent);

        // 리시버 등록
        IntentFilter filter=new IntentFilter();
        filter.addAction("calendar");
        registerReceiver(calendarReceiver, filter);
        IntentFilter filter1=new IntentFilter();
        filter1.addAction("calculate");
        registerReceiver(calculateReceiver, filter1);
        IntentFilter filter2=new IntentFilter();
        filter2.addAction("route");
        registerReceiver(routeReceiver, filter2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        LatLng school = new LatLng(36.362978, 127.344807); //충남대학교 정문
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(school,15));
        //핀 찍기
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng point) {
                Log.i(TAG, "count_marker "+count_marker);
                if(count_marker==0){//출발지 좌표
                    start=point;
                }
                else if(count_marker==1){//목적지 좌표
                    end=point;
                }
                if(count_marker==2){
                    count_marker++;

                    Intent intent = getIntent(); // 칼로리 가져오기
                    calorie = intent.getIntExtra("calorie",0); //set default kcal = 0
                    Log.i(TAG, "calorie "+calorie);

                    registerReceiver();

                    mHandler.postDelayed(runnable,0);

                    // 경유지 뽑기
                    Intent intentCal=new Intent(getApplicationContext(), Calculate.class);
                    intentCal.putExtra("start", start);
                    intentCal.putExtra("end", end);
                    intentCal.putExtra("num", 4);
                    startService(intentCal);



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

                }

            }
        });

    }


    public ArrayList<SolRoute> checkKcal(ArrayList<SolRoute> solRoutes, double kcal){
        /**
         * 사용자가 섭취한 칼로리를 소모할 수 있는 경로만 저장
         * */
        ArrayList<SolRoute> result = new ArrayList<>();
        for(int i=0;i<solRoutes.size();i++){
            if(solRoutes.get(i).calories>=kcal && solRoutes.get(i).time<=times){
                result.add(solRoutes.get(i));
            }
        }
        return result;
    }

    public void check_kcal(View view) {//섭취 칼로리 페이지로 이동
        Intent intent = new Intent(getApplicationContext(), Check_kcal.class);
        startActivity(intent);  //intent를 넣어 실행시키게 됩니다.
    }


    int[] polyColor={Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.BLACK, Color.MAGENTA, Color.DKGRAY, Color.CYAN, Color.LTGRAY, Color.WHITE};
    Polyline[] polylines;
    public void drawRoute(final ArrayList<SolRoute> resultList){ // 맵에 경로 그리기
        /**
         * problem
         * ArrayList<ArrayList<LatLngAlt>>
         * 경로가 overlap되는 부분이 있으면 width가 작은 것이 가려짐
         * 맵에는 경로 하나만 띄울 수 있도록 함, 밑에 경로 리스트 중 하나를 선택 시 그 경로를 보여주는 식으로 변경
         */
        Log.d(TAG,"drawRoute()");

        polylines=new Polyline[resultList.size()];
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
            polylines[i].setTag(resultList.get(i).time+","+resultList.get(i).meter+","+resultList.get(i).calories);

        }

        gMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){
            public void onPolylineClick(Polyline polyline) {
                int strokeColor = polyline.getColor() ^ 0x0000CC00;
                polyline.setColor(strokeColor);//클릭 시 색상 변경
                /**
                 * polyLine.getID()대신에 해당 폴리라인 리스트의 시간, 칼로리 내용으로 변경하기
                 * */

                String[] split = polyline.getTag().toString().split(",");
                Log.d(TAG, "시간:"+split[0]+",미터:"+split[1]+",칼로리:"+split[2]);
                makeAlertDialog(split);
            }
        });

    }

    public void drawOneRoute(String strIdx){
        int thisRoute=Integer.parseInt(strIdx);
        if(polylines!=null && polylines.length!=0){
            for(int i=0;i<polylines.length;i++){
                if(i!=thisRoute) polylines[i].setVisible(false);
                else polylines[i].setVisible(true);
            }
        }
    }

    public void drawCross(ArrayList<LatLng> nodeList) {
        MarkerOptions node = new MarkerOptions();
        node.title("node 좌표");
        node.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        for (int k = 0; k < nodeList.size(); k++) {
            node.snippet("위도 : " + nodeList.get(k).latitude + "경도 : " + nodeList.get(k).longitude);
            node.position(nodeList.get(k));
            gMap.addMarker(node);
        }
    }


    public void makeAlertDialog(String[] splits){//경로 클릭 시 경로 선택 화면
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle("경로 정보");
        alt_bld.setMessage(splits[0]+" sec\n"+splits[1]+" m\n"+splits[2]+" kcal\ncheck this path?").setCancelable(
                false).setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(),"no", Toast.LENGTH_SHORT).show();
                        // Action for 'NO' Button
                        dialog.cancel();
                    }
                }).setPositiveButton("ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(),"ok", Toast.LENGTH_SHORT).show();
                        // Action for 'Yes' Button
                    }
                });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        count_marker=0;
        LocalBroadcastManager.getInstance(this).registerReceiver(calendarReceiver, new IntentFilter("calendar"));
        LocalBroadcastManager.getInstance(this).registerReceiver(calculateReceiver, new IntentFilter("calculate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(routeReceiver, new IntentFilter("route"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(calendarReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(calculateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(routeReceiver);
    }

}

