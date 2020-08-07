package com.project.mpr;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetNode extends Thread{
    private static String TAG="GetNode";
    ArrayList<ArrayList<LatLngAlt>> resultList; // 모든 경로를 포함한 리스트
    ArrayList<LatLngAlt> list; // 경로 노드 리스트
    ArrayList<String> combList; // 조합 리스트
    String jsonData;
    String passList;

    ArrayList<LatLng> nodeList=new ArrayList<>();

    public ArrayList<ArrayList<LatLngAlt>> getNode(final LatLng start, final LatLng end) {
        /**
         * v 이제 combList를 받아서
         * v 경유지 포함한 url 만들 수 있게 메소드 수정 후 리턴 받아서
         * 경로 max 5니까 totalTime과 userTime 체크해서 초과하는 것은 거르고
         * v drawRoute()로 맵에 띄우기
         *
         * problem
         * v 같은 json을 리턴받는 경우
         * v 리턴 경로가 없는 경우 (FileNotFoundException)
         * 원래 최단경로도 보여주면 좋을 듯
         */
//        CalNodes getnode = new CalNodes(); // 경유지 가져오기
//        nodeList=getnode.calDist(2,end);

        combList=getCombList(2); // num 수정
        resultList=new ArrayList<>();

        // temp
        /*nodeList=new ArrayList<>();
        nodeList.add(new LatLng(36.368880, 127.341553)); // 인문대학 위 교차로
        nodeList.add(new LatLng(36.369278, 127.345920)); // 중앙도서관 앞 교차로
        Log.i(TAG, nodeList.size()+"");*/

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
                    if(list.size()!=0) resultList.add(list);
                }
            }
        };
        thread.start();

        try{
            thread.join(); // 쓰레드 종료 후 list 리턴
            Log.d(TAG, "resultList size: "+resultList.size());
            GetAltitude ga=new GetAltitude(); // 고도 받아오기
            ga.setAltitude(list);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return resultList;
    }

    public void jsonRead(String json){ // 파싱
        Log.i(TAG, "jsonRead()");
        int totalTime, totalDistance; // 경로 소요 시간
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
//                    Log.i(TAG, ""+MainActivity.userTime);
//                    if(totalTime>MainActivity.userTime){ // userTime보다 크면 경로에 안넣음
//                        Log.i(TAG, "totalTime>userTime");
//                        return;
//                    }
                }
                // 노드 파싱
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