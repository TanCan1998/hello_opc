/*
 * ************************************************************
 * 文件：KeepLiveService.java  模块：app  项目：hello
 * 当前修改时间：2019/11/27 17:28:49
 * 上次修改时间：2019/11/27 17:28:49
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.hello.R;
import com.example.hello.util.LogUtils;
import com.example.hello.util.NotificationHelper;

public class KeepLiveService extends Service {
    public static final  int    NOTIFICATION_ID = 0x11;
    private static final String TAG             = "Service";
    String                    CHANNEL_ONE_ID   = "hello";
    String                    CHANNEL_ONE_NAME = "前台服务";
    NotificationChannel       notificationChannel;
    NotificationManagerCompat mManager;
    @SuppressLint("StaticFieldLeak")
    static         KeepLiveService instance;
    private static boolean         stop = true;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.v(TAG, "KeepLiveService  onCreate");
        //API 18以上，发送Notification并将其置为前台
        newNotification();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                //设置通知标题
                .setContentTitle(getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        startForeground(NOTIFICATION_ID, builder.build());
        instance = this;
        stop = false;
    }

    public static void stop() {
        try {
            if (!isStop()) {
                instance.stopSelf();
                instance = null;
                stop = true;
                NotificationHelper.getInstance().deleteAll();
            }
        } catch (Exception ignore) {
        }
    }

    public static boolean isStop() {
        return stop;
    }

    private void newNotification() {
        mManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
            mManager.createNotificationChannel(notificationChannel);
        }
    }

}
