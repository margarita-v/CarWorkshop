package com.dsr_practice.car_workshop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Spinner;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel, spinnerWorks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        setTitle("Новая задача");

        etVIN = (EditText) findViewById(R.id.etVIN);
        etNumber = (EditText) findViewById(R.id.etNumber);
        spinnerMark = (Spinner) findViewById(R.id.spinnerMark);
        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerWorks = (Spinner) findViewById(R.id.spinnerWorks);
    }
}
