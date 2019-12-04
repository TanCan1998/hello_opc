package com.example.hello.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.hello.util.opc.MonitoredItemElement;
import com.example.hello.R;

import java.util.List;

public class MonitoredItemAdapter extends ArrayAdapter<MonitoredItemElement> {

    public MonitoredItemAdapter(Context context, int resource, List<MonitoredItemElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_monitored, null);
        TextView monitored = convertView.findViewById(R.id.txtMonitored);
        MonitoredItemElement obj = getItem(position);
        String text = "Monitored Item ID: " + obj.getMonitoredItem().getResults()[0].getMonitoredItemId() +
                "\nSampling Interval: " + obj.getMonitoredItem().getResults()[0].getRevisedSamplingInterval() +
                "\nQueue Size: " + obj.getMonitoredItem().getResults()[0].getRevisedQueueSize();
        monitored.setText(text);
        return convertView;
    }
}
