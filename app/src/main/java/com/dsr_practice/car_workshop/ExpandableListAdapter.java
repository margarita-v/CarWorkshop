package com.dsr_practice.car_workshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listHeaders;
    private HashMap<String, List<String>> listItems;

    ExpandableListAdapter(Context context,
                          List<String> headers,
                          HashMap<String, List<String>> items) {
        this.context = context;
        this.listHeaders = headers;
        this.listItems = items;
    }

    @Override
    public int getGroupCount() {
        return this.listHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listItems.get(this.listHeaders.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listItems.get(this.listHeaders.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater= (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.tvCarName);
        lblListHeader.setText(headerTitle);

        ImageButton imgBtnClose = (ImageButton) convertView.findViewById(R.id.imgBtnClose);
        ImageButton imgBtnMore = (ImageButton) convertView.findViewById(R.id.imgBtnMore);

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        imgBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        TextView txtListChild = (TextView) convertView.findViewById(R.id.tvWork);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
