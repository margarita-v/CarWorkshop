package com.dsr_practice.car_workshop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    List<String> listHeaders;
    HashMap<String, List<String>> listItems;
    ExpandableListView elvCars;
    ExpandableListAdapter adapter;

    private static final String[] JOB_PROJECTION = new String[] {
            Contract.JobEntry.COLUMN_NAME_JOB_NAME,
            Contract.JobEntry.COLUMN_NAME_PRICE
    };
    private static final int[] LIST_ITEM_PROJECTION = new int[] { R.id.tvWork, R.id.tvPrice };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        elvCars = (ExpandableListView) findViewById(R.id.elvCars);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TaskActivity.class));
            }
        });

        ApiInterface apiInterface = ApiClient.getApi();
        apiInterface.getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                List<Task> listTasks = response.body();
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Can't load tasks!", Toast.LENGTH_SHORT).show();
            }
        });

        /*
        listHeaders = new ArrayList<>();
        listItems = new HashMap<>();

        String[] sampleData = new String[] {"First", "Second", "Third", "Fourth", "5", "6", "7", "8", "9", "10"};
        List<String> sampleItems = new ArrayList<>(Arrays.asList("Car wash", "Change color", "Full repair"));
        for (String header: sampleData) {
            listHeaders.add(header);
            listItems.put(header, sampleItems);
        }

        adapter = new ExpandableListAdapter(this, listHeaders, listItems);
        elvCars.setAdapter(adapter);*/

        // Stub methods for testing the adapter
        String[] dateArray = new String[] {
                "2012-04-05T20:40:45Z",
                "2014-04-05T20:40:45Z",
                "2014-04-06T20:40:45Z",
                "2012-04-05T20:41:45Z"
        };
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        List<Task> taskList = new ArrayList<>();
        List<JobStatus> jobs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            jobs.add(new JobStatus());
        }
        for (int i = 0; i < dateArray.length; i++) {
            try {
                Date newDate = format.parse(dateArray[i]);
                Task task = new Task(i, newDate, 1, 2, "A001AA", "dfghj", "name", false);
                task.setJobs(jobs);
                taskList.add(task);
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
            }
        }
        adapter = new ExpandableListAdapter(this, taskList);
        elvCars.setAdapter(adapter);

        // This will create a new account with the system for our application, register our
        // SyncService with it, and establish a sync schedule
        //AccountGeneral.createSyncAccount(this);
    }
}
