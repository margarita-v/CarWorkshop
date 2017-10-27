package com.dsr_practice.car_workshop.activities;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.accounts.AccountGeneral;
import com.dsr_practice.car_workshop.adapters.TaskAdapter;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.loaders.CloseJobLoader;
import com.dsr_practice.car_workshop.loaders.CloseTaskLoader;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.sync.SyncAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, CloseDialog.CloseInterface,
        LoaderManager.LoaderCallbacks<List<Task>> {

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView elvTasks;
    private TaskAdapter adapter;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
        setTitle(R.string.main_title);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        elvTasks = findViewById(R.id.elvTasks);

        // This will create a new account with the system for our application, register our
        // SyncService with it, and establish a sync schedule
        AccountGeneral.createSyncAccount(this);
        startLoading();
    }

    //region Activity lifecycle
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
    //endregion

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
            case R.id.action_add:
                startActivity(new Intent(MainActivity.this, TaskActivity.class));
                return true;
            case R.id.action_sync:
                SyncAdapter.performSync();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Configure sync observer
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
            if (refreshing)
                refreshItem.setActionView(R.layout.actionbar);
            else
                refreshItem.setActionView(null);
        }
    }
    //endregion

    //region Callbacks from CloseDialog
    @Override
    public void onTaskClose(final Task task) {
        progressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(CloseTaskLoader.CLOSE_TASK_ID, null,
                new CloseActionCallbacks(task));
    }

    @Override
    public void onJobClose(Integer jobId, final Task task) {
        progressBar.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(CloseJobLoader.CLOSE_JOB_ID, null,
                new CloseActionCallbacks(task, jobId));
    }

    /**
     * Show message if task was closed
     * @param task Task which was closed
     */
    private void showTaskCloseMessage(Task task) {
        MessageDialog.showDialog(
                getString(R.string.task_was_closed),
                getString(R.string.task_full_price) + Integer.toString(task.getFullPrice()),
                getSupportFragmentManager());
    }
    //endregion

    //region Task loading
    /**
     * Perform task loading
     */
    private void startLoading() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
            loadTasks(false);
        }
    }

    @Override
    public void onRefresh() {
        loadTasks(true);
    }

    /**
     * Load tasks from server
     */
    public void loadTasks(boolean restart) {
        if (restart)
            getSupportLoaderManager().restartLoader(TaskLoader.TASK_LOADER_ID, null, this);
        else
            getSupportLoaderManager().initLoader(TaskLoader.TASK_LOADER_ID, null, this);
    }
    //endregion

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
            elvTasks.setAdapter(adapter);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {

    }

    /**
     * Sort task list by date
     * @param taskList List of tasks which will be sorted
     */
    private void sort(List<Task> taskList) {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                return task2.getDate().compareTo(task1.getDate());
            }
        });
    }
    //endregion

    /**
     * Callbacks for closing task and jobs
     */
    private class CloseActionCallbacks implements LoaderManager.LoaderCallbacks<Task> {

        private Task task;
        private int jobId;

        // Constructor for task closing
        CloseActionCallbacks(Task task) {
            this.task = task;
        }

        // Constructor for job closing
        CloseActionCallbacks(Task task, int jobId) {
            this.task = task;
            this.jobId = jobId;
        }

        @Override
        public Loader<Task> onCreateLoader(int id, Bundle args) {
            return id == CloseJobLoader.CLOSE_JOB_ID
                    ? new CloseJobLoader(MainActivity.this, task.getId(), jobId)
                    : new CloseTaskLoader(MainActivity.this, task.getId());
        }

        @Override
        public void onLoadFinished(final Loader<Task> loader, final Task data) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (data != null && data.getStatus())
                        showTaskCloseMessage(task);
                    finishLoading(data);
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<Task> loader) {

        }
    }

    /**
     * Actions after closing task of jobs
     * @param task Response from server
     */
    private void finishLoading(Task task) {
        if (task != null) {
            adapter.setTask(task);
            // If task is not close, then one job in task was closed
            if (!task.getStatus())
                Toast.makeText(this, R.string.toast_close_job,
                        Toast.LENGTH_SHORT).show();
        }
        else
            MessageDialog.showConnectionError(getSupportFragmentManager());
        progressBar.setVisibility(View.GONE);
    }
}
