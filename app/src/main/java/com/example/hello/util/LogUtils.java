/*
 * ************************************************************
 * 文件：LogUtils.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:18:56
 * 上次修改时间：2019/11/22 15:45:31
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.util.Log;

import com.example.hello.BuildConfig;

public class LogUtils {

    public static void v(String TAG, String msg) {
        v(TAG, msg, null);
    }

    public static void v(String TAG, String msg, Throwable e) {
        log('v', TAG, msg, e);
    }

    public static void e(String TAG, String msg) {
        e(TAG, msg, null);
    }

    public static void e(String TAG, String msg, Throwable e) {
        log('e', TAG, msg, e);
    }

    public static void d(String TAG, String msg) {
        d(TAG, msg, null);
    }

    public static void d(String TAG, String msg, Throwable e) {
        log('d', TAG, msg, e);
    }

    public static void i(String TAG, String msg) {
        i(TAG, msg, null);
    }

    public static void i(String TAG, String msg, Throwable e) {
        log('i', TAG, msg, e);
    }

    public static void w(String TAG, String msg) {
        w(TAG, msg, null);
    }

    public static void w(String TAG, String msg, Throwable e) {
        log('w', TAG, msg, e);
    }

    private static void log(char type, String TAG, String msg, Throwable e) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        switch (type) {
            case 'v':
                Log.v(TAG, msg, e);
                break;
            case 'e':
                Log.e(TAG, msg, e);
                break;
            case 'd':
                Log.d(TAG, msg, e);
                break;
            case 'i':
                Log.i(TAG, msg, e);
                break;
            case 'w':
                Log.w(TAG, msg, e);
                break;
        }
    }
}
