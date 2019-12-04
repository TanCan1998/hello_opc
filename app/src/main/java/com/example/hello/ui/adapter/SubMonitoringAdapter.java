package com.example.hello.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hello.R;
import com.example.hello.util.AlarmHelper;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.MonitoredItemElement;
import com.example.hello.util.opc.SubscriptionElement;
import com.sdsmdg.tastytoast.TastyToast;

import org.jetbrains.annotations.NotNull;
import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.example.hello.util.SetUtils.hideSoftInput;
import static com.example.hello.util.SetUtils.initInput;
import static com.example.hello.util.SetUtils.setViewColor;


public class SubMonitoringAdapter extends ArrayAdapter<MonitoredItemElement> {

    private List<SubscriptionElement> elements;
    private int                       position1;

    SubMonitoringAdapter(Context context, int resource, List<MonitoredItemElement> objects, List<SubscriptionElement> elements, int position1) {
        super(context, resource, objects);
        this.elements = elements;
        this.position1 = position1;
    }

    @NotNull
    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(final int position, View convertView, @NotNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        convertView = inflater.inflate(R.layout.item_submonitoring, null);
        MonitoredItemElement obj = getItem(position);

        TextView txtMonID = convertView.findViewById(R.id.txtMonID);
        TextView submonval = convertView.findViewById(R.id.txtsubmonval);
        TextView subsource = convertView.findViewById(R.id.txtsubsource);
        TextView subserv = convertView.findViewById(R.id.txtsubserv);
        TextView substatus = convertView.findViewById(R.id.txtsubstato);
        Button btnAddAlarm = convertView.findViewById(R.id.btn_add_alarm);

        assert obj != null;
        txtMonID.setText("Item ID: " + obj.getMonitoredItem().getResults()[0].getMonitoredItemId());
        final MonitoredItemNotification notification = obj.getReadings().getFirst();

        try {
            String tmp = "none";
            submonval.setText("Value: " + notification.getValue().getValue());
            if (notification.getValue().getSourceTimestamp() != null) {
                tmp = notification.getValue().getSourceTimestamp().toString();
                tmp = tmp.substring(0, tmp.length() - 10);
            }
            subsource.setText("Source Timestamp: " + tmp);
            tmp = "none";
            if (notification.getValue().getServerTimestamp() != null) {
                tmp = notification.getValue().getServerTimestamp().toString();
                tmp = tmp.substring(0, tmp.length() - 10);
            }
            subserv.setText("Server Timestamp: " + tmp);
            substatus.setText("Status: " + notification.getValue().getStatusCode());
        } catch (NoSuchElementException e) {
            submonval.setText("Value: ");
            subsource.setText("Source Timestamp: ");
            subserv.setText("Server Timestamp: ");
            substatus.setText("Status: ");
        }
        if (!(notification.getValue().getValue().getValue() instanceof Integer)) {
            return convertView;
        }
        btnAddAlarm.setVisibility(View.VISIBLE);
        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlarm(elements, position1, position);
            }
        });
        return convertView;
    }

    @SuppressLint("SetTextI18n")
    private void createAlarm(final List<SubscriptionElement> elements, final int position1, final int position2) {
        final Dialog dialogAlarm = new Dialog(getContext(), R.style.AppAlert);
        dialogAlarm.setContentView(R.layout.dialog_createalarm);
        final TextView currentValue = dialogAlarm.findViewById(R.id.current_value);
        final EditText edtMax = dialogAlarm.findViewById(R.id.edtMax);
        final EditText edtMin = dialogAlarm.findViewById(R.id.edtMin);
        Button btnOkAlarm = dialogAlarm.findViewById(R.id.btnOkAlarm);
        int curValue = elements.get(position1).getMonitoredItems().get(position2).getReadings().getFirst().getValue().getValue().intValue();
        currentValue.setText(getContext().getString(R.string.current_value)+curValue);

        btnOkAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtMax.getText().toString().equals("")||edtMin.getText().toString().equals("")){
                    ToastUtils.toast(getContext(), getContext().getString(R.string.insert_valid), TastyToast.WARNING);
                    return;
                }
                int max, min;
                max = Integer.parseInt(edtMax.getText().toString());
                min = Integer.parseInt(edtMin.getText().toString());
                if (max <= min) {
                    ToastUtils.toast(getContext(), getContext().getString(R.string.max_less_than_min), TastyToast.WARNING);
                } else {
                    hideSoftInput(v);
                    dialogAlarm.dismiss();
                    AlarmHelper.getInstance().addAlarm(elements, max, min, position1, position2);
                    ToastUtils.toast(getContext(), "OK");
                }
            }
        });
        dialogAlarm.show();
        setViewColor(Objects.requireNonNull(dialogAlarm.getWindow()).getDecorView());
        initInput(edtMax);
    }
}
