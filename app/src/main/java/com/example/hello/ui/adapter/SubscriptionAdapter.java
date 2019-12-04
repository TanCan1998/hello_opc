package com.example.hello.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hello.util.opc.SubscriptionElement;
import com.example.hello.R;

import java.util.List;


public class SubscriptionAdapter extends ArrayAdapter<SubscriptionElement> {

    public SubscriptionAdapter(Context context, int resource, List<SubscriptionElement> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_readings, null);
        TextView endpoint = convertView.findViewById(R.id.txtReaging);
        SubscriptionElement obj = getItem(position);
        String text = "Subscription ID: " + obj.getSubscription().getSubscriptionId() +
                "\nSessionID:" + obj.getSession().getSession().getName() +
                "\nPublishInterval: " + obj.getSubscription().getRevisedPublishingInterval() +
                "\nlifetime_count: " + obj.getSubscription().getRevisedLifetimeCount() +
                "\nmax_keep_alive_count: " + obj.getSubscription().getRevisedMaxKeepAliveCount();
        endpoint.setText(text);
        return convertView;
    }
}
