/*
 * ************************************************************
 * 文件：MonitoringActivity.java  模块：app  项目：hello
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

import com.example.hello.R;
import com.example.hello.ui.adapter.MonitoringAdapter;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SessionElement;
import com.sdsmdg.tastytoast.TastyToast;

public class MonitoringActivity extends BaseActivity {

    TextView          txtSessionMonitoring;
    ListView          listMonitoring;
    int               session_position;
    ManagerOPC        managerOPC;
    SessionElement    sessionElement;
    MonitoringAdapter adapter;
    static boolean running = true;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        listMonitoring = findViewById(R.id.listMonitoring);
        txtSessionMonitoring = findViewById(R.id.txtSessionMonitoring);

        managerOPC = ManagerOPC.getInstance();

        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            ToastUtils.toast(MonitoringActivity.this, getString(R.string.error_reading_session), TastyToast.ERROR);
            finish();
        }

        sessionElement = managerOPC.getSessions().get(session_position);

        txtSessionMonitoring.setText(getString(R.string.session_id) + "\n" + sessionElement.getSession().getSession().getName());

        adapter = new MonitoringAdapter(MonitoringActivity.this, R.layout.item_monitoring, sessionElement.getSubscriptions());
        listMonitoring.setAdapter(adapter);
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
                        Thread.sleep(MonitoredItemActivity.REFRESH_RATE);
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
        running = false;
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
                                Thread.sleep(MonitoredItemActivity.REFRESH_RATE);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                ToastUtils.toast(MonitoringActivity.this, getString(R.string.start_update), TastyToast.SUCCESS);
                break;
            case R.id.action_pause:
                ToastUtils.toast(MonitoringActivity.this, getString(R.string.pause_update), TastyToast.SUCCESS);
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
        toolbar.setTitle(R.string.moni_act_title);
    }

}
