package com.dsr_practice.car_workshop.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArraySet;
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
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.database.Provider;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskActivity extends AppCompatActivity implements
        View.OnClickListener,
        DialogInterface.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    //region Widgets
    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel;
    Button btnSaveTask;
    ListView lvJobs;
    //endregion

    //region Adapters for spinners and list view
    private SimpleCursorAdapter markAdapter;
    private SimpleCursorAdapter modelAdapter;
    private SimpleCursorAdapter jobAdapter;
    private boolean needLoad = false;
    //endregion

    private SimpleDateFormat dateFormat;
    private static ApiInterface apiInterface;
    private static final String DIALOG_TAG = "DIALOG";

    //region IDs for loaders
    private static final int MARK_LOADER_ID = 0;
    private static final int MODEL_LOADER_ID = 1;
    private static final int JOB_LOADER_ID = 2;
    //endregion

    //region View's IDs for adapters
    private static final int[] VIEWS_ID = {android.R.id.text1};
    private static final int[] JOB_IDS = {R.id.cbName, R.id.tvPrice};
    private static final int SPINNER_ITEM = android.R.layout.simple_spinner_item;
    private static final int SPINNER_DROPDOWN_ITEM = android.R.layout.simple_spinner_dropdown_item;
    //endregion

    // Positions of chosen jobs
    private Set<Integer> jobsPositions;
    // Chosen jobs list
    private List<Job> chosenJobs;

    //region Task fields
    private int markId;
    private int modelId;
    private int markPosition;
    private int modelPosition;
    //endregion

    //region String keys for bundle
    private static final String MARK_ID = "MARK_ID";
    private static final String MODEL_ID = "MODEL_ID";
    private static final String MARK_POSITION = "MARK_POSITION";
    private static final String MODEL_POSITION = "MODEL_POSITION";
    private static final String VIN = "VIN";
    private static final String NUMBER = "NUMBER";
    private static final String JOBS = "JOBS";
    //endregion

    private static final int VIN_LENGTH = 17;
    private static final int NUMBER_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        apiInterface = ApiClient.getApi();
        dateFormat = new SimpleDateFormat(getString(R.string.date_format));

        // Get header view and footer view for list view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listHeader = inflater.inflate(R.layout.task_header, null);
        View listFooter = inflater.inflate(R.layout.task_footer, null);

        //region Find widgets by IDs
        etVIN = (EditText) listHeader.findViewById(R.id.etVIN);
        etNumber = (EditText) listHeader.findViewById(R.id.etNumber);
        spinnerMark = (Spinner) listHeader.findViewById(R.id.spinnerMark);
        spinnerModel = (Spinner) listHeader.findViewById(R.id.spinnerModel);
        btnSaveTask = (Button) listFooter.findViewById(R.id.btnSaveTask);
        lvJobs = (ListView) findViewById(R.id.lvJobs);
        //endregion

        btnSaveTask.setOnClickListener(this);

        chosenJobs = new ArrayList<>();
        jobsPositions = new ArraySet<>();

        //region Create adapters

        // Create mark adapter
        markAdapter = new SimpleCursorAdapter(
                this, SPINNER_ITEM, null, Contract.MARK_PROJECTION, VIEWS_ID, 0);
        markAdapter.setDropDownViewResource(SPINNER_DROPDOWN_ITEM);
        spinnerMark.setAdapter(markAdapter);

        // Create model adapter
        modelAdapter = new SimpleCursorAdapter(
                this, SPINNER_ITEM, null, Contract.MODEL_PROJECTION, VIEWS_ID, 0);
        modelAdapter.setDropDownViewResource(SPINNER_DROPDOWN_ITEM);
        spinnerModel.setAdapter(modelAdapter);

        // Create job adapter
        jobAdapter = new SimpleCursorAdapter(
                this, R.layout.job_item, null, Contract.JOB_PROJECTION, JOB_IDS, 0);
        jobAdapter.setDropDownViewResource(SPINNER_DROPDOWN_ITEM);

        //endregion

        // Configure list view
        lvJobs.addHeaderView(listHeader);
        lvJobs.addFooterView(listFooter);
        lvJobs.setAdapter(jobAdapter);
        lvJobs.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cbName = (CheckBox) view.findViewById(R.id.cbName);
                boolean isChecked = !cbName.isChecked();
                cbName.setChecked(isChecked);
                position--;
                if (isChecked)
                    jobsPositions.add(position);
                else
                    jobsPositions.remove(position);
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
            int[] positions = savedInstanceState.getIntArray(JOBS);
            if (positions != null) {
                for (int position : positions) jobsPositions.add(position);
            }
        }

        // Load info from database
        getSupportLoaderManager().initLoader(MARK_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(JOB_LOADER_ID, null, this);

        // Load models for chosen mark
        // Save ID of chosen mark
        spinnerMark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                markPosition = position;
                if (needLoad) {
                    loadModels(position);
                    modelPosition = 0;
                    spinnerModel.setSelection(0);
                }
                else
                    needLoad = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Save ID of chosen model
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                modelId = getItemId(cursor, Contract.ModelEntry.COLUMN_NAME_MODEL_ID);
                modelPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MARK_ID, markId);
        outState.putInt(MODEL_ID, modelId);
        outState.putInt(MARK_POSITION, markPosition);
        outState.putInt(MODEL_POSITION, modelPosition);
        outState.putString(VIN, etVIN.getText().toString());
        outState.putString(NUMBER, etNumber.getText().toString());
        if (!jobsPositions.isEmpty()) {
            int size = jobsPositions.size(), i = 0;
            int[] jobs = new int[size];
            for (int position: jobsPositions)
                jobs[i++] = position;
            outState.putIntArray(JOBS, jobs);
        }
    }

    // Buttons OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveTask:
                String vin = etVIN.getText().toString(), number = etNumber.getText().toString();
                // Get chosen jobs
                chosenJobs.clear();
                for (int i: jobsPositions){
                    Cursor cursor = (Cursor) jobAdapter.getItem(i);
                    int id = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_ID));
                    String name = cursor.getString(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_JOB_NAME));
                    int price = cursor.getInt(cursor.getColumnIndex(Contract.JobEntry.COLUMN_NAME_PRICE));
                    chosenJobs.add(new Job(id, price, name));
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

    // Dialog OnClickListener for dismiss dialogs
    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }

    // Get ID for chosen mark or model
    private int getItemId(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    // Check all task fields
    private boolean checkInput(String vin, String number) {
        if (vin.equals("") || number.equals("")) {
            createErrorDialog(R.string.empty_fields_title, R.string.empty_fields_message);
            return false;
        }
        if (vin.length() < VIN_LENGTH) {
            createErrorDialog(R.string.invalid_vin_title, R.string.invalid_vin_message);
            return false;
        }
        if (number.length() < NUMBER_LENGTH) {
            createErrorDialog(R.string.invalid_number_title, R.string.invalid_number_message);
            return false;
        }
        if (chosenJobs.isEmpty()) {
            createErrorDialog(R.string.empty_jobs_title, R.string.empty_jobs_message);
            return false;
        }
        return true;
    }

    // Create error dialog for task validation
    private void createErrorDialog(int titleId, int messageId) {
        MessageDialog dialog = MessageDialog.newInstance(titleId, messageId);
        dialog.show(getSupportFragmentManager(), DIALOG_TAG);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MARK_LOADER_ID:
                return new CursorLoader(
                        this, Contract.MarkEntry.CONTENT_URI, Contract.MARK_PROJECTION, null, null, null);
            case MODEL_LOADER_ID:
                return new CursorLoader(
                        this, Provider.URI_MODELS_FOR_MARK, Contract.MODEL_PROJECTION, null,
                        new String[] {Integer.toString(markId)}, null);
            case JOB_LOADER_ID:
                return new CursorLoader(
                        this, Contract.JobEntry.CONTENT_URI, Contract.JOB_PROJECTION, null, null, null);
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
                if (!jobsPositions.isEmpty()) {
                    LayoutInflater layoutInflater = LayoutInflater.from(this);
                    View convertView = layoutInflater.inflate(R.layout.job_item, lvJobs, false);
                    for (int position: jobsPositions) {
                        View listItem = jobAdapter.getView(position, convertView, lvJobs);
                        CheckBox cbName = (CheckBox) listItem.findViewById(R.id.cbName);
                        cbName.setChecked(true);
                    }
                    jobAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // Load models for chosen mark
    private void loadModels(int markPosition) {
        Cursor cursor = (Cursor) spinnerMark.getItemAtPosition(markPosition);
        markId = getItemId(cursor, Contract.MarkEntry.COLUMN_NAME_MARK_ID);
        getSupportLoaderManager().restartLoader(MODEL_LOADER_ID, null, this);
    }
}
