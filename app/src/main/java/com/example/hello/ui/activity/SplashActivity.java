/*
 * ************************************************************
 * 文件：SplashActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/12 15:26:58
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.hello.R;
import com.example.hello.service.KeepLiveService;

public class SplashActivity extends BaseActivity {
    Button mSpJumpBtn;
    String skip;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.TranslucentTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.splash_pic).startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash));

        skip = getString(R.string.skip);
        mSpJumpBtn = findViewById(R.id.sp_jump_btn);
        setCountDownTimer();

        verifyStoragePermissions(this);
    }

    private void gotoConnectingActivity() {
        //跳转
        countDownTimer.cancel();
        Intent intent = new Intent(this, ConnectingActivity.class);
        startActivity(intent);
        finish();
    }

    private void setCountDownTimer() {
        countDownTimer = new CountDownTimer(5400, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                mSpJumpBtn.setText(skip + "(" + millisUntilFinished / 1000 + "s)");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                mSpJumpBtn.setText(skip + "(0s)");
                gotoConnectingActivity();
            }
        };
        countDownTimer.start();
        mSpJumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpJumpBtn.setEnabled(false);
                countDownTimer.onFinish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (KeepLiveService.isStop()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, KeepLiveService.class));
            } else {
                startService(new Intent(this, KeepLiveService.class));
            }
        }
    }

    protected void verifyStoragePermissions(Activity activity) {
        String[] PERMISSIONS = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int REQUEST_EXTERNAL_STORAGE = 1;
            ActivityCompat.requestPermissions(activity, PERMISSIONS,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
