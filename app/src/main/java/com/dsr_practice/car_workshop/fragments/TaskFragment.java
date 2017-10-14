package com.dsr_practice.car_workshop.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.adapters.TaskListAdapter;
import com.dsr_practice.car_workshop.loaders.TaskLoader;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TaskFragment extends ListFragment {

    // Widgets of an empty view
    private TextView tvEmpty;
    private ImageView imgEmpty;

    // Task list widget and its adapter
    private ExpandableListView elvCars;
    private TaskListAdapter adapter;

    private TaskLoaderCallbacks callbacks;
    private TaskLoaderListener taskLoaderListener;

    private static final int TASK_LOADER_ID = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        callbacks = new TaskLoaderCallbacks();

        View view = inflater.inflate(R.layout.task_fragment, container, false);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        imgEmpty = (ImageView) view.findViewById(R.id.imgEmpty);

        elvCars = (ExpandableListView) view.findViewById(android.R.id.list);
        adapter = new TaskListAdapter(getContext(), new ArrayList<Task>(), getActivity().getSupportFragmentManager());
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
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            try {
                taskLoaderListener = (TaskLoaderListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement TaskLoaderListener");
            }
        }
    }

    /**
     * Perform UI changes before and after task loading
     * @param isLoadFinished If true, then function will applied after task loading
     */
    private void loadingActions(boolean isLoadFinished) {
        elvCars.setEnabled(isLoadFinished);
        if (isLoadFinished) {
            if (adapter.getGroupCount() == 0) {
                tvEmpty.setText(R.string.empty_list);
                imgEmpty.setImageResource(R.drawable.ic_assignment_late_black_24dp);
            }
            taskLoaderListener.onLoadFinished();
        }
        else {
            tvEmpty.setText(R.string.loading);
            imgEmpty.setImageResource(R.drawable.ic_directions_car_black_24dp);
        }
    }

    // Stub method for testing adapter
    public void loadTasksStub() {
        loadingActions(false);
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
                List<JobStatus> jobs = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Job job = new Job(i, priceArray[i], jobsArray[i]);
                    jobs.add(new JobStatus(i, i, job, false));
                }
                for (int i = 0; i < dateArray.length; i++) {
                    try {
                        Date newDate = format.parse(dateArray[i]);
                        Task task = new Task(i, newDate, 1, 2, "A001AA", "dfghj", "name", false, jobs);
                        taskList.add(task);
                    } catch (ParseException e) {
                        Toast.makeText(getContext(), R.string.toast_invalid_date, Toast.LENGTH_SHORT).show();
                    }
                }
                sort(taskList);
                adapter.setTaskList(taskList);
                loadingActions(true);
            }
        }, 3000);
    }

    /**
     * Load tasks from server
     */
    public void loadTasks() {
        loadingActions(false);
        if (adapter == null || adapter.getGroupCount() == 0)
            getActivity().getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, callbacks);
        else
            getActivity().getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, callbacks);
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
            return new TaskLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
            // Sort task list by date and show it
            if (data != null) {
                sort(data);
                adapter = new TaskListAdapter(
                        getActivity(),
                        data,
                        getActivity().getSupportFragmentManager());
                elvCars.setAdapter(adapter);
            }
            loadingActions(true);
        }

        @Override
        public void onLoaderReset(Loader<List<Task>> loader) {

        }
    }

    public interface TaskLoaderListener {
        void onLoadFinished();
    }
}
