package com.dsr_practice.car_workshop.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dsr_practice.car_workshop.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Информация о задаче");
    }
}
