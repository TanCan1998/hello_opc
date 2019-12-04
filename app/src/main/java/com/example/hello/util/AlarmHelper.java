/*
 * ************************************************************
 * 文件：AlarmHelper.java  模块：app  项目：hello
 * 当前修改时间：2019/11/28 21:51:09
 * 上次修改时间：2019/11/28 21:51:09
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import com.example.hello.ui.MyApplication;
import com.example.hello.util.opc.SubscriptionElement;
import com.litesuits.common.utils.VibrateUtil;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class AlarmHelper {
    private                 boolean     alarming;
    private volatile static AlarmHelper instance;
    private                 int         badgeCount;

    private AlarmHelper() {
        badgeCount = 1;
    }

    public static AlarmHelper getInstance() {
        if (instance == null) {
            synchronized (AlarmHelper.class) {
                if (instance == null) {
                    instance = new AlarmHelper();
                }
            }
        }
        return instance;
    }

    private class Alarm {
        private List<SubscriptionElement> elements;
        private int                       max;
        private int                       min;
        private int                       position1;
        private int                       position2;

        Alarm(List<SubscriptionElement> elements, int max, int min, int position1, int position2) {
            this.elements = elements;
            this.max = max;
            this.min = min;
            this.position1 = position1;
            this.position2 = position2;
        }

        boolean shouldAlarm() {
            int value = getValue();
            return !(min < value && max > value);
        }

        int getValue() {
            return elements.get(position1).getMonitoredItems().get(position2).getReadings().getFirst().getValue().getValue().intValue();
        }

        int getId() {
            return elements.get(position1).getMonitoredItems().get(position2).getMonitoredItem().getResults()[0].getMonitoredItemId().intValue();
        }
    }

    public void addAlarm(List<SubscriptionElement> elements, int max, int min, int position1, int position2) {
        if (!alarming) {
            alarming = true;
        }
        startAlarm(new Alarm(elements, max, min, position1, position2));
    }

    private void startAlarm(final Alarm alarm) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (alarming) {
                    if (alarm.shouldAlarm()) {
                        VibrateUtil.vibrate(MyApplication.context(), 1200);
                        NotificationHelper.getInstance()
                                .createNotification("Warning! Item ID : "
                                        + alarm.getId(), "Current value : " + alarm.getValue());
                        ShortcutBadger.applyCount(MyApplication.context(), badgeCount++);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopAlarm() {
        ShortcutBadger.removeCount(MyApplication.context());
        alarming = false;
        badgeCount = 0;
    }
}
