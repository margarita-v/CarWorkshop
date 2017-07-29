package com.dsr_practice.car_workshop.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel, spinnerWorks;

    private ContentResolver contentResolver;
    private int[] viewsId = {android.R.id.text1};
    private String markId;

    //SimpleCursorAdapter markAdapter;
    //SimpleCursorAdapter jobAdapter;

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

        /*
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
        spinnerWorks.setAdapter(jobAdapter);*/

        loadSpinnerInfo(Contract.MarkEntry.CONTENT_URI, Contract.MARK_PROJECTION, spinnerMark);
        //TODO Multiple choice for jobs
        loadSpinnerInfo(Contract.JobEntry.CONTENT_URI, Contract.JOB_PROJECTION, spinnerWorks);

        // Load models for concrete mark
        spinnerModel.setVisibility(View.VISIBLE);
        Cursor cursor = contentResolver.query(
                Provider.URI_MODELS_FOR_MARK,
                Contract.MODEL_PROJECTION,
                null, new String[] {markId}, null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                Contract.MODEL_PROJECTION,
                viewsId,
                0
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModel.setAdapter(adapter);
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
        if (uri == Contract.MarkEntry.CONTENT_URI && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(Contract.MarkEntry.COLUMN_NAME_MARK_ID));
            markId = Integer.toString(id);
        }
    }

    /*
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
    }*/
}
