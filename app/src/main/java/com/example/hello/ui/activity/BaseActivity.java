/*
 * ************************************************************
 * 文件：BaseActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/20 15:28:45
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.SkinAppCompatDelegateImpl;
import androidx.appcompat.widget.Toolbar;

import com.example.hello.R;
import com.example.hello.util.ActivityContainer;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

public abstract class BaseActivity extends AppCompatActivity implements SkinCompatSupportable {
    protected      Toolbar  toolbar;
    protected      int      colorPrimary;

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return SkinAppCompatDelegateImpl.get(this, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityContainer.getInstance().addActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public void applySkin() {
        colorPrimary = SkinCompatResources.getInstance().getColor(R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(colorPrimary);
    }
}