package com.dsr_practice.car_workshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskListAdapter;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    ExpandableListView elvCars;
    SwipeRefreshLayout swipeRefreshLayout;

    TaskListAdapter adapter;
    private TaskLoader taskLoader;
    private TaskLoaderCallbacks callbacks;
    private static ApiInterface apiInterface;
    private static final int TASK_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.main_title);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        elvCars = (ExpandableListView) findViewById(R.id.elvCars);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TaskActivity.class));
            }
        });

        // This will create a new account with the system for our application, register our
        // SyncService with it, and establish a sync schedule
        //AccountGeneral.createSyncAccount(this);
        apiInterface = ApiClient.getApi();
        taskLoader = new TaskLoader(this);
        callbacks = new TaskLoaderCallbacks();

        // Stub methods for testing the adapter
        String[] dateArray = new String[] {
                "2012-04-05T20:40:45Z",
                "2014-04-05T20:40:45Z",
                "2014-04-06T20:40:45Z",
                "2012-04-05T20:41:45Z"
        };
        String[] jobsArray = new String[] {
                "Car wash",
                "Full repair",
                "Cleaning",
                "Change color"
        };
        int[] priceArray = new int[] { 300, 1000, 200, 500 };
        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date_format));
        List<Task> taskList = new ArrayList<>();
        List<JobStatus> jobs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Job job = new Job(i, priceArray[i], jobsArray[i]);
            jobs.add(new JobStatus(i, i, job, false));
        }
        for (int i = 0; i < dateArray.length; i++) {
            try {
                Date newDate = format.parse(dateArray[i]);
                Task task = new Task(i, newDate, 1, 2, "A001AA", "dfghj", "name", false);
                task.setJobs(jobs);
                taskList.add(task);
            } catch (ParseException e) {
                Toast.makeText(this, R.string.toast_invalid_date, Toast.LENGTH_SHORT).show();
            }
        }
        // Sort taskList by date
        for (int i = 0; i < taskList.size(); i++)
            for (int j = 0; j < taskList.size(); j++) {
                Task iTask = taskList.get(i), jTask = taskList.get(j);
                if (iTask.getDate().before(jTask.getDate())) {
                    taskList.set(i, jTask);
                    taskList.set(j, iTask);
                }
            }
        adapter = new TaskListAdapter(this, taskList, getSupportFragmentManager());
        elvCars.setAdapter(adapter);
        elvCars.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPosition = elvCars.getExpandableListPosition(position);

                int itemType = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
                    adapter.onGroupLongClick(groupPosition);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO Get all task in onCreate() and get last task in onStart() after task creation
        apiInterface.getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                /*
                List<Task> taskList = response.body();
                // Sort taskList by date
                for (int i = 0; i < taskList.size(); i++)
                    for (int j = 0; j < taskList.size(); j++) {
                        Task iTask = taskList.get(i), jTask = taskList.get(j);
                        if (iTask.getDate().before(jTask.getDate())) {
                            taskList.set(i, jTask);
                            taskList.set(j, iTask);
                        }
                    }
                adapter = new TaskListAdapter(MainActivity.this, taskList);
                elvCars.setAdapter(adapter);*/
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.toast_cant_load, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loadTasks(true);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Load tasks from server
     * @param restart If true, then we should restart loading, else we should init loader for a first usage
     */
    private void loadTasks(boolean restart) {
        if (restart)
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, callbacks);
        else
            getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, callbacks);
    }

    // Class for loading task list from server using Loader
    private class TaskLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Task>> {

        @Override
        public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
            return taskLoader;
        }

        @Override
        public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {

        }

        @Override
        public void onLoaderReset(Loader<List<Task>> loader) {

        }
    }
}
