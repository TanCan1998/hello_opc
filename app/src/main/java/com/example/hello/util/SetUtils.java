/*
 * ************************************************************
 * 文件：SetUtils.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:18:56
 * 上次修改时间：2019/11/19 23:10:50
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.example.hello.R;
import com.litesuits.common.utils.InputMethodUtils;

import skin.support.content.res.SkinCompatResources;
import skin.support.utils.SkinPreference;

public class SetUtils {
    public static final int LOADING_DIALOG = 1;
    public static final int CONFIRM_DIALOG = 0;

    public static void setDialog(Dialog dialog, int style) {
        /*设置透明度*/
        Window window = dialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        if (style == LOADING_DIALOG) {
            lp.alpha = 0.7f;// 透明度
            lp.dimAmount = 0.3f;// 黑暗度
        } else {
            lp.alpha = 1.0f;// 透明度
            lp.dimAmount = 0.3f;// 黑暗度
        }
        window.setBackgroundDrawable(SkinCompatResources.getInstance().getDrawable(R.drawable.infobox_bg));
        window.setAttributes(lp);
    }

    public static void setViewColor(View view) {
        String skin = SkinPreference.getInstance().getSkinName();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewChild = vp.getChildAt(i);
                //再次 调用本身（递归）
                setViewColor(viewChild);
            }
        } else if (view.isEnabled() && !skin.equals("night")) {
            if (view instanceof RadioButton) {
                ((RadioButton) view).setButtonTintList(ColorStateList.valueOf(SkinCompatResources.getInstance().getColor(R.color.colorPrimaryDark)));
            } else if (view instanceof EditText) {
                view.setBackgroundTintList(ColorStateList.valueOf(SkinCompatResources.getInstance().getColor(R.color.colorPrimaryDark)));
            } else if (view instanceof CheckBox) {
                ((CheckBox) view).setButtonTintList(ColorStateList.valueOf(SkinCompatResources.getInstance().getColor(R.color.colorPrimaryDark)));
            }
        }
    }

    public static void hideSoftInput(View v) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        v.setFocusableInTouchMode(false);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void initInput(final View v) {
        v.requestFocus();
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodUtils.showSoftInput(v);
            }
        }, 50);
    }
}
