package com.dsr_practice.car_workshop.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel, spinnerWorks;
    Button btnAddWork;

    private ContentResolver contentResolver;
    private int[] viewsId = {android.R.id.text1};
    private String markId;
    private AlertDialog dialog;

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
        btnAddWork = (Button) findViewById(R.id.btnAddWork);

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
        //loadSpinnerInfo(Contract.JobEntry.CONTENT_URI, Contract.JOB_PROJECTION, spinnerWorks);

        spinnerMark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                markId = getMarkIdToString(cursor);
                loadModelsForMark();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get all jobs from database
        final Cursor jobCursor = contentResolver.query(
                Contract.JobEntry.CONTENT_URI,
                Contract.JOB_PROJECTION,
                null, null, null);

        // Create dialog for job choice
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_work);
        builder.setMultiChoiceItems(
                jobCursor,
                Contract.JobEntry.COLUMN_NAME_PRICE,
                Contract.JobEntry.COLUMN_NAME_JOB_NAME,
                new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                            }
                        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog)dialog).getListView();
                long[] checkedItemIds = listView.getCheckedItemIds();
                ListAdapter adapter = listView.getAdapter();
                for (int i = 0; i < checkedItemIds.length; i++) {
                    Cursor chosenJob = (Cursor) adapter.getItem(i);
                    //TODO Get list of chosen jobs
                }
            }
        });
        dialog = builder.create();

        // Show dialog with jobs on button click
        btnAddWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    // Load info for marks and jobs spinners
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
        if (uri == Contract.MarkEntry.CONTENT_URI)
            markId = getMarkIdToString(cursor);
    }

    // Get mark ID for chosen mark
    private String getMarkIdToString(Cursor cursor) {
        int markId = cursor.getInt(cursor.getColumnIndex(Contract.MarkEntry.COLUMN_NAME_MARK_ID));
        return Integer.toString(markId);
    }

    // Load models for chosen mark
    private void loadModelsForMark() {
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
