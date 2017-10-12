package com.dsr_practice.car_workshop.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskInfoAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;

public class InfoActivity extends AppCompatActivity {

    ListView lvJobs;
    TaskInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        lvJobs = (ListView) findViewById(R.id.lvJobs);
        Task task = (Task) getIntent().getSerializableExtra(getString(R.string.task_intent));
        adapter = new TaskInfoAdapter(this, task);
        lvJobs.setAdapter(adapter);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listHeader = inflater.inflate(R.layout.info_header, null);
        lvJobs.addHeaderView(listHeader);

        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        TextView tvVin = (TextView) findViewById(R.id.tvVIN);
        TextView tvMark = (TextView) findViewById(R.id.tvMark);
        TextView tvModel = (TextView) findViewById(R.id.tvModel);
        TextView tvDate = (TextView) findViewById(R.id.tvDate);
        TextView tvNumber = (TextView) findViewById(R.id.tvNumber);

        final String statusName = task.getStatus() ? getString(R.string.closed) : getString(R.string.opened);
        tvStatus.setText(statusName);

        Uri markUri = Contract.MarkEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(task.getMark())).build();
        Uri modelUri = Contract.ModelEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(task.getModel())).build();

        tvVin.setText(task.getVin());
        setNameFromDatabase(markUri, Contract.MARK_PROJECTION, Contract.MarkEntry.COLUMN_NAME_MARK_NAME, tvMark);
        setNameFromDatabase(modelUri, Contract.MODEL_PROJECTION, Contract.ModelEntry.COLUMN_NAME_MODEL_NAME, tvModel);
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        tvDate.setText(dateFormat.format(task.getDate()));
        tvNumber.setText(task.getNumber());
    }

    private void setNameFromDatabase(Uri uri, String[] projection, String columnName, TextView textView) {
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(columnName));
            textView.setText(name);
        }
        cursor.close();
    }
}
