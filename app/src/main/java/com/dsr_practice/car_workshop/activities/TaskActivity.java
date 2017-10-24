package com.dsr_practice.car_workshop.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.JobAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.loaders.CreateTaskLoader;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.post.TaskPost;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;

public class TaskActivity extends AppCompatActivity
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    //region Widgets
    private EditText etVIN, etNumber;
    private Spinner spinnerMark, spinnerModel;
    private ListView lvJobs;
    private Button btnSaveTask;
    private ProgressBar progressBar;
    //endregion

    //region Adapters
    private SimpleCursorAdapter markAdapter, modelAdapter;
    private JobAdapter jobAdapter;

    // Flag which shows if we should load models for mark (used on restore instance state)
    private boolean needLoad = false;
    //endregion

    //region View's IDs for adapters
    private static final int[] VIEWS_ID = {android.R.id.text1};
    private static final int[] JOB_IDS = {R.id.cbName, R.id.tvPrice};
    private static final int SPINNER_ITEM = android.R.layout.simple_spinner_item;
    private static final int SPINNER_DROPDOWN_ITEM = android.R.layout.simple_spinner_dropdown_item;
    //endregion

    // IDs for loaders
    private static final int MARK_LOADER_ID = 0, MODEL_LOADER_ID = 1, JOB_LOADER_ID = 2;

    // Values for user input validation
    private static final int VIN_MAX_LENGTH = 17, NUMBER_MIN_LENGTH = 6;

    // Task fields
    private int markId, modelId, markPosition, modelPosition;

    // Chosen jobs list
    private List<Job> chosenJobs;
    // Positions of chosen jobs
    private boolean[] checkedPositions;

    //region String keys for bundle
    private static final String MARK_ID = "MARK_ID";
    private static final String MODEL_ID = "MODEL_ID";
    private static final String MARK_POSITION = "MARK_POSITION";
    private static final String MODEL_POSITION = "MODEL_POSITION";
    private static final String VIN = "VIN";
    private static final String NUMBER = "NUMBER";
    private static final String JOBS = "JOBS";
    //endregion

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        chosenJobs = new ArrayList<>();

        //region Find widgets by IDs and set listeners
        View listHeader = View.inflate(this, R.layout.task_header, null);
        View listFooter = View.inflate(this, R.layout.task_footer, null);

        etVIN = listHeader.findViewById(R.id.etVIN);
        etNumber = listHeader.findViewById(R.id.etNumber);
        spinnerMark = listHeader.findViewById(R.id.spinnerMark);
        spinnerModel = listHeader.findViewById(R.id.spinnerModel);
        btnSaveTask = listFooter.findViewById(R.id.btnSaveTask);
        progressBar = listFooter.findViewById(R.id.progressBar);

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

        //region Configure list view
        lvJobs = (ListView) findViewById(R.id.lvJobs);
        lvJobs.addHeaderView(listHeader);
        lvJobs.addFooterView(listFooter);
        lvJobs.setAdapter(jobAdapter);
        lvJobs.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Decrease position because list view has header
                boolean isChecked = jobAdapter.check(--position);
                CheckBox cbName = view.findViewById(R.id.cbName);
                cbName.setChecked(isChecked);
            }
        });
        //endregion

        //region Restore saved data
        if (savedInstanceState != null) {
            markId = savedInstanceState.getInt(MARK_ID);
            modelId = savedInstanceState.getInt(MODEL_ID);
            markPosition = savedInstanceState.getInt(MARK_POSITION);
            modelPosition = savedInstanceState.getInt(MODEL_POSITION);
            etVIN.setText(savedInstanceState.getString(VIN));
            etNumber.setText(savedInstanceState.getString(NUMBER));
            checkedPositions = savedInstanceState.getBooleanArray(JOBS);
        }
        //endregion

        // Load info from database
        getSupportLoaderManager().initLoader(MARK_LOADER_ID, null, this);
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
        final Calendar calendar = Calendar.getInstance();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
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
                    setViewsEnabled(false);

                    TaskPost taskPostObject = new TaskPost(
                            markId, modelId, dateFormat.format(calendar.getTime()), vin, number, chosenJobs);
                    getSupportLoaderManager().restartLoader(CreateTaskLoader.CREATE_TASK_ID, null,
                            new CreateTaskCallbacks(taskPostObject));
                    /*
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TaskActivity.this, R.string.toast_create_task, Toast.LENGTH_SHORT).show();
                            TaskActivity.this.finish();
                        }
                    }, 1000);*/
                } // if checkInput...
            } //run()
        });
    }

    /**
     * Callbacks for task creation
     */
    private class CreateTaskCallbacks implements LoaderManager.LoaderCallbacks<ResponseBody> {

        private TaskPost taskPostObject;

        CreateTaskCallbacks(TaskPost taskPostObject) {
            this.taskPostObject = taskPostObject;
        }

        @Override
        public Loader<ResponseBody> onCreateLoader(int id, Bundle args) {
            return new CreateTaskLoader(TaskActivity.this, taskPostObject);
        }

        @Override
        public void onLoadFinished(Loader<ResponseBody> loader, ResponseBody data) {
            finishTaskCreation(data != null);
        }

        @Override
        public void onLoaderReset(Loader<ResponseBody> loader) {

        }
    }

    /**
     * Actions after task creation
     * @param success True if task creation was finished successfully
     */
    private void finishTaskCreation(boolean success) {
        if (success) {
            Toast.makeText(this, R.string.toast_create_task, Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            MessageDialog.showConnectionError(getSupportFragmentManager());
            setViewsEnabled(true);
        }
    }

    //region Methods for UI
    /**
     * Set views enabled state
     * @param enabled True if views should be enabled for user interaction
     */
    private void setViewsEnabled(boolean enabled) {
        etVIN.setEnabled(enabled);
        spinnerMark.setEnabled(enabled);
        spinnerModel.setEnabled(enabled);
        etNumber.setEnabled(enabled);
        lvJobs.setEnabled(enabled);
        btnSaveTask.setEnabled(enabled);
        progressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    /**
     * Create error dialog for task validation
     * @param titleId ID for title's string resource
     * @param messageId ID for message's string resource
     */
    private void createErrorDialog(int titleId, int messageId) {
        MessageDialog.showDialog(titleId, messageId, getSupportFragmentManager());
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
        if (number.length() < NUMBER_MIN_LENGTH) {
            createErrorDialog(R.string.invalid_number_title, R.string.invalid_number_message);
            return false;
        }
        if (chosenJobs.isEmpty()) {
            createErrorDialog(R.string.empty_jobs_title, R.string.empty_jobs_message);
            return false;
        }
        return true;
    }

    //endregion

    //region Methods for database interaction

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
     * Load models for chosen mark
     * @param markPosition Position of chosen mark
     */
    private void loadModels(int markPosition) {
        Cursor cursor = (Cursor) spinnerMark.getItemAtPosition(markPosition);
        markId = getItemId(cursor, Contract.MarkEntry.COLUMN_NAME_MARK_ID);
        getSupportLoaderManager().restartLoader(MODEL_LOADER_ID, null, this);
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

    //endregion

    //region Callbacks for loading all entries from database using CursorLoader
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
        if (data.getCount() > 0) {
            switch (loader.getId()) {
                case MARK_LOADER_ID:
                    markAdapter.swapCursor(data);
                    spinnerMark.setSelection(markPosition);
                    loadModels(markPosition);
                    break;
                case MODEL_LOADER_ID:
                    modelAdapter.swapCursor(data);
                    spinnerModel.setSelection(modelPosition);
                    getSupportLoaderManager().initLoader(JOB_LOADER_ID, null, this);
                    break;
                case JOB_LOADER_ID:
                    jobAdapter.swapCursor(data);
                    if (checkedPositions != null)
                        jobAdapter.setCheckedPositions(checkedPositions);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    //endregion
}
