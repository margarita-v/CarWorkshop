package com.dsr_practice.car_workshop.activities;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskInfoAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.Task;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        RecyclerView rvJobs = (RecyclerView) findViewById(R.id.rvJobs);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));

        //Get task from intent
        Task task = getIntent().getParcelableExtra(getString(R.string.task_intent));

        // Get names of mark and model from database
        String mark = getNameFromDatabase(
                Contract.MarkEntry.CONTENT_URI,
                task.getMark(),
                Contract.MARK_PROJECTION,
                Contract.MarkEntry.COLUMN_NAME_MARK_NAME);

        String model = getNameFromDatabase(
                Contract.ModelEntry.CONTENT_URI,
                task.getModel(),
                Contract.MODEL_PROJECTION,
                Contract.ModelEntry.COLUMN_NAME_MODEL_NAME);

        // Create an adapter
        TaskInfoAdapter adapter = new TaskInfoAdapter(task, mark, model);
        rvJobs.setAdapter(adapter);
    }

    @Nullable
    private String getNameFromDatabase(Uri baseUri, int id,
                                       String[] projection, String columnName) {
        Uri uri = baseUri.buildUpon().appendPath(Integer.toString(id)).build();
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(columnName));
            cursor.close();
            return name;
        }
        return null;
    }
}
