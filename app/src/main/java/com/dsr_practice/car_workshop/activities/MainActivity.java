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
import android.support.v7.widget.Toolbar.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskListAdapter;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ExpandableListView elvCars;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TaskListAdapter adapter;
    private TaskLoaderCallbacks callbacks;

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

        /*
        // Find task list widget and set its empty view
        elvCars = (ExpandableListView) findViewById(R.id.elvCars);
        View emptyView = getLayoutInflater().inflate(R.layout.empty_list, null, false);
        addContentView(emptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        elvCars.setEmptyView(emptyView);

        // Configure ExpandableListView
        adapter = new TaskListAdapter(this, new ArrayList<Task>(), getSupportFragmentManager());
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

        Button btnLoad = (Button) emptyView.findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(this);

*/
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
        //loadTasks(false);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // If adapter is not empty, then we should restart loader
                //loadTasks(adapter.getGroupCount() > 0);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onRefreshItemClick(MenuItem item) {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    // Stub method for testing adapter
    private void loadTasksStub() {
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
        sort(taskList);
        adapter.setTaskList(taskList);
    }

    /**
     * Load tasks from server
     * @param restart If true, then we should restart loading, else we should init loader for a first usage
     */
    private void loadTasks(boolean restart) {
        //TODO Loading picture
        if (restart)
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, callbacks);
        else
            getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, callbacks);
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
            sort(data);
            adapter = new TaskListAdapter(MainActivity.this, data, getSupportFragmentManager());
            elvCars.setAdapter(adapter);
        }

        @Override
        public void onLoaderReset(Loader<List<Task>> loader) {

        }
    }
}
