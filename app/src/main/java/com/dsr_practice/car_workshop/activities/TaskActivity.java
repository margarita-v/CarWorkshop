package com.dsr_practice.car_workshop.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.dsr_practice.car_workshop.models.post.TaskPost;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity
        implements View.OnClickListener, DialogInterface.OnClickListener {

    EditText etVIN, etNumber;
    Spinner spinnerMark, spinnerModel;
    Button btnSaveTask;
    ListView lvJobs;

    private ContentResolver contentResolver;
    private SimpleCursorAdapter jobAdapter;
    private SimpleDateFormat dateFormat;
    private static ApiInterface apiInterface;
    private static final String DIALOG_TAG = "DIALOG";

    // View's IDs for adapters
    private int[] viewsId = {android.R.id.text1};
    private int[] jobIds = {R.id.cbName, R.id.tvPrice};
    private int spinnerItem = android.R.layout.simple_spinner_item;
    private int spinnerDropDown = android.R.layout.simple_spinner_dropdown_item;

    // Positions of chosen jobs
    private Set<Integer> jobsPositions;
    // Chosen jobs list
    private List<Job> chosenJobs;
    // Chosen mark ID
    private int markId;
    // Chosen model ID
    private int modelId;

    private static final int VIN_LENGTH = 17;
    private static final int NUMBER_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        apiInterface = ApiClient.getApi();
        dateFormat = new SimpleDateFormat(getString(R.string.date_format));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listHeader = inflater.inflate(R.layout.task_header, null);
        View listFooter = inflater.inflate(R.layout.task_footer, null);

        etVIN = (EditText) listHeader.findViewById(R.id.etVIN);
        etNumber = (EditText) listHeader.findViewById(R.id.etNumber);
        spinnerMark = (Spinner) listHeader.findViewById(R.id.spinnerMark);
        spinnerModel = (Spinner) listHeader.findViewById(R.id.spinnerModel);
        btnSaveTask = (Button) listFooter.findViewById(R.id.btnSaveTask);
        lvJobs = (ListView) findViewById(R.id.lvJobs);

        btnSaveTask.setOnClickListener(this);

        // Load data from database
        contentResolver = getContentResolver();
        chosenJobs = new ArrayList<>();
        jobsPositions = new ArraySet<>();

        // Load mark info
        SimpleCursorAdapter markAdapter = getAdapter(
                Contract.MarkEntry.CONTENT_URI,
                Contract.MARK_PROJECTION,
                null,
                spinnerItem,
                viewsId);
        markAdapter.setDropDownViewResource(spinnerDropDown);
        spinnerMark.setAdapter(markAdapter);

        // Load models for chosen mark
        // Save ID of chosen mark
        spinnerMark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                markId = getItemId(cursor, Contract.MarkEntry.COLUMN_NAME_MARK_ID);
                SimpleCursorAdapter modelAdapter = getAdapter(
                        Provider.URI_MODELS_FOR_MARK,
                        Contract.MODEL_PROJECTION,
                        new String[] {Integer.toString(markId)},
                        spinnerItem,
                        viewsId);
                modelAdapter.setDropDownViewResource(spinnerDropDown);
                spinnerModel.setAdapter(modelAdapter);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get all jobs from database
        jobAdapter = getAdapter(
                Contract.JobEntry.CONTENT_URI,
                Contract.JOB_PROJECTION,
                null,
                R.layout.job_item,
                jobIds);

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

    // Get cursor adapter for database entities
    private SimpleCursorAdapter getAdapter(Uri uri, String[] projection, String[] args, int layout, int[] views) {
        Cursor cursor = contentResolver.query(uri, projection, null, args, null, null);
        return new SimpleCursorAdapter(this, layout, cursor, projection, views, 0);
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
}
