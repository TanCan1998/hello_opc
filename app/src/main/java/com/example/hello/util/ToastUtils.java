/*
 * ************************************************************
 * 文件：ToastUtils.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:18:56
 * 上次修改时间：2019/11/20 14:25:26
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.sdsmdg.tastytoast.TastyToast;

import es.dmoral.toasty.Toasty;

public class ToastUtils {
    private static Toast myToast;

    public static void toast(Context context, String msg) {
        toast(context, msg, TastyToast.INFO);
    }

    public static void toast(Context context, String msg, int style) {
        toast(context, msg, TastyToast.LENGTH_SHORT, style);
    }

    public static void toastLong(Context context, String msg, int style) {
        toast(context, msg, TastyToast.LENGTH_LONG, style);
    }

    public static void toast(Context context, String msg, int duration, int style) {
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = TastyToast.makeText(context, msg, duration, style);
    }

    public static void toasty(Context context, CharSequence msg, Drawable drawable, int tintColor, int textColor) {
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = Toasty.custom(context, msg, drawable, tintColor, textColor, Toast.LENGTH_SHORT, true, true);
        myToast.show();
    }

}
