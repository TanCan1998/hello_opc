/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:23:35
 * 上次修改时间：2019/11/22 15:45:31
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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hello.R;
import com.example.hello.ui.fragment.BrowseContainerFragment;
import com.example.hello.ui.fragment.BrowseFragment;
import com.example.hello.ui.fragment.SessionFragment;
import com.example.hello.ui.widget.MyFabOptions;
import com.example.hello.util.ActivityContainer;
import com.example.hello.util.AlarmHelper;
import com.example.hello.util.LogUtils;
import com.example.hello.util.SetUtils;
import com.example.hello.util.ToastUtils;
import com.example.hello.util.opc.ManagerOPC;
import com.example.hello.util.opc.SessionElement;
import com.example.hello.util.opc.thread.ThreadCreateSubscription;
import com.google.android.material.navigation.NavigationView;
import com.sdsmdg.tastytoast.TastyToast;

import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedByte;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.CreateSubscriptionRequest;

import java.io.File;
import java.util.Objects;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SessionFragment     mSessionFragment;
    private BrowseFragment      mBrowseFragment = new BrowseFragment();
    private Fragment            currentFragment;
    private boolean             isBrowsing      = false;
    private ManagerOPC          managerOPC;
    private int                 session_position;
    private SessionElement      sessionElement;
    private DrawerLayout        drawer;
    private String              TAG             = "asdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isBrowsing = false;
        LogUtils.i(TAG, "main onCreated");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            LottieAnimationView lv;

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (lv == null) {
                    lv = drawerView.findViewById(R.id.header_anim_view);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                lv.resumeAnimation();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                lv.cancelAnimation();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        //引入header和menu
        navigationView.inflateHeaderView(R.layout.nav_header_main);
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_session, R.id.nav_browse, R.id.nav_monitoring,
                R.id.nav_list_subscription, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_session || menuItem.getItemId() == R.id.nav_browse) {
                    toolbar.setTitle(menuItem.getTitle());
                    menuItem.setChecked(true);
                }
                drawer.closeDrawer(GravityCompat.START);
                pageJump(menuItem.getItemId());//判断跳转页面
                return false;
            }
        });
        try {
            Thread.sleep(50);
            setFab();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        init();
    }

    public void init() {
        managerOPC = ManagerOPC.getInstance();
        session_position = getIntent().getIntExtra("sessionPosition", -1);
        sessionElement = managerOPC.getSessions().get(session_position);
    }

    private void pageJump(int id) {
        switch (id) {
            case R.id.nav_session:
                isBrowsing = false;
                showFragment(mSessionFragment);
                break;
            case R.id.nav_browse:
                isBrowsing = true;
                showFragment(mBrowseFragment);
                break;
            case R.id.nav_monitoring:
                Intent intent_monitoring = new Intent(MainActivity.this, MonitoringActivity.class);
                intent_monitoring.putExtra("sessionPosition", session_position);
                startActivity(intent_monitoring);
                break;
            case R.id.nav_list_subscription:
                Intent intent = new Intent(MainActivity.this, ListSubscriptionActivity.class);
                intent.putExtra("sessionPosition", getIntent().getIntExtra("sessionPosition", -1));
                startActivity(intent);
                break;
            case R.id.nav_send:
                isBrowsing = false;
                setLocale();
                break;
            case R.id.nav_share:
                isBrowsing = false;
                shareApk();
                break;
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != fragment) {//  判断传入的fragment是不是当前的currentFragmentgit
            transaction.hide(currentFragment);//  不是则隐藏
            currentFragment = fragment;  //  然后将传入的fragment赋值给currentFragment
            transaction.setCustomAnimations(
                    R.anim.scale, R.anim.alpha_out);
            if (!fragment.isAdded()) { //  判断传入的fragment是否已经被add()过
                transaction.add(R.id.nav_host_fragment, fragment).show(fragment).commit();
            } else {
                transaction.show(fragment).commit();
            }
        }
    }

    public void bindSessionFragment(SessionFragment fragment) {
        currentFragment = mSessionFragment = fragment;
        getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment, currentFragment).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setFab() {//对fab按钮的设置
        final MyFabOptions fab = findViewById(R.id.fab_options);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.faboptions_subscription:
                        Intent intent = new Intent(MainActivity.this, ListSubscriptionActivity.class);
                        intent.putExtra("sessionPosition", getIntent().getIntExtra("sessionPosition", -1));
                        startActivity(intent);
                        break;
                    case R.id.faboptions_add:
                        createSubscription();
                        break;
                    case R.id.faboptions_read:
                        BrowseContainerFragment.readNode(BrowseContainerFragment.NonDesignated, MainActivity.this, sessionElement);
                        break;
                    case R.id.faboptions_write:
                        BrowseContainerFragment.writeNode(BrowseContainerFragment.NonDesignated, MainActivity.this, sessionElement);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void shareApk() {
        File apkFile = new File(getApplicationInfo().sourceDir);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", apkFile);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    private void setLocale() {
        Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
        startActivity(intent);
    }

    private void createSubscription() {
        final Dialog dialog_subscription = new Dialog(MainActivity.this, R.style.AppAlert);
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
                    ToastUtils.toast(MainActivity.this, getString(R.string.insert_valid), TastyToast.WARNING);
                } else {
                    SetUtils.hideSoftInput(v);
                    requestedLifetimeCount = new UnsignedInteger(edtRequestedLifetimeCount.getText().toString());
                    maxNotificationPerPublish = new UnsignedInteger(edtMaxNotificationPerPublish.getText().toString());
                    requestedPublishInterval = Double.parseDouble(edtRequestedPublishInterval.getText().toString());
                    requestedMaxKeepAliveCount = new UnsignedInteger(edtRequestedMaxKeepAliveCount.getText().toString());
                    priority = new UnsignedByte(edtPriority.getText().toString());
                    publishingEnabled = checkPublishingEnable.isChecked();
                    final int session_position = getIntent().getIntExtra("sessionPosition", -1);
                    if (requestedLifetimeCount.intValue() >= 3 * requestedMaxKeepAliveCount.intValue()) {
                        CreateSubscriptionRequest req = new CreateSubscriptionRequest(null, requestedPublishInterval, requestedLifetimeCount, requestedMaxKeepAliveCount, maxNotificationPerPublish, publishingEnabled, priority);
                        ThreadCreateSubscription t = new ThreadCreateSubscription(ManagerOPC.getInstance().getSessions().get(session_position), req);
                        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.connectingAttempt), getString(R.string.create_subscription), true);
                        SetUtils.setDialog(progressDialog, SetUtils.LOADING_DIALOG);
                        @SuppressLint("HandlerLeak") Handler handler_subscription = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                progressDialog.dismiss();
                                if (msg.what == -1) {
                                    ToastUtils.toastLong(MainActivity.this, getString(R.string.failed_to_create_subscription) + ((StatusCode) msg.obj).getDescription() + "\nCode: " + ((StatusCode) msg.obj).getValue().toString(), TastyToast.ERROR);
                                } else if (msg.what == -2) {
                                    ToastUtils.toastLong(getApplicationContext(), getString(R.string.error), TastyToast.ERROR);
                                } else {
                                    int position = (int) msg.obj;
                                    Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                                    intent.putExtra("subPosition", position);
                                    intent.putExtra("sessionPosition", session_position);
                                    startActivity(intent);
                                }
                            }
                        };
                        dialog_subscription.dismiss();
                        t.start(handler_subscription);
                    } else {
                        ToastUtils.toastLong(MainActivity.this, getString(R.string.invalid_constraint) + "\nlifetime_count>3*max_keep_alive_count", TastyToast.WARNING);
                    }
                }
            }
        });
        dialog_subscription.show();
        SetUtils.setViewColor(Objects.requireNonNull(dialog_subscription.getWindow()).getDecorView());
        SetUtils.initInput(edtRequestedPublishInterval);
    }

    @Override
    public void onBackPressed() {
        //响应返回键
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (isBrowsing && mBrowseFragment.onBackPress()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.session_close);
        builder.setMessage(R.string.session_close_notice);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        managerOPC.getSessions().remove(session_position);
                        sessionElement.getSession().closeAsync();
                        dialogInterface.dismiss();
                        finish();
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
        SetUtils.setDialog(g, SetUtils.CONFIRM_DIALOG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("fromMain", "yes");
            intent.putExtra("msg", bundle);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_exit);
            builder.setMessage(R.string.msg_exit);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            managerOPC.getSessions().remove(session_position);
                            sessionElement.getSession().closeAsync();
                            dialogInterface.dismiss();
                            ActivityContainer.getInstance().finishAllActivity();
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
            SetUtils.setDialog(g, SetUtils.CONFIRM_DIALOG);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            LogUtils.i(TAG, e.getMessage());
        }
        LogUtils.i(TAG, "main onSaveInstanceState");
    }

    public int getSession_position() {
        return mSessionFragment.getSession_position();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            managerOPC.getSessions().remove(session_position);
            sessionElement.getSession().closeAsync();
        } catch (Exception ignore) {
        }
        LogUtils.v(TAG, "main destroy");
        System.gc();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ToastUtils.toastLong(getApplicationContext(), getString(R.string.locale_changed), TastyToast.INFO);
    }

    @Override
    public void finish() {
        super.finish();
        AlarmHelper.getInstance().stopAlarm();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("fromNotification", false)) {
            pageJump(R.id.nav_monitoring);
            ShortcutBadger.removeCount(this);
        }
    }
}
