package com.project.mpr;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Route extends Service {
    private static String TAG="Route";
    ArrayList<ArrayList<LatLngAlt>> resultList; // 모든 경로를 포함한 리스트
    ArrayList<LatLngAlt> list; // 경로 노드 리스트
    ArrayList<String> combList; // 조합 리스트
    String jsonData;
    String passList;
    double kcal;
    ArrayList<SolRoute> solutionList;
    int totalTime, totalDistance; // 경로 소요 시간

    ArrayList<LatLng> nodeList;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        solutionList = new ArrayList<>();
        resultList=new ArrayList<>();
        nodeList=new ArrayList<>();

        final LatLng start=(LatLng)intent.getExtras().get("start");
        final LatLng end=(LatLng)intent.getExtras().get("end");
        int num=intent.getExtras().getInt("num");
        nodeList=(ArrayList<LatLng>) intent.getSerializableExtra("nodeList");
        combList=getCombList(num); // num 수정

        Thread thread=new Thread() {
        public void run() {
            HttpConnect h = new HttpConnect();
            for(int i=0;i<combList.size();i++){ // 조합 수 만큼 경유지 포함한 경로 요청
                list=new ArrayList<>();
                passList="";
                String[] stopoverIdx=combList.get(i).split(",");
                for (String idx:stopoverIdx) { // passList 생성
                    LatLng node=nodeList.get(Integer.parseInt(idx));
                    if(!passList.equals("")) passList+=",";
                    passList+=node.longitude+","+node.latitude;
                }
                String url = h.getDirectionURL(start, end);
                url+=passList;
                jsonData = h.httpConnection(url);
                if(!jsonData.equals("")) jsonRead(jsonData);
                Log.d(TAG, "Node Size: "+list.size());
                if(list.size()!=0) {
                    resultList.add(list);//지워라
                    GetAltitude ga=new GetAltitude(); // 고도 받아오기
                    ga.setAltitude(list);

                    Calories calories=new Calories(); // 칼로리 계산
                    kcal=calories.getCalories(list, totalDistance, totalTime);
                    solutionList.add(new SolRoute(list,totalDistance,totalTime,kcal));//경로 정보 저장
                }

            }
        }
    };
        thread.start();

        try{
            thread.join(); // 쓰레드 종료 후 list 리턴
            Log.d(TAG, "resultList size: "+resultList.size());
            Log.d(TAG, "solutionList size: "+solutionList.size());

            //solutionList를 Main한테 전달
            Intent intent1=new Intent("route");
            intent1.putExtra("solutionList", solutionList);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        }catch(InterruptedException e){
            e.printStackTrace();
        }



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind()");
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return null;
    }
    public void jsonRead(String json){ // 파싱
        Log.i(TAG, "jsonRead()");
        try {
            JSONObject jsonObj = new JSONObject(json);
            String features=jsonObj.getString("features");
            JSONArray fArray=new JSONArray(features);
            for(int i=0;i<fArray.length();i++){
                JSONObject jObject=fArray.getJSONObject(i);
                if(i==0) { // totalTime 파싱
                    JSONObject properties=jObject.getJSONObject("properties");
                    totalTime=properties.getInt("totalTime");
                    totalDistance=properties.getInt("totalDistance");
                    Log.i(TAG, "totalTime: "+totalTime+" totalDistance: "+totalDistance);
                }
                // 노드 파싱
                JSONObject geometry=jObject.getJSONObject("geometry");

                String type=geometry.getString("type");
                if(type.equals("LineString")){
                    String coordinates=geometry.getString("coordinates");
                    coordinates=coordinates.replaceAll("[\\[\\]]", "");// 대괄호 삭제
                    String[] latlng=coordinates.split(",");
                    for(int j=0;j<latlng.length;j+=2)
                        list.add(new LatLngAlt(Double.parseDouble(latlng[j+1]), Double.parseDouble(latlng[j])));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> getCombList(int num){ // 경유지 인덱스 조합 구하기
        ArrayList<String> combList=new ArrayList<>();
        boolean[] visit;
        for(int i=0;i<num;i++){
            visit=new boolean[num];
            comb(visit, 0, num, i+1, "", combList);
        }
        return combList;
    }

    public void comb(boolean[] visited, int start, int n, int r, String nowComb, ArrayList<String> list){ // 조합
        // 파라미터: (사용여부 체크 배열, 총 숫자 개수, 뽑을 개수, 현재까지 뽑은 숫자들, 조합을 저장할 리스트)
        if(r==0) { // r개 만큼 뽑기 완료
            list.add(nowComb);
            return;
        }
        for(int i=start;i<n;i++) {
            visited[i] = true; // 인덱스 i를 뽑는 경우
            comb(visited, i+1, n, r-1, nowComb+i+",", list);
            visited[i] = false; // 안뽑는 경우
        }
    }
}
