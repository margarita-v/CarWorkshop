package com.dsr_practice.car_workshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskAdapter;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.models.common.sync.Job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, CloseDialog.CloseInterface {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private TaskLoaderCallbacks callbacks;

    // Tag for dialog usage
    private static final String DIALOG_TAG = "DIALOG";

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

        rvTasks = (RecyclerView) findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TaskActivity.class));
            }
        });

        // This will create a new account with the system for our application, register our
        // SyncService with it, and establish a sync schedule
        //AccountGeneral.createSyncAccount(this);
        callbacks = new TaskLoaderCallbacks();
        startLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRefresh() {
        loadTasksStub();
    }


    @Override
    public void onTaskClose(Task task) {
        task.setStatus(true);
        for (JobStatus jobStatus: task.getJobs()) {
            jobStatus.setStatus(true);
        }
        showTaskCloseMessage(task);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onJobClose(JobStatus jobStatus, Task task) {
        jobStatus.setStatus(true);
        // Check if all jobs in task are closed
        boolean allClosed = true;
        for (JobStatus status: task.getJobs()) {
            allClosed = status.getStatus();
            if (!allClosed)
                break;
        }
        if (allClosed) {
            task.setStatus(true);

            //TODO If response is True
            showTaskCloseMessage(task);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Show message if task was closed
     * @param task Task which was closed
     */
    private void showTaskCloseMessage(Task task) {
        MessageDialog.newInstance(
                getString(R.string.task_was_closed),
                getString(R.string.task_full_price) + Integer.toString(task.getFullPrice()))
                .show(getSupportFragmentManager(), DIALOG_TAG);
    }

    /**
     * Perform task loading
     */
    private void startLoading() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * Option item menu click
     * @param item Menu item which was chosen
     */
    public void onRefreshItemClick(MenuItem item) {
        startLoading();
    }

    // Stub method for testing adapter
    public void loadTasksStub() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                for (int i = 0; i < dateArray.length; i++) {
                    try {
                        Date newDate = format.parse(dateArray[i]);
                        List<JobStatus> jobs = new ArrayList<>();
                        for (int j = 0; j < 4; j++) {
                            Job job = new Job(i, priceArray[i], jobsArray[i]);
                            jobs.add(new JobStatus(i, i, job, false));
                        }
                        Task task = new Task(i, newDate, 1, 2, "A001AA", "dfghj", "name", false, jobs);
                        taskList.add(task);
                    } catch (ParseException e) {
                        Toast.makeText(MainActivity.this, R.string.toast_invalid_date, Toast.LENGTH_SHORT).show();
                    }
                }
                sort(taskList);
                adapter = new TaskAdapter(taskList, MainActivity.this, getSupportFragmentManager());
                rvTasks.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    /**
     * Load tasks from server
     */
    public void loadTasks() {
        if (adapter == null || adapter.getItemCount() == 0)
            getSupportLoaderManager().initLoader(TaskLoader.TASK_LOADER_ID, null, callbacks);
        else
            getSupportLoaderManager().restartLoader(TaskLoader.TASK_LOADER_ID, null, callbacks);
    }

    /**
     * Sort task list by date
     * @param taskList List of tasks which will be sorted
     */
    private void sort(List<Task> taskList) {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                return task1.getDate().compareTo(task2.getDate());
            }
        });
    }

    /**
     * Class for loading task list from server using Loader
     */
    private class TaskLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Task>> {

        @Override
        public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
            return new TaskLoader(MainActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
            // Sort task list by date and show it
            if (data != null) {
                sort(data);
                adapter = new TaskAdapter(data, MainActivity.this, getSupportFragmentManager());
                rvTasks.setAdapter(adapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Task>> loader) {

        }
    }
}
