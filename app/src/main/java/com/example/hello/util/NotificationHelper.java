/*
 * ************************************************************
 * 文件：NotificationHelper.java  模块：app  项目：hello
 * 当前修改时间：2019/11/29 00:42:46
 * 上次修改时间：2019/11/29 00:42:46
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.hello.R;
import com.example.hello.ui.MyApplication;
import com.example.hello.ui.activity.MainActivity;

public class NotificationHelper {
    private static volatile NotificationHelper instance;
    private static          int                id;
    private final           String             CHANNEL_TWO_ID = "hello1";

    private NotificationHelper() {
        id = 0x11;
        id++;
        newNotification();
    }

    public static NotificationHelper getInstance() {
        if (instance == null) {
            synchronized (AlarmHelper.class) {
                if (instance == null) {
                    instance = new NotificationHelper();
                }
            }
        }
        return instance;
    }

    void createNotification(String title, String text) {
        Context context = MyApplication.context();
        NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(context);
        NotificationCompat.Builder notifyBuilder =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        new NotificationCompat.Builder(context, CHANNEL_TWO_ID) :
                        new NotificationCompat.Builder(context);
        notifyBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                // 点击消失
                .setAutoCancel(true)
                // 设置该通知优先级
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_foreground))
//                // 通知首次出现在通知栏，带上升动画效果的
//                .setTicker(title)
                // 通知产生的时间，会在通知信息里显示
                .setWhen(System.currentTimeMillis())
                // 向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fromNotification", true);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 1, intent
                        , PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //heads up notification
                .setFullScreenIntent(pendingIntent, false);
        notifyMgr.notify(id, notifyBuilder.build());
        id++;
    }

    public void deleteAll() {
        Context context = MyApplication.context();
        NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(context);
        notifyMgr.cancelAll();
    }

    private void newNotification() {
        NotificationManagerCompat mManager = NotificationManagerCompat.from(MyApplication.context());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_TWO_NAME = "消息警报";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_TWO_ID,
                    CHANNEL_TWO_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            mManager.createNotificationChannel(notificationChannel);
        }
    }
}
