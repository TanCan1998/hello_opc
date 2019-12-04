package com.example.hello.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hello.R;
import com.example.hello.util.opc.SubscriptionElement;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MonitoringAdapter extends ArrayAdapter<SubscriptionElement> {
    private List<SubscriptionElement> objects;

    public MonitoringAdapter(Context context, int resource, List<SubscriptionElement> objects) {
        super(context, resource, objects);
        this.objects = objects;
    }

    @NotNull
    @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        convertView = inflater.inflate(R.layout.item_monitoring, null);
        SubscriptionElement obj = getItem(position);

        TextView txtSubID = convertView.findViewById(R.id.txtSubID);
        final ListView listSub = convertView.findViewById(R.id.listSubMonitored);
        TextView txtpubinterval = convertView.findViewById(R.id.txtSubPubInterval);

        assert obj != null;
        txtSubID.setText("Subscription ID: " + obj.getSubscription().getSubscriptionId());
        txtpubinterval.setText("Publishing interval: " + obj.getSubscription().getRevisedPublishingInterval().toString() + " ms");

        SubMonitoringAdapter adapter = new SubMonitoringAdapter(getContext(), R.layout.item_submonitoring, obj.getMonitoredItems(), objects, position);
        listSub.setAdapter(adapter);
//        SetUtils.setListViewHeightBasedOnChildren(listSub);

        return convertView;
    }
}
