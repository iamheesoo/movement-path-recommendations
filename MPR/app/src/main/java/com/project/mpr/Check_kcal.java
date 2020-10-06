package com.project.mpr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Check_kcal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_kcal);
    }
    public void goMain(View view) {//메인 페이지로 이동
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);

        switch (view.getId()){
            case R.id.but1 :
                intent.putExtra("calorie",0);
                break;
            case R.id.but2 :
                intent.putExtra("calorie",6);
                break;
            case R.id.but3 :
                intent.putExtra("calorie",9);
                break;
            case R.id.but4 :
                intent.putExtra("calorie",12);
                break;
            case R.id.but5 :
                intent.putExtra("calorie",15);
                break;
            case R.id.but6 :
                intent.putExtra("calorie",18);
                break;
            case R.id.but7 :
                intent.putExtra("calorie",21);
                break;
            case R.id.but8 :
                intent.putExtra("calorie",24);
                break;
            case R.id.but9 :
                intent.putExtra("calorie",27);
                break;
        }
        startActivity(intent);
    }

}