package com.dsr_practice.car_workshop.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel, spinnerWorks;

    private ContentResolver contentResolver;
    private int[] viewsId = {android.R.id.text1};

    SimpleCursorAdapter markAdapter;
    SimpleCursorAdapter jobAdapter;

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
        contentResolver = getContentResolver();

        // Marks
        markAdapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item,
                null, Contract.MARK_PROJECTION,
                viewsId, 0);
        markAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMark.setAdapter(markAdapter);

        // Jobs
        jobAdapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item,
                null, Contract.JOB_PROJECTION,
                viewsId, 0);
        jobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorks.setAdapter(jobAdapter);

        loadSpinnerInfo(Contract.MarkEntry.CONTENT_URI, Contract.MARK_PROJECTION, spinnerMark);
        loadSpinnerInfo(Contract.ModelEntry.CONTENT_URI, Contract.MODEL_PROJECTION, spinnerModel);
        //TODO Multiple choice for jobs
        loadSpinnerInfo(Contract.JobEntry.CONTENT_URI, Contract.JOB_PROJECTION, spinnerWorks);
    }

    private void loadSpinnerInfo(Uri uri, String[] projection, Spinner spinner) {
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                projection,
                viewsId,
                0
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    static class ItemLoader extends CursorLoader {
        Uri uri;
        String[] projection;

        public ItemLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            this.uri = uri;
            this.projection = projection;
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver().query(uri, projection, null, null, null);
        }
    }
}
