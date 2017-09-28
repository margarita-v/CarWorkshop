package com.dsr_practice.car_workshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.fragments.TaskFragment;

public class MainActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, TaskFragment.TaskLoaderListener {

    private TaskFragment fragment;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        fragment = (TaskFragment) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        startLoading();
    }

    private void startLoading() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
            fragment.loadTasksStub();
        }
    }

    @Override
    public void onRefresh() {
        fragment.loadTasksStub();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Option item menu click
     * @param item Menu item which was chosen
     */
    public void onRefreshItemClick(MenuItem item) {
        startLoading();
    }

    @Override
    public void onLoadFinished() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
