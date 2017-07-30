package com.dsr_practice.car_workshop.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;
import com.dsr_practice.car_workshop.models.common.Job;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel;
    Button btnAddWork;
    TextView tvWorkList;

    private ContentResolver contentResolver;
    private int[] viewsId = {android.R.id.text1};
    private String markId;
    private AlertDialog dialog;
    private List<Job> chosenJobs;

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
        btnAddWork = (Button) findViewById(R.id.btnAddWork);
        tvWorkList = (TextView) findViewById(R.id.tvWorkList);

        // Load data for spinners from database
        contentResolver = getContentResolver();
        chosenJobs = new ArrayList<>();

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

        // Load mark info
        Cursor markCursor = contentResolver.query(
                Contract.MarkEntry.CONTENT_URI,
                Contract.MARK_PROJECTION,
                null, null, null);
        SimpleCursorAdapter markAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                markCursor,
                Contract.MARK_PROJECTION,
                viewsId,
                0
        );
        markAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMark.setAdapter(markAdapter);
        markId = getMarkIdToString(markCursor);

        // Load models for chosen mark
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
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                ListAdapter adapter = listView.getAdapter();
                chosenJobs.clear();
                tvWorkList.setText("");
                for (int i = 0; i < checkedItems.size(); i++) {
                    int key = checkedItems.keyAt(i);
                    if (checkedItems.get(key)) {
                        Cursor cursor = (Cursor) adapter.getItem(key);
                        int id = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_NAME));
                        int price = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_PRICE));
                        chosenJobs.add(new Job(id, price, name));
                    }
                }
                for (Job job: chosenJobs) {
                    tvWorkList.append(job.getName().concat("\n"));
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
