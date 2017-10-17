package com.dsr_practice.car_workshop.activities;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.JobAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity
        implements View.OnClickListener, DialogInterface.OnClickListener {

    //region Widgets
    private EditText etVIN, etNumber;
    private Spinner spinnerMark, spinnerModel;
    //endregion

    //region Adapters for spinners and list view
    private SimpleCursorAdapter markAdapter, modelAdapter;
    private JobAdapter jobAdapter;
    private boolean needLoad = false;
    //endregion

    private CursorLoaderCallbacks callbacks;
    private SimpleDateFormat dateFormat;
    private static ApiInterface apiInterface;
    private static final String DIALOG_TAG = "DIALOG";

    // IDs for loaders
    private static final int MARK_LOADER_ID = 0, MODEL_LOADER_ID = 1, JOB_LOADER_ID = 2;

    //region View's IDs for adapters
    private static final int[] VIEWS_ID = {android.R.id.text1};
    private static final int[] JOB_IDS = {R.id.cbName, R.id.tvPrice};
    private static final int SPINNER_ITEM = android.R.layout.simple_spinner_item;
    private static final int SPINNER_DROPDOWN_ITEM = android.R.layout.simple_spinner_dropdown_item;
    //endregion

    // Chosen jobs list
    private List<Job> chosenJobs;
    // Positions of chosen jobs
    private boolean[] checkedPositions;

    // Task fields
    private int markId, modelId, markPosition, modelPosition;

    // Values for user input validation
    private static final int VIN_MAX_LENGTH = 17, NUMBER_MAX_LENGTH = 9;

    //region String keys for bundle
    private static final String MARK_ID = "MARK_ID";
    private static final String MODEL_ID = "MODEL_ID";
    private static final String MARK_POSITION = "MARK_POSITION";
    private static final String MODEL_POSITION = "MODEL_POSITION";
    private static final String VIN = "VIN";
    private static final String NUMBER = "NUMBER";
    private static final String JOBS = "JOBS";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        apiInterface = ApiClient.getApi();
        dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        chosenJobs = new ArrayList<>();

        // Get header view and footer view for list view
        LayoutInflater inflater = LayoutInflater.from(this);
        View listHeader = inflater.inflate(R.layout.task_header, null);
        View listFooter = inflater.inflate(R.layout.task_footer, null);

        //region Find widgets by IDs and set listeners
        etVIN = listHeader.findViewById(R.id.etVIN);
        etNumber = listHeader.findViewById(R.id.etNumber);
        spinnerMark = listHeader.findViewById(R.id.spinnerMark);
        spinnerModel = listHeader.findViewById(R.id.spinnerModel);
        Button btnSaveTask = listFooter.findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(this);
        spinnerMark.setOnItemSelectedListener(onItemSelectedListener);
        spinnerModel.setOnItemSelectedListener(onItemSelectedListener);
        //endregion

        //region Create adapters
        markAdapter = configureAdapter(Contract.MARK_PROJECTION, false);
        spinnerMark.setAdapter(markAdapter);

        modelAdapter = configureAdapter(Contract.MODEL_PROJECTION, false);
        spinnerModel.setAdapter(modelAdapter);

        jobAdapter = (JobAdapter) configureAdapter(Contract.JOB_PROJECTION, true);
        //endregion

        // Configure list view
        ListView lvJobs = (ListView) findViewById(R.id.lvJobs);
        lvJobs.addHeaderView(listHeader);
        lvJobs.addFooterView(listFooter);
        lvJobs.setAdapter(jobAdapter);
        lvJobs.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = jobAdapter.check(--position);
                CheckBox cbName = view.findViewById(R.id.cbName);
                cbName.setChecked(isChecked);
            }
        });

        // Restore saved data
        if (savedInstanceState != null) {
            markId = savedInstanceState.getInt(MARK_ID);
            modelId = savedInstanceState.getInt(MODEL_ID);
            markPosition = savedInstanceState.getInt(MARK_POSITION);
            modelPosition = savedInstanceState.getInt(MODEL_POSITION);
            etVIN.setText(savedInstanceState.getString(VIN));
            etNumber.setText(savedInstanceState.getString(NUMBER));
            checkedPositions = savedInstanceState.getBooleanArray(JOBS);
        }

        // Load info from database
        callbacks = new CursorLoaderCallbacks();
        getSupportLoaderManager().initLoader(MARK_LOADER_ID, null, callbacks);
        getSupportLoaderManager().initLoader(JOB_LOADER_ID, null, callbacks);
    }

    /**
     * Configure cursor adapter
     * @param projection Projection for query
     * @param isJobs True if adapter will be used for list of jobs
     * @return SimpleCursorAdapter for spinners OR JobAdapter for list of jobs
     */
    private SimpleCursorAdapter configureAdapter(String[] projection, boolean isJobs) {
        SimpleCursorAdapter result = isJobs
                ? new JobAdapter(this, R.layout.job_item, null, projection, JOB_IDS, 0)
                : new SimpleCursorAdapter(this, SPINNER_ITEM, null, projection, VIEWS_ID, 0);
        result.setDropDownViewResource(SPINNER_DROPDOWN_ITEM);
        return result;
    }

    /**
     * Spinner item selection listener
     */
    private AdapterView.OnItemSelectedListener onItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.spinnerMark: {
                    markPosition = position;
                    if (needLoad) {
                        loadModels(position);
                        modelPosition = 0;
                        spinnerModel.setSelection(0);
                    } else
                        needLoad = true;
                    break;
                }
                case R.id.spinnerModel: {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    modelId = getItemId(cursor, Contract.ModelEntry.COLUMN_NAME_MODEL_ID);
                    modelPosition = position;
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    /**
     * Save chosen task fields in Bundle
     * @param outState Bundle which will contain chosen task fields
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MARK_ID, markId);
        outState.putInt(MODEL_ID, modelId);
        outState.putInt(MARK_POSITION, markPosition);
        outState.putInt(MODEL_POSITION, modelPosition);
        outState.putString(VIN, etVIN.getText().toString());
        outState.putString(NUMBER, etNumber.getText().toString());
        if (jobAdapter.getCheckedCount() > 0)
            outState.putBooleanArray(JOBS, jobAdapter.getCheckedPositions());
    }

    /**
     * Button's OnClickListener
     * @param v Button which was clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveTask:
                String vin = etVIN.getText().toString(), number = etNumber.getText().toString();
                // Get chosen jobs
                chosenJobs.clear();
                for (int i = 0; i < jobAdapter.getSize(); i++) {
                    if (jobAdapter.isChecked(i)) {
                        Cursor cursor = (Cursor) jobAdapter.getItem(i);
                        int id = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_NAME));
                        int price = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_PRICE));
                        chosenJobs.add(new Job(id, price, name));
                    }
                }
                if (checkInput(vin, number)) {
                    /*
                    Calendar calendar = Calendar.getInstance();
                    String date = dateFormat.format(calendar.getTime());
                    apiInterface.createTask(new TaskPost(markId, modelId, date, vin, number, chosenJobs))
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Toast.makeText(TaskActivity.this, R.string.toast_create_task, Toast.LENGTH_SHORT).show();
                                    TaskActivity.this.finish();
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(TaskActivity.this, R.string.toast_cant_create, Toast.LENGTH_SHORT).show();
                                }
                            });*/
                    Toast.makeText(this, R.string.toast_create_task, Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
        }
    }

    /**
     * Dialog OnClickListener for dismiss dialogs
     * @param dialog Dialog which was shown
     * @param which ID of chosen dialog's button
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }

    /**
     * Get ID for chosen mark or model
     * @param cursor Current cursor
     * @param columnName Column name for ID
     * @return Entity's ID
     */
    private int getItemId(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * Check all task fields
     * @param vin VIN value
     * @param number Car number
     * @return True if all task fields are correct
     */
    private boolean checkInput(String vin, String number) {
        if (vin.equals("") || number.equals("")) {
            createErrorDialog(R.string.empty_fields_title, R.string.empty_fields_message);
            return false;
        }
        if (vin.length() < VIN_MAX_LENGTH) {
            createErrorDialog(R.string.invalid_vin_title, R.string.invalid_vin_message);
            return false;
        }
        if (number.length() < NUMBER_MAX_LENGTH) {
            createErrorDialog(R.string.invalid_number_title, R.string.invalid_number_message);
            return false;
        }
        if (chosenJobs.isEmpty()) {
            createErrorDialog(R.string.empty_jobs_title, R.string.empty_jobs_message);
            return false;
        }
        return true;
    }

    /**
     * Create error dialog for task validation
     * @param titleId ID for title's string resource
     * @param messageId ID for message's string resource
     */
    private void createErrorDialog(int titleId, int messageId) {
        MessageDialog.newInstance(titleId, messageId, false)
                .show(getSupportFragmentManager(), DIALOG_TAG);
    }

    /**
     * Load models for chosen mark
     * @param markPosition Position of chosen mark
     */
    private void loadModels(int markPosition) {
        Cursor cursor = (Cursor) spinnerMark.getItemAtPosition(markPosition);
        markId = getItemId(cursor, Contract.MarkEntry.COLUMN_NAME_MARK_ID);
        getSupportLoaderManager().restartLoader(MODEL_LOADER_ID, null, callbacks);
    }

    /**
     * Class for loading all entries from database using CursorLoader
     */
    private class CursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case MARK_LOADER_ID:
                    return new CursorLoader(
                            TaskActivity.this,
                            Contract.MarkEntry.CONTENT_URI, Contract.MARK_PROJECTION,
                            null, null, null);
                case MODEL_LOADER_ID:
                    return new CursorLoader(
                            TaskActivity.this,
                            Provider.URI_MODELS_FOR_MARK, Contract.MODEL_PROJECTION,
                            null, new String[] {Integer.toString(markId)}, null);
                case JOB_LOADER_ID:
                    return new CursorLoader(
                            TaskActivity.this,
                            Contract.JobEntry.CONTENT_URI, Contract.JOB_PROJECTION,
                            null, null, null);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case MARK_LOADER_ID:
                    markAdapter.swapCursor(data);
                    spinnerMark.setSelection(markPosition);
                    loadModels(markPosition);
                    break;
                case MODEL_LOADER_ID:
                    modelAdapter.swapCursor(data);
                    spinnerModel.setSelection(modelPosition);
                    break;
                case JOB_LOADER_ID:
                    jobAdapter.swapCursor(data);
                    if (checkedPositions != null)
                        jobAdapter.setCheckedPositions(checkedPositions);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
