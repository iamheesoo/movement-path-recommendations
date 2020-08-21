package com.project.mpr;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Calendar extends Service {
    private static final String TAG="Calendar";
    double calendarSec=0;

    @Override
    public void onCreate(){ // 서비스 최초 생성될 때 호출
        this.calendarSec=Double.MAX_VALUE;
        Log.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){ // startService()로 서비스를 시작할 때 호출
        Log.i(TAG, "onStartCommand()");
        final String CONTENT_URI="content://com.android.calendar/events";
        Uri uri=Uri.parse(CONTENT_URI);
        Cursor cursor=null;
        final String[] EVENT_PROJECTION=new String[]{ "calendar_id", "title", "dtstart", "dtend"};
        Long currentTimeMillis=System.currentTimeMillis();
        String currentDate=milliToDate(currentTimeMillis);
        String selection="calendar_id=4 AND dtstart>"+currentTimeMillis;
        cursor=getContentResolver().query(uri, EVENT_PROJECTION, selection, null, "dtstart asc");
        long startTime=0;
        if(cursor.moveToNext()){
//            Log.i(TAG,"calendar_id: "+cursor.getString(0));
//            Log.i(TAG,"title: "+cursor.getString(1));
//            Log.i(TAG,"start time: "+milliToDate(cursor.getLong(2)));
//            Log.i(TAG,"end time: "+milliToDate(cursor.getLong(3)));
            startTime=cursor.getLong(2);
        }
        Log.i(TAG, "current Date: "+currentDate+" eventDate: "+milliToDate(startTime));
        if(currentDate.equals(milliToDate(startTime))){
            Log.i(TAG, "today event exist");
            this.calendarSec=(startTime-currentTimeMillis)/1000.0;
        }
        Log.i(TAG, calendarSec+"sec");

        Intent intent1=new Intent("calendar");
        intent1.putExtra("times", calendarSec);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // bindService()로 바인딩을 실행할 때 호출
        Log.i(TAG, "onBind()");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent){ // unbindService()로 바인딩을 해제할 때 호출
        Log.i(TAG, "onUnbind()");
        return true;
    }

    @Override
    public void onRebind(Intent intent){ // 이미 onUnbind()가 호출된 후에 bindService()로 바인딩을 실행할 때때
        Log.i(TAG, "onRebind()");
    }

    @Override
    public void onDestroy(){ // 서비스가 소멸될 때 호출
        Log.i(TAG, "onDestroy()");
    }

    private String milliToDate(long millis){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(millis));
    }

}
