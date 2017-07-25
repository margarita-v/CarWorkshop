package com.dsr_practice.car_workshop.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskInfoAdapter;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;

public class InfoActivity extends AppCompatActivity {

    ListView lvJobs;
    TaskInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Информация о задаче");

        lvJobs = (ListView) findViewById(R.id.lvJobs);
        Task task = (Task) getIntent().getSerializableExtra("Task");
        adapter = new TaskInfoAdapter(this, task);
        lvJobs.setAdapter(adapter);

        TextView tvVin = (TextView) findViewById(R.id.tvVIN);
        TextView tvMark = (TextView) findViewById(R.id.tvMark);
        TextView tvModel = (TextView) findViewById(R.id.tvModel);
        TextView tvDate = (TextView) findViewById(R.id.tvDate);
        TextView tvNumber = (TextView) findViewById(R.id.tvNumber);

        tvVin.setText(task.getVin());
        tvMark.setText(Integer.toString(task.getMark()));
        tvModel.setText(Integer.toString(task.getModel()));
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        tvDate.setText(dateFormat.format(task.getDate()));
        tvNumber.setText(task.getNumber());
    }
}
