/*
 * ************************************************************
 * 文件：SubscriptionActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/20 14:25:26
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.example.hello.R;
import com.example.hello.ui.adapter.MonitoredItemAdapter;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SubscriptionElement;
import com.example.hello.util.opc.thread.ThreadCreateMonitoredItem;
import com.example.hello.util.opc.thread.ThreadDeleteSubscription;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;

import java.util.Objects;

import static com.example.hello.util.SetUtils.CONFIRM_DIALOG;
import static com.example.hello.util.SetUtils.LOADING_DIALOG;
import static com.example.hello.util.SetUtils.setDialog;
import static com.example.hello.util.SetUtils.setViewColor;


public class SubscriptionActivity extends BaseActivity {

    public static int idchandle = 0;
    ManagerOPC          managerOPC;
    SubscriptionElement subscriptionElement;
    TextView            txtInfoSubscription, txtSubscriptionParameters;
    Button               btnNewMonitoredItem;
    ListView             listMonitoredItem;
    MonitoredItemAdapter adapter;

    int             namespace;
    int             nodeid;
    String          nodeid_String;
    double          sampling_interval;
    UnsignedInteger queue_size;
    boolean         discard_oldest;
    double          deadband;
    int             session_position;
    int             sub_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        managerOPC = ManagerOPC.getInstance();
        session_position = getIntent().getIntExtra("sessionPosition", -1);
        sub_position = getIntent().getIntExtra("subPosition", -1);
        if (session_position < 0 || sub_position < 0) {
            ToastUtils.toast(SubscriptionActivity.this, getString(R.string.subscription_read_error), TastyToast.ERROR);
            finish();
        }

        subscriptionElement = managerOPC.getSessions().get(session_position).getSubscriptions().get(sub_position);

        txtInfoSubscription = findViewById(R.id.txtSubscriptionInfo);
        txtSubscriptionParameters = findViewById(R.id.txtParameterSubscription);
        btnNewMonitoredItem = findViewById(R.id.btnCreateMonitoredItem);
        listMonitoredItem = findViewById(R.id.listMonitoredItem);

        adapter = new MonitoredItemAdapter(SubscriptionActivity.this, R.id.listMonitoredItem, subscriptionElement.getMonitoredItems());
        listMonitoredItem.setAdapter(adapter);

        String text = "Endpoint\n" + subscriptionElement.getSession().getSession().getEndpoint().getEndpointUrl() +
                "\nSessionID\n" + subscriptionElement.getSession().getSession().getName() +
                "\nSubscriptionID\n" + subscriptionElement.getSubscription().getSubscriptionId();
        txtInfoSubscription.setText(text);

        text = getString(R.string.returned_parameters) +
                "\nPublishInterval: " + subscriptionElement.getSubscription().getRevisedPublishingInterval() +
                "\nLifetimeCount: " + subscriptionElement.getSubscription().getRevisedLifetimeCount() +
                "\nMaxKeepAliveCount: " + subscriptionElement.getSubscription().getRevisedMaxKeepAliveCount();
        txtSubscriptionParameters.setText(text);

        btnNewMonitoredItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MonitoredItemCreateRequest[] monitoredItems = new MonitoredItemCreateRequest[1];
                monitoredItems[0] = new MonitoredItemCreateRequest();


                final Dialog dialog = new Dialog(SubscriptionActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_createmonitoreditem);
                final Spinner timestamps = dialog.findViewById(R.id.spinnerTimestamp);
                ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(SubscriptionActivity.this, R.array.timestamps, android.R.layout.simple_spinner_dropdown_item);
                timestamps.setAdapter(spinneradapter);

                final EditText edtMonitoredNamespace = dialog.findViewById(R.id.edtMonitoredNamespace);
                final EditText edtMonitoredNodeID = dialog.findViewById(R.id.edtMonitoredNodeID);
                final EditText edtMonitoredSampling = dialog.findViewById(R.id.edtSamplingInterval);
                final EditText edtMonitoredQueue = dialog.findViewById(R.id.edtQueueSize);
                final CheckBox checkDiscardOldest = dialog.findViewById(R.id.checkDiscardOldest);
                final RadioGroup rdgroupfiltro = dialog.findViewById(R.id.rdgroupDeadband);
                final EditText edtValDeadband = dialog.findViewById(R.id.edtValDeadband);

                edtMonitoredSampling.setHint("Ex: " + ManagerOPC.Default_SamplingInterval);
                edtMonitoredQueue.setHint("Ex: " + ManagerOPC.Default_QueueSize + "");
                edtValDeadband.setHint("Ex: " + ManagerOPC.Default_AbsoluteDeadBand + "");


                Button btnOkMonitored = dialog.findViewById(R.id.btnOkMonitoredItem);

                btnOkMonitored.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimestampsToReturn timestamp = null;
                        switch (timestamps.getSelectedItem().toString()) {
                            case "Server":
                                timestamp = TimestampsToReturn.Server;
                                break;
                            case "Source":
                                timestamp = TimestampsToReturn.Source;
                                break;
                            case "Both":
                                timestamp = TimestampsToReturn.Both;
                                break;
                            case "Neither":
                                timestamp = TimestampsToReturn.Neither;
                                break;
                        }


                        if (edtMonitoredNamespace.getText().toString().length() != 0 && edtMonitoredNodeID.getText().toString().length() != 0 && edtMonitoredSampling.getText().toString().length() != 0
                                && edtMonitoredQueue.getText().toString().length() != 0 && edtValDeadband.getText().toString().length() != 0) {
                            namespace = Integer.parseInt(edtMonitoredNamespace.getText().toString());
                            try {
                                nodeid = Integer.parseInt(edtMonitoredNodeID.getText().toString());
                                nodeid_String = null;
                            } catch (Exception e) {
                                nodeid_String = edtMonitoredNodeID.getText().toString();
                            }
                            sampling_interval = Double.parseDouble(edtMonitoredSampling.getText().toString());
                            queue_size = new UnsignedInteger(edtMonitoredQueue.getText().toString());
                            discard_oldest = checkDiscardOldest.isChecked();
                            DeadbandType deadbandType = null;
                            switch (rdgroupfiltro.getCheckedRadioButtonId()) {
                                case R.id.rdAbsolute:
                                    deadbandType = DeadbandType.Absolute;
                                    break;
                                case R.id.rdPercentage:
                                    deadbandType = DeadbandType.Percent;
                                    break;
                            }
                            deadband = Double.parseDouble(edtValDeadband.getText().toString());

                            DataChangeFilter filter = new DataChangeFilter();
                            filter.setTrigger(DataChangeTrigger.StatusValue);
                            assert deadbandType != null;
                            filter.setDeadbandType(new UnsignedInteger(deadbandType.getValue()));
                            filter.setDeadbandValue(deadband);
                            ExtensionObject fil = new ExtensionObject(filter);

                            MonitoringParameters reqParams = new MonitoringParameters();
                            reqParams.setClientHandle(new UnsignedInteger(idchandle++));
                            reqParams.setSamplingInterval(sampling_interval);
                            reqParams.setQueueSize(queue_size);
                            reqParams.setDiscardOldest(discard_oldest);
                            reqParams.setFilter(fil);
                            monitoredItems[0].setRequestedParameters(reqParams);
                            monitoredItems[0].setMonitoringMode(MonitoringMode.Reporting);
                            NodeId nodeId;
                            if (nodeid_String == null)
                                nodeId = new NodeId(namespace, nodeid);
                            else
                                nodeId = new NodeId(namespace, nodeid_String);
                            monitoredItems[0].setItemToMonitor(new ReadValueId(nodeId, Attributes.Value, null, null));

                            final CreateMonitoredItemsRequest mi = new CreateMonitoredItemsRequest();
                            mi.setSubscriptionId(subscriptionElement.getSubscription().getSubscriptionId());
                            mi.setTimestampsToReturn(timestamp);
                            mi.setItemsToCreate(monitoredItems);

                            ThreadCreateMonitoredItem t = new ThreadCreateMonitoredItem(subscriptionElement, mi);
                            final ProgressDialog progressDialog = ProgressDialog.show(SubscriptionActivity.this, getString(R.string.connectingAttempt), getString(R.string.creating_monitored_item), true);
                            @SuppressLint("HandlerLeak") Handler handler_monitoreditem = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    progressDialog.dismiss();
                                    if (msg.what == -1) {
                                        ToastUtils.toastLong(getApplicationContext(), getString(R.string.unknown_error) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.CONFUSING);
                                    } else if (msg.what == -2) {
                                        ToastUtils.toast(getApplicationContext(), getString(R.string.requestTimeout), TastyToast.ERROR);
                                    } else if (msg.what == -3) {
                                        ToastUtils.toastLong(getApplicationContext(), getString(R.string.error_toast) + msg.obj.toString(), TastyToast.ERROR);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                        listMonitoredItem.setSelection(adapter.getCount() - 1);
                                    }
                                }
                            };
                            t.start(handler_monitoreditem);
                            dialog.dismiss();
                        } else {
                            ToastUtils.toast(SubscriptionActivity.this, getString(R.string.insert_valid), TastyToast.WARNING);
                        }
                    }
                });
                dialog.show();
                setViewColor(Objects.requireNonNull(dialog.getWindow()).getDecorView());
            }

        });

        listMonitoredItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubscriptionActivity.this, MonitoredItemActivity.class);
                intent.putExtra("sessionPosition", session_position);
                intent.putExtra("subPosition", sub_position);
                intent.putExtra("monPosition", position);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subscription, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_terminate) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SubscriptionActivity.this);
            builder.setTitle(R.string.close_subscription);
            builder.setMessage(R.string.closing_subscription);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            ThreadDeleteSubscription t = new ThreadDeleteSubscription(subscriptionElement.getSession(), subscriptionElement.getSubscription().getSubscriptionId());
                            final ProgressDialog progressDialog = ProgressDialog.show(SubscriptionActivity.this, getString(R.string.connectingAttempt), getString(R.string.cancel_subscription), true);
                            setDialog(progressDialog, LOADING_DIALOG);
                            @SuppressLint("HandlerLeak") Handler handler_delete_subscription = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    progressDialog.dismiss();
                                    if (msg.what == -1) {
                                        ToastUtils.toastLong(SubscriptionActivity.this, getString(R.string.elimination_subscription_failed) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                                    } else if (msg.what == -2) {
                                        ToastUtils.toast(getApplicationContext(), getString(R.string.requestTimeout), TastyToast.ERROR);
                                    } else {
                                        managerOPC.getSessions().get(session_position).getSubscriptions().remove(sub_position);
                                        finish();
                                    }
                                }
                            };
                            t.start(handler_delete_subscription);
                            dialogInterface.dismiss();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialogInterface.dismiss();
                            break;
                    }
                }
            };
            builder.setPositiveButton(android.R.string.yes, listener);
            builder.setNegativeButton(android.R.string.no, listener);
            builder.setCancelable(false);
            Dialog g = builder.create();
            g.show();
            setDialog(g, CONFIRM_DIALOG);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.subscription_information);
    }
}
