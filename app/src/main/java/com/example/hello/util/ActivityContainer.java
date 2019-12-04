/*
 * ************************************************************
 * 文件：ActivityContainer.java  模块：app  项目：hello
 * 当前修改时间：2019/11/28 19:53:54
 * 上次修改时间：2019/11/28 19:53:54
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import com.example.hello.ui.activity.BaseActivity;
import com.example.hello.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityContainer {

    private ActivityContainer() {
    }

    private volatile static ActivityContainer  instance;
    private static          List<BaseActivity> activityStack;
    private static          MainActivity       main;

    public static ActivityContainer getInstance() {
        if (instance == null) {
            synchronized (ActivityContainer.class) {
                if (instance == null) {
                    instance = new ActivityContainer();
                }
            }
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(BaseActivity activity) {
        if (activityStack == null) {
            activityStack = new ArrayList<>();
        }
        activityStack.add(activity);
        if (activity instanceof MainActivity) {
            main = (MainActivity) activity;
        }
    }

    /**
     * 移除指定的Activity
     */
    public void removeActivity(BaseActivity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity = null;
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
        main = null;
    }

    public MainActivity getMain() {
        return main;
    }

}
