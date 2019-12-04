package com.example.hello.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.hello.R;
import com.example.hello.ui.activity.SubscriptionActivity;
import com.example.hello.ui.adapter.NodeAdapter;
import com.example.hello.ui.adapter.SubscriptionAdapter;
import com.example.hello.util.SetUtils;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.BrowseDataStamp;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SessionElement;
import com.example.hello.util.opc.SubscriptionElement;
import com.example.hello.util.opc.thread.ThreadCreateMonitoredItem;
import com.example.hello.util.opc.thread.ThreadCreateSubscription;
import com.example.hello.util.opc.thread.ThreadRead;
import com.example.hello.util.opc.thread.ThreadWrite;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.DataChangeTrigger;
import org.opcfoundation.ua.core.DeadbandType;
import org.opcfoundation.ua.core.MonitoredItemCreateRequest;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.MonitoringParameters;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class BrowseContainerFragment extends Fragment {
    private             SessionElement             sessionElement;
    private static      ArrayList<BrowseDataStamp> data;
    public final static int                        NonDesignated = Integer.MIN_VALUE;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_container, container, false);

        Bundle bundle = getArguments();
        ListView listNode = view.findViewById(R.id.listNode);

        assert bundle != null;
        ArrayList<String> nodes = bundle.getStringArrayList("nodes");
        ArrayList<String> namespace = bundle.getStringArrayList("namespace");
        ArrayList<String> nodeindex = bundle.getStringArrayList("nodeindex");
        ArrayList<String> clazz = bundle.getStringArrayList("nodeclass");
        int sessionPosition = bundle.getInt("sessionPosition");

        sessionElement = ManagerOPC.getInstance().getSessions().get(sessionPosition);

        data = new ArrayList<>();
        assert nodes != null;
        assert namespace != null;
        assert nodeindex != null;
        assert clazz != null;
        for (int i = 0; i < nodes.size(); i++) {
            BrowseDataStamp tmp = new BrowseDataStamp(nodes.get(i), namespace.get(i), nodeindex.get(i), clazz.get(i), i);
            data.add(tmp);
        }
        Collections.sort(data, new Comparator<BrowseDataStamp>() {
            @Override
            public int compare(BrowseDataStamp o1, BrowseDataStamp o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        Collections.sort(data, new Comparator<BrowseDataStamp>() {
            @Override
            public int compare(BrowseDataStamp o1, BrowseDataStamp o2) {
                return o1.nodeclass.compareTo(o2.nodeclass);
            }
        });

        NodeAdapter adapter = new NodeAdapter(getContext(), R.layout.item_node, data);
        listNode.setAdapter(adapter);

        listNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BrowseDataStamp obj = (BrowseDataStamp) view.getTag();
                assert getParentFragment() != null;
                ((BrowseFragment) getParentFragment()).browseToPosition(obj.position, obj.name);
            }
        });


        listNode.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                String[] actions = {getString(R.string.read), getString(R.string.write), getString(R.string.menu_monitoring)};

                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle(getString(R.string.menu_actions));
                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                readNode(position, getContext(), sessionElement);
                                break;
                            case 1:
                                writeNode(position, getContext(), sessionElement);
                                break;
                            case 2:
                                createMonItem(position);
                                break;
                            default:
                                dialog.cancel();
                        }
                    }
                });
                Dialog g = builder.create();
                g.show();
                SetUtils.setDialog(g, SetUtils.CONFIRM_DIALOG);
                return true;
            }
        });

        return view;
    }

    private void createMonItem(final int position) {
        if (sessionElement.getSubscriptions().isEmpty()) {
            ToastUtils.toast(getContext(), getString(R.string.create_subscription_mon), TastyToast.INFO);

            final Dialog dialog_subscription = new Dialog(Objects.requireNonNull(getContext()), R.style.AppAlert);
            dialog_subscription.setContentView(R.layout.dialog_insertdatasubscription);

            final EditText edtRequestedPublishInterval = dialog_subscription.findViewById(R.id.edtRequestedPublishingInterval);
            final EditText edtRequestedMaxKeepAliveCount = dialog_subscription.findViewById(R.id.edtRequestedMaxKeepAliveCount);
            final EditText edtRequestedLifetimeCount = dialog_subscription.findViewById(R.id.edtRequestedLifetimeCount);
            final EditText edtMaxNotificationPerPublish = dialog_subscription.findViewById(R.id.edtMaxNotificationPerPublish);
            final EditText edtPriority = dialog_subscription.findViewById(R.id.edtPriotity);
            final CheckBox checkPublishingEnable = dialog_subscription.findViewById(R.id.checkPublishingEnable);
            Button btnOkSubscription = dialog_subscription.findViewById(R.id.btnOkSubscription);

            edtRequestedLifetimeCount.setHint("Ex: " + ManagerOPC.Default_RequestedLifetimeCount);
            edtMaxNotificationPerPublish.setHint("Ex: " + ManagerOPC.Default_MaxNotificationsPerPublish);
            edtRequestedPublishInterval.setHint("Ex: " + ManagerOPC.Default_RequestedPublishingInterval);
            edtRequestedMaxKeepAliveCount.setHint("Ex: " + ManagerOPC.Default_RequestedMaxKeepAliveCount);
            edtPriority.setHint("Ex: " + ManagerOPC.Default_Priority);

            btnOkSubscription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double requestedPublishInterval;
                    UnsignedInteger requestedLifetimeCount;
                    UnsignedInteger requestedMaxKeepAliveCount;
                    UnsignedInteger maxNotificationPerPublish;
                    UnsignedByte priority;
                    boolean publishingEnabled;

                    if (edtRequestedLifetimeCount.getText().toString().length() == 0 ||
                            edtMaxNotificationPerPublish.getText().toString().length() == 0 ||
                            edtRequestedPublishInterval.getText().toString().length() == 0 ||
                            edtRequestedMaxKeepAliveCount.getText().toString().length() == 0 ||
                            edtPriority.getText().toString().length() == 0) {
                        ToastUtils.toast(getContext(), getString(R.string.insert_valid), TastyToast.WARNING);
                    } else {
                        SetUtils.hideSoftInput(v);
                        requestedLifetimeCount = new UnsignedInteger(edtRequestedLifetimeCount.getText().toString());
                        maxNotificationPerPublish = new UnsignedInteger(edtMaxNotificationPerPublish.getText().toString());
                        requestedPublishInterval = Double.parseDouble(edtRequestedPublishInterval.getText().toString());
                        requestedMaxKeepAliveCount = new UnsignedInteger(edtRequestedMaxKeepAliveCount.getText().toString());
                        priority = new UnsignedByte(edtPriority.getText().toString());
                        publishingEnabled = checkPublishingEnable.isChecked();
                        if (requestedLifetimeCount.intValue() >= 3 * requestedMaxKeepAliveCount.intValue()) {
                            CreateSubscriptionRequest req = new CreateSubscriptionRequest(null, requestedPublishInterval, requestedLifetimeCount, requestedMaxKeepAliveCount, maxNotificationPerPublish, publishingEnabled, priority);
                            ThreadCreateSubscription t = new ThreadCreateSubscription(sessionElement, req);
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.connectingAttempt), getString(R.string.creation_subscription), true);
                            @SuppressLint("HandlerLeak") Handler handler_subscription = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    progressDialog.dismiss();
                                    if (msg.what == -1) {
                                        ToastUtils.toastLong(getContext(), getString(R.string.failed_to_create_subscription) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.INFO);
                                    } else if (msg.what == -2) {
                                        ToastUtils.toast(getContext(), getString(R.string.requestTimeout), TastyToast.WARNING);
                                    } else {
                                        int subPosition = (int) msg.obj;
                                        createMonitoredItemWithSubscription(sessionElement.getSubscriptions().get(subPosition), position);
                                    }
                                }
                            };
                            dialog_subscription.dismiss();
                            t.start(handler_subscription);
                        } else {
                            ToastUtils.toastLong(getContext(), getString(R.string.invalid_constraint) + "\nLifetimeCount>3*max_keep_alive_count", TastyToast.WARNING);
                        }
                    }
                }
            });

            dialog_subscription.show();
            SetUtils.setViewColor(Objects.requireNonNull(dialog_subscription.getWindow()).getDecorView());
            SetUtils.initInput(edtRequestedPublishInterval);

        } else {
            ListView listSubscriptions;
            SubscriptionAdapter adapter;

            final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()), R.style.AppAlert);
            dialog.setContentView(R.layout.dialog_list_subscription);
            listSubscriptions = dialog.findViewById(R.id.listSubscriptions);
            adapter = new SubscriptionAdapter(getContext(), R.layout.item_subscriptions, sessionElement.getSubscriptions());
            listSubscriptions.setAdapter(adapter);
            listSubscriptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int subPosition, long id) {
                    createMonitoredItemWithSubscription(sessionElement.getSubscriptions().get(subPosition), position);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private void createMonitoredItemWithSubscription(final SubscriptionElement subscriptionElement, int position) {
        final MonitoredItemCreateRequest[] monitoredItems = new MonitoredItemCreateRequest[1];
        monitoredItems[0] = new MonitoredItemCreateRequest();

        final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()), R.style.AppAlert);
        dialog.setContentView(R.layout.dialog_createmonitoreditem);
        final Spinner timestamps = dialog.findViewById(R.id.spinnerTimestamp);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(getContext(), R.array.timestamps, android.R.layout.simple_spinner_dropdown_item);
        timestamps.setAdapter(spinneradapter);

        final EditText edtMonitoredNamespace = dialog.findViewById(R.id.edtMonitoredNamespace);
        final EditText edtMonitoredNodeID = dialog.findViewById(R.id.edtMonitoredNodeID);
        final EditText edtMonitoredSampling = dialog.findViewById(R.id.edtSamplingInterval);
        final EditText edtMonitoredQueue = dialog.findViewById(R.id.edtQueueSize);
        final CheckBox checkDiscardOldest = dialog.findViewById(R.id.checkDiscardOldest);
        final RadioGroup rdGroupFilter = dialog.findViewById(R.id.rdgroupDeadband);
        final EditText edtValDeadband = dialog.findViewById(R.id.edtValDeadband);

        edtMonitoredNamespace.setText(data.get(position).namespace);
        edtMonitoredNodeID.setText(data.get(position).nodeindex);
        edtMonitoredNamespace.setEnabled(false);
        edtMonitoredNodeID.setEnabled(false);
        edtMonitoredNamespace.setFocusable(false);
        edtMonitoredNodeID.setFocusable(false);

        edtMonitoredSampling.setHint("Ex: " + ManagerOPC.Default_SamplingInterval);
        edtMonitoredQueue.setHint("Ex: " + ManagerOPC.Default_QueueSize + "");
        edtValDeadband.setHint("Ex: " + ManagerOPC.Default_AbsoluteDeadBand + "");

        Button btnOkMonitored = dialog.findViewById(R.id.btnOkMonitoredItem);

        btnOkMonitored.setOnClickListener(new View.OnClickListener() {

            int namespace, nodeId;
            String nodeIdString;
            double samplingInterval, deadband;
            UnsignedInteger queueSize;
            boolean discardOldest;

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

                if (edtMonitoredNamespace.getText().toString().length() != 0
                        && edtMonitoredNodeID.getText().toString().length() != 0
                        && edtMonitoredSampling.getText().toString().length() != 0
                        && edtMonitoredQueue.getText().toString().length() != 0
                        && edtValDeadband.getText().toString().length() != 0) {
                    SetUtils.hideSoftInput(v);
                    namespace = Integer.parseInt(edtMonitoredNamespace.getText().toString());
                    try {
                        nodeId = Integer.parseInt(edtMonitoredNodeID.getText().toString());
                        nodeIdString = null;
                    } catch (Exception e) {
                        nodeIdString = edtMonitoredNodeID.getText().toString();
                    }
                    samplingInterval = Double.parseDouble(edtMonitoredSampling.getText().toString());
                    queueSize = new UnsignedInteger(edtMonitoredQueue.getText().toString());
                    discardOldest = checkDiscardOldest.isChecked();
                    DeadbandType deadbandType = null;
                    switch (rdGroupFilter.getCheckedRadioButtonId()) {
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
                    reqParams.setClientHandle(new UnsignedInteger(SubscriptionActivity.idchandle++));
                    reqParams.setSamplingInterval(samplingInterval);
                    reqParams.setQueueSize(queueSize);
                    reqParams.setDiscardOldest(discardOldest);
                    reqParams.setFilter(fil);
                    monitoredItems[0].setRequestedParameters(reqParams);
                    monitoredItems[0].setMonitoringMode(MonitoringMode.Reporting);
                    NodeId nodeId;
                    if (nodeIdString == null) {
                        nodeId = new NodeId(namespace, this.nodeId);
                    } else {
                        nodeId = new NodeId(namespace, nodeIdString);
                    }
                    monitoredItems[0].setItemToMonitor(new ReadValueId(nodeId, Attributes.Value, null, null));

                    final CreateMonitoredItemsRequest mi = new CreateMonitoredItemsRequest();
                    mi.setSubscriptionId(subscriptionElement.getSubscription().getSubscriptionId());
                    mi.setTimestampsToReturn(timestamp);
                    mi.setItemsToCreate(monitoredItems);

                    ThreadCreateMonitoredItem t = new ThreadCreateMonitoredItem(subscriptionElement, mi);
                    final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.connectingAttempt), getString(R.string.creating_monitored_item), true);
                    SetUtils.setDialog(progressDialog, SetUtils.LOADING_DIALOG);
                    @SuppressLint("HandlerLeak") Handler handler_monitoredItem = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            progressDialog.dismiss();
                            if (msg.what == -1) {
                                ToastUtils.toastLong(getContext(), getString(R.string.unknown_error) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.CONFUSING);
                            } else if (msg.what == -2) {
                                ToastUtils.toast(getContext(), getString(R.string.requestTimeout), TastyToast.WARNING);
                            } else if (msg.what == -3) {
                                ToastUtils.toastLong(getContext(), getString(R.string.error_toast) + msg.obj.toString(), TastyToast.ERROR);
                            } else {
                                //TODO create suitable message
                                ToastUtils.toast(getContext(), getString(R.string.created_successfully), TastyToast.SUCCESS);
                            }
                        }
                    };
                    t.start(handler_monitoredItem);
                    dialog.dismiss();
                } else {
                    ToastUtils.toast(getContext(), getString(R.string.insert_valid), TastyToast.WARNING);
                }
            }
        });
        dialog.show();
        SetUtils.setViewColor(Objects.requireNonNull(dialog.getWindow()).getDecorView());
        SetUtils.initInput(edtMonitoredSampling);
    }

    public static void readNode(int position, final Context context, final SessionElement sessionElement) {
        final Dialog dialogRead = new Dialog(context, R.style.AppAlert);
        dialogRead.setContentView(R.layout.dialog_inserdataread);
        final EditText edtNamespace = dialogRead.findViewById(R.id.edtNamespace);
        final EditText edtNodeId = dialogRead.findViewById(R.id.edtNodeID);
        final RadioGroup rdGroupTimestamp = dialogRead.findViewById(R.id.rdgrouptimestamp);
        final EditText edtMaxAge = dialogRead.findViewById(R.id.edtMaxAge);
        Button btnOkRead = dialogRead.findViewById(R.id.btnOkRead);
        EditText firstFocus = edtNamespace;

        if (position != NonDesignated) {
            edtNamespace.setText(data.get(position).namespace);
            edtNodeId.setText(data.get(position).nodeindex);
            edtNamespace.setEnabled(false);
            edtNodeId.setEnabled(false);
            edtNamespace.setFocusable(false);
            edtNodeId.setFocusable(false);
            firstFocus = edtMaxAge;
        }

        btnOkRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int namespace, nodeId;
                String nodeId_string;
                double maxAge;
                if (edtNamespace.getText().toString().length() == 0 || edtNodeId.getText().toString().length() == 0 || edtMaxAge.getText().toString().length() == 0) {
                    ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
                } else {
                    SetUtils.hideSoftInput(v);
                    namespace = Integer.parseInt(edtNamespace.getText().toString());

                    if (TextUtils.isDigitsOnly(edtNodeId.getText())) {
                        nodeId = Integer.parseInt(edtNodeId.getText().toString());
                        nodeId_string = null;
                    } else {
                        nodeId = -1;
                        nodeId_string = edtNodeId.getText().toString();
                    }
                    maxAge = Double.parseDouble(edtMaxAge.getText().toString());
                    TimestampsToReturn timestamps = TimestampsToReturn.Both;
                    switch (rdGroupTimestamp.getCheckedRadioButtonId()) {
                        case R.id.rdServer:
                            timestamps = TimestampsToReturn.Server;
                            break;
                        case R.id.rdSource:
                            timestamps = TimestampsToReturn.Source;
                            break;
                        case R.id.rdBoth:
                            timestamps = TimestampsToReturn.Both;
                            break;
                        case R.id.rdNeither:
                            timestamps = TimestampsToReturn.Neither;
                            break;
                    }

                    ThreadRead t;
                    if (nodeId_string == null) {
                        t = new ThreadRead(sessionElement.getSession(), maxAge, timestamps, namespace, nodeId, Attributes.Value);
                    } else {
                        t = new ThreadRead(sessionElement.getSession(), maxAge, timestamps, namespace, nodeId_string, Attributes.Value);
                    }

                    final ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.connectingAttempt), context.getString(R.string.reading_in_progress), true);
                    SetUtils.setDialog(progressDialog, SetUtils.LOADING_DIALOG);
                    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == -1) {
                                ToastUtils.toastLong(context, context.getString(R.string.failed_reading) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                            } else if (msg.what == -2) {
                                ToastUtils.toast(context, context.getString(R.string.requestTimeout), TastyToast.ERROR);
                            } else {
                                ReadResponse res = (ReadResponse) msg.obj;
                                DataValue dv = res.getResults()[0];
                                Object obj = dv.getValue().getValue();
                                String types = obj != null ? obj.getClass().getName() : "null";
                                String text = "Value: " + obj +
                                        "\nTypes: " + types +
                                        "\nStatus: " + dv.getStatusCode() +
                                        "\nServerTimestamp: " + dv.getServerTimestamp() +
                                        "\nSourceTimestamp: " + dv.getSourceTimestamp();
                                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle(context.getString(R.string.result));
                                alertDialog.setMessage(text);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                SetUtils.setDialog(alertDialog, SetUtils.CONFIRM_DIALOG);
                            }
                            progressDialog.dismiss();
                        }
                    };
                    t.start(handler);
                    dialogRead.dismiss();
                }
            }
        });
        dialogRead.show();
        SetUtils.setViewColor(Objects.requireNonNull(dialogRead.getWindow()).getDecorView());
        SetUtils.initInput(firstFocus);
    }

    public static void writeNode(int position, final Context context, final SessionElement sessionElement) {
        final Dialog dialog_write = new Dialog(context, R.style.AppAlert);
        dialog_write.setContentView(R.layout.dialog_inserdatawrite);
        final EditText edtNamespace_write = dialog_write.findViewById(R.id.edtNamespaceWrite);
        final EditText edtNodeId_write = dialog_write.findViewById(R.id.edtNodeIDWrite);
        final EditText edtValue_write = dialog_write.findViewById(R.id.edtValueWrite);
        Button btnOkWrite = dialog_write.findViewById(R.id.btnOkWrite);
        EditText firstFocus = edtNamespace_write;

        if (position != NonDesignated) {
            edtNamespace_write.setText(data.get(position).namespace);
            edtNodeId_write.setText(data.get(position).nodeindex);
            edtNamespace_write.setFocusable(false);
            edtNodeId_write.setFocusable(false);
            edtNamespace_write.setEnabled(false);
            edtNodeId_write.setEnabled(false);
            firstFocus = edtValue_write;
        }

        final Spinner spinnerType = dialog_write.findViewById(R.id.spinnertype);
        final ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(context, R.array.WriteType, android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinneradapter);

        btnOkWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int namespace, nodeId;
                String nodeId_string;
                Variant value_write = null;

                if (checkValue(dialog_write, context)) {
                    SetUtils.hideSoftInput(v);
                    namespace = Integer.parseInt(edtNamespace_write.getText().toString());

                    if (TextUtils.isDigitsOnly(edtNodeId_write.getText())) {
                        nodeId = Integer.parseInt(edtNodeId_write.getText().toString());
                        nodeId_string = null;
                    } else {
                        nodeId = -1;
                        nodeId_string = edtNodeId_write.getText().toString();
                    }
                    String value = edtValue_write.getText().toString();
                    switch (spinnerType.getSelectedItem().toString()) {
                        case "Integer":
                            value_write = new Variant(Integer.parseInt(value));
                            break;
                        case "Float":
                            value_write = new Variant(Float.parseFloat(value));
                            break;
                        case "Double":
                            value_write = new Variant(Double.parseDouble(value));
                            break;
                        case "String":
                            value_write = new Variant(value);
                            break;
                        case "Boolean":
                            if (value.compareToIgnoreCase("true") == 0) {
                                value_write = new Variant(true);
                            } else if (value.compareToIgnoreCase("false") == 0) {
                                value_write = new Variant(false);
                            }
                            break;
                    }
                    ThreadWrite t;

                    final ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.connectingAttempt), context.getString(R.string.write_in_progress), true);
                    SetUtils.setDialog(progressDialog, SetUtils.LOADING_DIALOG);
                    if (nodeId_string == null) {
                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeId, Attributes.Value, value_write);
                    } else {
                        t = new ThreadWrite(sessionElement.getSession(), namespace, nodeId_string, Attributes.Value, value_write);
                    }
                    @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            progressDialog.dismiss();
                            if (msg.what == -1) {
                                ToastUtils.toastLong(context, context.getString(R.string.write_failed) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                            } else if (msg.what == -2) {
                                ToastUtils.toast(context, context.getString(R.string.requestTimeout), TastyToast.ERROR);
                            } else {
                                WriteResponse res = (WriteResponse) msg.obj;
                                String response = res.getResults()[0].getDescription();
                                int style = TastyToast.SUCCESS;
                                int duration = TastyToast.LENGTH_SHORT;
                                if (response.length() > 0) {
                                    response = "\n" + res.getResults()[0].getDescription();
                                    style = TastyToast.CONFUSING;
                                    duration = TastyToast.LENGTH_LONG;
                                }
                                ToastUtils.toast(context, context.getString(R.string.sent_values) + response, duration, style);
                            }
                        }
                    };
                    t.start(handler);
                    dialog_write.dismiss();
                }
            }
        });
        dialog_write.show();
        SetUtils.setViewColor(Objects.requireNonNull(dialog_write.getWindow()).getDecorView());
        SetUtils.initInput(firstFocus);
    }

    private static boolean checkValue(Dialog dialog_write, Context context) {
        final EditText edtNamespace_write = dialog_write.findViewById(R.id.edtNamespaceWrite);
        final EditText edtNodeId_write = dialog_write.findViewById(R.id.edtNodeIDWrite);
        final EditText edtValue_write = dialog_write.findViewById(R.id.edtValueWrite);
        final Spinner spinnerType = dialog_write.findViewById(R.id.spinnertype);
        if (edtNamespace_write.getText().toString().length() == 0 || edtNodeId_write.getText().toString().length() == 0 || edtValue_write.getText().toString().length() == 0) {
            ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
            return false;
        } else {
            String value = edtValue_write.getText().toString();
            switch (spinnerType.getSelectedItem().toString()) {
                case "Integer":
                    try {
                        Integer.parseInt(value);
                        return true;
                    } catch (NumberFormatException ignored) {
                    }
                    ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
                    return false;
                case "Float":
                    try {
                        Float.parseFloat(value);
                        return true;
                    } catch (NumberFormatException ignored) {
                    }
                    ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
                    return false;
                case "Double":
                    try {
                        Double.parseDouble(value);
                        return true;
                    } catch (NumberFormatException ignored) {
                    }
                    ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
                    return false;
                case "String":
                    return true;
                case "Boolean":
                    if (value.compareToIgnoreCase("true") == 0) {
                        return true;
                    } else if (value.compareToIgnoreCase("false") == 0) {
                        return true;
                    } else {
                        ToastUtils.toast(context, context.getString(R.string.insert_valid), TastyToast.WARNING);
                    }
                    break;
            }
            return false;
        }
    }
}