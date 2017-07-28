package com.dsr_practice.car_workshop.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel, spinnerWorks;
    private int[] viewsId = {android.R.id.text1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        setTitle(R.string.task_title);

        etVIN = (EditText) findViewById(R.id.etVIN);
        etNumber = (EditText) findViewById(R.id.etNumber);
        spinnerMark = (Spinner) findViewById(R.id.spinnerMark);
        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerWorks = (Spinner) findViewById(R.id.spinnerWorks);

        // Load data for spinners from database
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Contract.JobEntry.CONTENT_URI,
                Contract.JOB_NAMES_PROJECTION, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                Contract.JOB_NAMES_PROJECTION,
                viewsId,
                0
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorks.setAdapter(adapter);
    }
}
