package com.example.hello.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hello.R;

import org.opcfoundation.ua.core.EndpointDescription;

import java.util.List;

public class EndpointsAdapter extends ArrayAdapter<EndpointDescription> {

    public EndpointsAdapter(Context context, int resource, List<EndpointDescription> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_endpoints, null);
        TextView endpoint = convertView.findViewById(R.id.txtEndpoint);
        String text = "";
        EndpointDescription obj = getItem(position);
        text += "Uri: " + obj.getEndpointUrl() + '\n';
        text += "Security Mode: " + obj.getSecurityMode() + '\n';
        text += "Security Level: " + obj.getSecurityLevel();
        endpoint.setText(text);
        return convertView;
    }
}
