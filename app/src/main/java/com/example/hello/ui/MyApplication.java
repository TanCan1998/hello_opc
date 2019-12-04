package com.example.hello.ui;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.hello.BuildConfig;
import com.example.hello.util.CrashHandler;

import skin.support.SkinCompatManager;
import skin.support.app.SkinAppCompatViewInflater;
import skin.support.constraint.app.SkinConstraintViewInflater;
import skin.support.design.app.SkinMaterialViewInflater;
import skin.support.utils.Slog;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        // 框架换肤日志打印
        Slog.DEBUG = BuildConfig.DEBUG;
        SkinCompatManager.withoutActivity(this)
                .addInflater(new SkinAppCompatViewInflater())   // 基础控件换肤初始化
                .addInflater(new SkinMaterialViewInflater())    // material design
                .addInflater(new SkinConstraintViewInflater())  // ConstraintLayout
//                .setSkinWindowBackgroundEnable(false)           // 关闭windowBackground换肤
//                .setSkinAllActivityEnable(false)                // true: 默认所有的Activity都换肤; false: 只有实现SkinCompatSupportable接口的Activity换肤
                .loadSkin();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //当程序发生Uncaught异常的时候,由该类来接管程序
        CrashHandler.getInstance().init(this);
        sContext = this;
    }

    public static Context context() {
        return sContext;
    }

}