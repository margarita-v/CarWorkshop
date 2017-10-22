package com.dsr_practice.car_workshop.activities;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.accounts.AccountGeneral;
import com.dsr_practice.car_workshop.adapters.TaskAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.post.CloseJobPost;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, CloseDialog.CloseInterface,
        LoaderManager.LoaderCallbacks<List<Task>> {

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private ApiInterface apiInterface;

    /**
     * Handle to a SyncObserver.
     * The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object syncObserverHandle;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
        setTitle(R.string.main_title);
        apiInterface = ApiClient.getApi();

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
        //TODO AccountGeneral.createSyncAccount(this);
        startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        syncObserverHandle = ContentResolver.addStatusChangeListener(mask, syncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (syncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(syncObserverHandle);
            syncObserverHandle = null;
        }
    }

    //region Configure options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                startLoading();
                return true;
            case R.id.action_sync:
                //TODO SyncAdapter.performSync();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    /**
     * Set the state of the Refresh button.
     * If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occurring, false otherwise
     */
    public void setRefreshActionButtonState(boolean refreshing) {
        if (optionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = optionsMenu.findItem(R.id.action_sync);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver.
     * It's attached to the app's ContentResolver in onResume(), and removed in onPause().
     * If status changes, it sets the state of the Refresh button.
     * If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver syncStatusObserver = new SyncStatusObserver() {
        /**
         * Callback invoked with the sync adapter status changes.
         */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread.
                 * To update the UI, onStatusChanged() runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // AccountGeneral.createSyncAccount().
                    // This will be used to query the system to see
                    // how the sync status has changed.
                    Account account = AccountGeneral.getAccount();

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, Contract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, Contract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

    @Override
    public void onRefresh() {
        loadTasksStub();
    }

    //region Callbacks from CloseDialog
    @Override
    public void onTaskClose(final Task task) {
        post();

        /*
        progressBar.setVisibility(View.VISIBLE);
        apiInterface.closeTask(task.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                showTaskCloseMessage(task);
                finishLoading(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                finishLoading(false);
            }
        });*/

        task.setStatus(true);
        for (JobStatus jobStatus: task.getJobs()) {
            jobStatus.setStatus(true);
        }
        showTaskCloseMessage(task);
    }

    @Override
    public void onJobClose(JobStatus jobStatus, final Task task) {
        post();

        /*
        progressBar.setVisibility(View.VISIBLE);
        apiInterface.closeJobInTask(new CloseJobPost(task.getId(), jobStatus.getJob().getId()))
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.body())
                            showTaskCloseMessage(task);
                        finishLoading(true);
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        finishLoading(false);
                    }
                });*/

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
    }

    private void post() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        }, 1000);
    }
    //endregion

    /**
     * Show message if task was closed
     * @param task Task which was closed
     */
    private void showTaskCloseMessage(Task task) {
        MessageDialog.newInstance(
                getString(R.string.task_was_closed),
                getString(R.string.task_full_price) + Integer.toString(task.getFullPrice()))
                .show(getSupportFragmentManager(), MessageDialog.TAG);
    }

    /**
     * Perform actions after loading
     * @param success True if loading was finished successfully
     */
    private void finishLoading(boolean success) {
        if (success)
            adapter.notifyDataSetChanged();
        else
            MessageDialog.newInstance(R.string.conn_error_title, R.string.conn_error_message)
                    .show(getSupportFragmentManager(), MessageDialog.TAG);
        progressBar.setVisibility(View.GONE);
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
                            Job job = new Job(j, priceArray[j], jobsArray[j]);
                            jobs.add(new JobStatus(j, j, job, false));
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
            getSupportLoaderManager().initLoader(TaskLoader.TASK_LOADER_ID, null, this);
        else
            getSupportLoaderManager().restartLoader(TaskLoader.TASK_LOADER_ID, null, this);
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

    //region Callbacks for loading task list from server using Loader
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
    //endregion
}
