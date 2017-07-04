package com.dsr_practice.car_workshop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> listHeaders;
    HashMap<String, List<String>> listItems;
    ExpandableListView elvCars;
    ExpandableListAdapter adapter;

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

        listHeaders = new ArrayList<>();
        listItems = new HashMap<>();

        String[] sampleData = new String[] {"First", "Second", "Third", "Fourth", "5", "6", "7", "8", "9", "10"};
        List<String> sampleItems = new ArrayList<>(Arrays.asList("Car wash", "Change color", "Full repair"));
        for (String header: sampleData) {
            listHeaders.add(header);
            listItems.put(header, sampleItems);
        }

        adapter = new ExpandableListAdapter(this, listHeaders, listItems);
        elvCars.setAdapter(adapter);
    }
}
