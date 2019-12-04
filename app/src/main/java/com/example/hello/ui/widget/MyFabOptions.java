/*
 * ************************************************************
 * 文件：MyFabOptions.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 00:51:41
 * 上次修改时间：2019/11/22 00:51:36
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.hello.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joaquimley.faboptions.FabOptions;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatHelper;
import skin.support.widget.SkinCompatSupportable;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

public class MyFabOptions extends FabOptions implements SkinCompatSupportable {
    private int mBackgroundColorResId;
    private int mFabColorResId;

    public MyFabOptions(Context context) {
        this(context, null);
    }

    public MyFabOptions(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFabOptions(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FabOptions, defStyleAttr, 0);
        mFabColorResId = a.getResourceId(R.styleable.FabOptions_fab_color, INVALID_ID);
        mBackgroundColorResId = a.getResourceId(R.styleable.FabOptions_background_color, INVALID_ID);
        a.recycle();
        applyFabColor();
        applyBackgroundColor();
    }

    private void applyFabColor() {
        mFabColorResId = SkinCompatHelper.checkResourceId(mFabColorResId);
        if (mFabColorResId != INVALID_ID) {
            setFabColor(mFabColorResId);
        }
    }

    private void applyBackgroundColor() {
        mBackgroundColorResId = SkinCompatHelper.checkResourceId(mBackgroundColorResId);
        if (mFabColorResId != INVALID_ID) {
            setBackgroundColor(getContext(), SkinCompatResources.getInstance().getColor(mBackgroundColorResId));
        }
    }

    @Override
    public void applySkin() {
        applyFabColor();
        applyBackgroundColor();
    }

    @Override
    public void setFabColor(int fabColor) {
        Context context = getContext();
        if (context != null) {
            try {
                FloatingActionButton myFab = findViewById(R.id.faboptions_fab);
                myFab.setBackgroundTintList(ColorStateList.valueOf(SkinCompatResources.getInstance().getColor(fabColor)));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void setBackgroundColor(Context context, int backgroundColor) {
        Drawable backgroundShape = ContextCompat.getDrawable(context, R.drawable.faboptions_background);
        if (backgroundShape != null) {
            backgroundShape.setColorFilter(backgroundColor, PorterDuff.Mode.ADD);
        }

        View myBackground = findViewById(R.id.faboptions_background);
        myBackground.setBackground(backgroundShape);
    }

}
