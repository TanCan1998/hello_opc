/*
 * ************************************************************
 * 文件：ListSubscriptionActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/19 23:10:50
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.example.hello.R;
import com.example.hello.ui.adapter.SubscriptionAdapter;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.sdsmdg.tastytoast.TastyToast;

public class ListSubscriptionActivity extends BaseActivity {

    ListView            listSubscriptions;
    SubscriptionAdapter adapter;
    ManagerOPC          manager;
    int                 session_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_subscription);

        manager = ManagerOPC.getInstance();
        session_position = getIntent().getIntExtra("sessionPosition", -1);
        if (session_position < 0) {
            ToastUtils.toast(ListSubscriptionActivity.this, getString(R.string.error), TastyToast.ERROR);
            finish();
        }

        listSubscriptions = findViewById(R.id.listSubscriptions);
        adapter = new SubscriptionAdapter(ListSubscriptionActivity.this, R.layout.item_subscriptions, manager.getSessions().get(session_position).getSubscriptions());
        listSubscriptions.setAdapter(adapter);
        listSubscriptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListSubscriptionActivity.this, SubscriptionActivity.class);
                intent.putExtra("subPosition", position);
                intent.putExtra("sessionPosition", session_position);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
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
        toolbar.setTitle(R.string.list_sub_act_title);
    }
}