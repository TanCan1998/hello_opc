package com.example.hello.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hello.util.opc.BrowseDataStamp;
import com.example.hello.R;

import java.util.List;


public class NodeAdapter extends ArrayAdapter<BrowseDataStamp> {

    public NodeAdapter(Context context, int resource, List<BrowseDataStamp> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_node, null);
        TextView node = convertView.findViewById(R.id.txtNode);
        TextView nodedata = convertView.findViewById(R.id.txtNodeData);
        BrowseDataStamp obj = getItem(position);
        node.setText(obj.name);
        String text = getContext().getString(R.string.namespace) + ": " + obj.namespace +
                "\n" + getContext().getString(R.string.node_index) + ": " + obj.nodeindex +
                "\nClass: " + obj.nodeclass;
        nodedata.setText(text);
        convertView.setTag(obj);
        if (obj.nodeclass.equals("Variable"))
            convertView.findViewById(R.id.nodeIcon).setBackgroundResource(R.drawable.ic_edit);
        return convertView;
    }
}
