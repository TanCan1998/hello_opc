/*
 * ************************************************************
 * 文件：ChildLiistView.java  模块：app  项目：hello
 * 当前修改时间：2019/11/28 18:06:21
 * 上次修改时间：2019/11/28 18:06:21
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ChildListView extends ListView {

    public ChildListView(Context context) {
        super(context);
    }

    public ChildListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ChildListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


    /*********************** 不要拦截父控件ListView的下拉刷新事件 start *************************/
    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    */

    /**
     * 为了让ChildListView的adapter中的控件可以触发点击事件
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }


    /**
     * 为了让外层的AutoListView可以下拉刷新
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
    /*********************** 不要拦截父控件ListView的下拉刷新事件  end *************************/

}