/*
 * ************************************************************
 * 文件：MonitoredItemActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/19 23:10:50
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.MonitoredItemElement;
import com.example.hello.util.opc.SessionElement;
import com.example.hello.R;
import com.example.hello.ui.adapter.ReadMonitoredAdapter;
import com.example.hello.util.ToastUtils;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;


public class MonitoredItemActivity extends BaseActivity {
    public final static int REFRESH_RATE = 100;

    TextView                    txtData;
    SessionElement              sessionElement;
    int                         sub_pos;
    int                         mon_pos;
    SubscriptionAcknowledgement subAck;
    UnsignedInteger             LastSeqNumber;
    ListView                    listMonRead;
    ReadMonitoredAdapter        adapter;
    static boolean running = false;
    TextView txtBuffer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitored_item);
        txtData = findViewById(R.id.txtMonitoredDati);
        listMonRead = findViewById(R.id.listMonitoredRead);
        txtBuffer = findViewById(R.id.txtBufferLetture);
        LastSeqNumber = new UnsignedInteger(0);

        txtBuffer.setText(getString(R.string.breaking) + MonitoredItemElement.BUFFER_SIZE + getString(R.string.readings));
        int session_pos = getIntent().getIntExtra("sessionPosition", -1);
        sub_pos = getIntent().getIntExtra("subPosition", -1);
        mon_pos = getIntent().getIntExtra("monPosition", -1);

        sessionElement = ManagerOPC.getInstance().getSessions().get(session_pos);

        adapter = new ReadMonitoredAdapter(MonitoredItemActivity.this, R.layout.item_monitoredreadings, sessionElement.getSubscriptions().get(sub_pos).getMonitoredItems().get(mon_pos).getReadings());
        listMonRead.setAdapter(adapter);

        CreateMonitoredItemsResponse mi = sessionElement.getSubscriptions().get(sub_pos).getMonitoredItems().get(mon_pos).getMonitoredItem();
        String text = "Monitored Item ID: " + mi.getResults()[0].getMonitoredItemId() +
                "\nSampling Interval: " + mi.getResults()[0].getRevisedSamplingInterval() +
                "\nQueue Size: " + mi.getResults()[0].getRevisedQueueSize();
        txtData.setText(text);

        subAck = new SubscriptionAcknowledgement();
        subAck.setSubscriptionId(new UnsignedInteger(sessionElement.getSubscriptions().get(sub_pos).getSubscription().getSubscriptionId()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        setRunning(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getRunning()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    try {
                        Thread.sleep(REFRESH_RATE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setRunning(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.monitoreditem, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                setRunning(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (getRunning()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            try {
                                Thread.sleep(REFRESH_RATE);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                ToastUtils.toast(MonitoredItemActivity.this, getString(R.string.start_update), TastyToast.SUCCESS);
                break;
            case R.id.action_pause:
                ToastUtils.toast(MonitoredItemActivity.this, getString(R.string.pause_update), TastyToast.SUCCESS);
                setRunning(false);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static synchronized void setRunning(boolean running) {
        MonitoredItemActivity.running = running;
    }

    public static synchronized boolean getRunning() {
        return MonitoredItemActivity.running;
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
        toolbar.setTitle(R.string.moni_item_act_title);
    }

}
