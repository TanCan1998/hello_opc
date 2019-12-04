package com.example.hello.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hello.R;
import com.example.hello.util.opc.SubscriptionElement;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SubscriptionAdapter extends ArrayAdapter<SubscriptionElement> {
    private       int            resourceR;
    private final LayoutInflater myInflater;

    public SubscriptionAdapter(Context context, int resource, List<SubscriptionElement> objects, int resourceR) {
        super(context, resource, objects);
        this.resourceR = resourceR;
        myInflater = LayoutInflater.from(context);
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        convertView = convertView != null ? convertView : myInflater.inflate(resourceR, null);
        TextView endpoint = convertView.findViewById(R.id.txtReaging);
        SubscriptionElement obj = getItem(position);
        assert obj != null;
        String text = "Subscription ID: " + obj.getSubscription().getSubscriptionId() +
                "\nSessionID:" + obj.getSession().getSession().getName() +
                "\nPublishInterval: " + obj.getSubscription().getRevisedPublishingInterval() +
                "\nlifetime_count: " + obj.getSubscription().getRevisedLifetimeCount() +
                "\nmax_keep_alive_count: " + obj.getSubscription().getRevisedMaxKeepAliveCount();
        endpoint.setText(text);
        return convertView;
    }
}
