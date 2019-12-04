/*
 * ************************************************************
 * 文件：ConnectingActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/20 16:52:38
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hello.R;
import com.example.hello.service.KeepLiveService;
import com.example.hello.ui.adapter.EndpointsAdapter;
import com.example.hello.ui.widget.HistoryEditText;
import com.example.hello.util.AnimHelper;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SessionElement;
import com.example.hello.util.opc.thread.ThreadCreateSession;
import com.example.hello.util.opc.thread.ThreadDiscoveryEndpoints;
import com.litesuits.common.utils.VibrateUtil;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.EndpointDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import skin.support.content.res.SkinCompatResources;

import static com.example.hello.util.SetUtils.CONFIRM_DIALOG;
import static com.example.hello.util.SetUtils.LOADING_DIALOG;
import static com.example.hello.util.SetUtils.hideSoftInput;
import static com.example.hello.util.SetUtils.setDialog;
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;
import static org.opcfoundation.ua.utils.EndpointUtil.sortBySecurityLevel;

public class ConnectingActivity extends BaseActivity {

    HistoryEditText           edtURL;
    Button                    btnConnects;
    ListView                  listEndpoints;
    ManagerOPC                manager;
    ProgressDialog            dialog;
    EndpointDescription[]     endpoints;
    List<EndpointDescription> endpoints_list;
    EndpointsAdapter          adapter;
    String                    url;
    long                      lastClick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        File certFile = new File(getFilesDir(), "OPCCert.der");
        File privKeyFile = new File(getFilesDir(), "OPCCert.pem");

        manager = ManagerOPC.CreateManagerOPC(certFile, privKeyFile);

        edtURL = findViewById(R.id.edtURL);
        btnConnects = findViewById(R.id.btnConnect);
        listEndpoints = findViewById(R.id.ListEndpoints);

        endpoints_list = new ArrayList<>();
        adapter = new EndpointsAdapter(getApplicationContext(), R.layout.activity_connecting, endpoints_list);
        listEndpoints.setAdapter(adapter);

        findViewById(R.id.connect_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(v);
            }
        });

        edtURL.setHistoryFile("URL_history.txt");
        btnConnects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(v);
                endpoints_list.clear();
                if (!Objects.requireNonNull(edtURL.getText()).toString().toLowerCase().startsWith("opc.tcp://")) {
                    url = "opc.tcp://" + edtURL.getText().toString();
                } else {
                    url = edtURL.getText().toString();
                }

                if (url.length() <= 10) {
                    ToastUtils.toast(getApplicationContext(), getString(R.string.invalidAddress), TastyToast.WARNING);
                } else {
                    Client client = manager.getClient();
                    dialog = ProgressDialog.show(ConnectingActivity.this, getString(R.string.connectingAttempt), getString(R.string.requestEndpoints), true);
                    setDialog(dialog, LOADING_DIALOG);
                    ThreadDiscoveryEndpoints t = new ThreadDiscoveryEndpoints(client, url);

                    @SuppressLint("HandlerLeak") Handler handler_discovery = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            dialog.dismiss();
                            if (msg.what == -1) {
                                ToastUtils.toast(getApplicationContext(), getString(R.string.endpointsNotFound) + "\n" + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.CONFUSING);
                            } else if (msg.what == -2) {
                                ToastUtils.toast(getApplicationContext(), getString(R.string.requestTimeout), TastyToast.WARNING);
                            } else {
                                endpoints = selectByProtocol(sortBySecurityLevel((EndpointDescription[]) msg.obj), "opc.tcp");
                                endpoints_list.addAll(Arrays.asList(endpoints));
                                edtURL.addToHistory();
                            }
                            adapter.notifyDataSetChanged();
                            listEndpoints.setVisibility(View.VISIBLE);
                            AnimHelper.getInstance()
                                    .getAnim(listEndpoints, null, null)
                                    .setDuration(600)
                                    .startAnimation();
                        }
                    };
                    t.start(handler_discovery);
                }
            }
        });


        listEndpoints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (endpoints[position].getEndpointUrl().toLowerCase().startsWith("opc.tcp")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConnectingActivity.this);
                    builder.setTitle(R.string.connectConfirm);
                    builder.setMessage(endpoints[position].getEndpointUrl() + "\n"
                            + "SecurityMode: " + endpoints[position].getSecurityMode() + "\n"
                            + "SecurityLevel: " + endpoints[position].getSecurityLevel()
                    );
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    ThreadCreateSession t = new ThreadCreateSession(manager, url, endpoints[position]);
                                    dialog = ProgressDialog.show(ConnectingActivity.this, getString(R.string.connectingAttempt), getString(R.string.sessionCreation), true);
                                    setDialog(dialog, LOADING_DIALOG);
                                    @SuppressLint("HandlerLeak") Handler handler_createsession = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            dialog.dismiss();
                                            if (msg.what == -1) {
                                                ToastUtils.toastLong(getApplicationContext(), getString(R.string.sessionNotCreate) + "\n" + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                                            } else if (msg.what == -2) {
                                                ToastUtils.toast(getApplicationContext(), getString(R.string.requestTimeout), TastyToast.WARNING);
                                            } else {
                                                int session_position = (int) msg.obj;
                                                Intent intent = new Intent(ConnectingActivity.this, MainActivity.class);
                                                intent.putExtra("sessionPosition", session_position);
                                                intent.putExtra("url", manager.getSessions().get(session_position).getUrl());
                                                startActivity(intent);
                                                VibrateUtil.vibrate(getApplicationContext(), 300);//振动
                                                listEndpoints.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    };
                                    t.start(handler_createsession);
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
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ConnectingActivity.this).create();
                    alertDialog.setTitle(R.string.notSupported);
                    alertDialog.setMessage(getString(R.string.protocolNotSupport));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    setDialog(alertDialog, LOADING_DIALOG);
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (SessionElement session : manager.getSessions()) {
            session.getSession().closeAsync();
        }
        manager.getSessions().clear();
        findViewById(R.id.connect_anim_view).clearAnimation();
        KeepLiveService.stop();
    }

    private boolean checkDoubleClick() {
        if (lastClick == 0 || new Date().getTime() - lastClick > 1500) {
            lastClick = new Date().getTime();
            ToastUtils.toast(getApplicationContext(), getString(R.string.exit), TastyToast.DEFAULT);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (checkDoubleClick()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(ConnectingActivity.this, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("fromConnecting", "yes");
            intent.putExtra("msg", bundle);
            startActivity(intent);
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LottieAnimationView lv = findViewById(R.id.connect_anim_view);
        lv.playAnimation();
        lv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                btnConnects.performClick();
                return true;
            }
        });
        listEndpoints.setBackground(SkinCompatResources.getInstance().getDrawable(R.drawable.infobox_bg));
        edtURL.setBackgroundTintList(SkinCompatResources.getInstance().getColorStateList(R.color.colorPrimaryDark));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LottieAnimationView) findViewById(R.id.connect_anim_view)).cancelAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (KeepLiveService.isStop()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, KeepLiveService.class));
            } else {
                startService(new Intent(this, KeepLiveService.class));
            }
        }
    }
}
