/*
 * ************************************************************
 * 文件：ClearEditText.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 20:46:02
 * 上次修改时间：2019/11/22 20:46:02
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.Objects;

public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {

    private Drawable mClearDrawable;
    private boolean  hasFocus;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // getCompoundDrawablesRelative() Returns drawables for the start(0), top(1), end(2) and bottom(3)
        mClearDrawable = getCompoundDrawablesRelative()[2] == null ? getCompoundDrawables()[2] : getCompoundDrawablesRelative()[2]; // 获取drawableRight、End
        if (mClearDrawable == null) {
            // 如果为空，即没有设置drawableRight，则使用安卓自带图片
            mClearDrawable = getResources().getDrawable(android.R.drawable.ic_notification_clear_all);
        }
        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
        // 默认隐藏图标
        setClearDrawableVisible(false);
    }

    /**
     * 我们无法直接给EditText设置点击事件，只能通过按下的位置来模拟clear点击事件
     * 当我们按下的位置在图标包括图标到控件右边的间距范围内均算有效
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                int start = getWidth() - getTotalPaddingRight() + getPaddingRight(); // 起始位置
                int end = getWidth(); // 结束位置
                boolean available = (event.getX() > start) && (event.getX() < end);
                if (available) {
                    setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus && Objects.requireNonNull(getText()).length() > 0) {
            setClearDrawableVisible(true); // 有焦点且有文字时显示图标
        } else {
            setClearDrawableVisible(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (hasFocus) {
            setClearDrawableVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    protected void setClearDrawableVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

}