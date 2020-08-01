package com.project.mpr;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetNode extends Thread{
    private static String TAG="GetNode";
    public ArrayList<LatLngAlt> list; // 경로 노드 리스트
    String jsonData;

    public ArrayList<LatLngAlt> getNode(final LatLng start, final LatLng end) {
        list=new ArrayList<>();
        /**
         * 이제 combList를 받아서
         * 경유지 포함한 url 만들 수 있게 메소드 수정 후 리턴 받아서
         * 경로 max 5니까 totalTime과 userTime 체크해서 초과하는 것은 거르고
         * drawRoute()로 맵에 띄우기
         */
        Thread thread=new Thread() {
            public void run() {
                HttpConnect h = new HttpConnect();
                String url = h.getDirectionURL(start, end);
                jsonData = h.httpConnection(url);
                jsonRead(jsonData);
                Log.d(TAG, "Node Size: "+list.size());
            }
        };
        thread.start();

        try{
            thread.join(); // 쓰레드 종료 후 list 리턴
            GetAltitude ga=new GetAltitude(); // 고도 받아오기
            ga.setAltitude(list);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return list;
    }

    public void jsonRead(String json){ // 파싱
        try {
            JSONObject jsonObj = new JSONObject(json);
            String features=jsonObj.getString("features");
            JSONArray fArray=new JSONArray(features);
            for(int i=0;i<fArray.length();i++){
                JSONObject jObject=fArray.getJSONObject(i);
                JSONObject geometry=jObject.getJSONObject("geometry");
                String type=geometry.getString("type");
                if(type.equals("LineString")){
                    String coordinates=geometry.getString("coordinates");
                    coordinates=coordinates.replaceAll("[\\[\\]]", "");// 대괄호 삭제
                    String[] latlng=coordinates.split(",");
                    for(int j=0;j<latlng.length;j+=2)
                        list.add(new LatLngAlt(Double.parseDouble(latlng[j+1]), Double.parseDouble(latlng[j]), 0));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void printNode(ArrayList<LatLngAlt> list){ // 노드들 리스트 값 출력
        Log.d(TAG, "printNode()");
        for(LatLngAlt point:list)
            Log.d(TAG, point.latitude+", "+point.longitude+", "+point.altitude+"\n");
    }

    public ArrayList<String> getCombList(int num){ // 경유지 인덱스 조합 구하기
        ArrayList<String> combList=new ArrayList<>();
        boolean[] visit;
        for(int i=0;i<num;i++){
            visit=new boolean[num];
            comb(visit, 0, num, i, "", combList);
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