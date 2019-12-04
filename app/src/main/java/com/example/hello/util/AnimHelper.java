/*
 * ************************************************************
 * 文件：AnimHelper.java  模块：app  项目：hello
 * 当前修改时间：2019/12/04 12:10:44
 * 上次修改时间：2019/12/04 12:10:44
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.animation.Animator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;

public class AnimHelper {
    private Animator animator;

    public static AnimHelper getInstance() {
        return new AnimHelper();
    }

    public AnimHelper getAnim(View show, MotionEvent event, View fade) {//参考网上的圆形画图代码
        // get the center for the clipping circle
        int cx;
        int cy;
        if (event == null) {
//            cx = (show.getLeft() + show.getRight()) / 2;
//            cy = (show.getTop() + show.getBottom()) / 2;
            cx = 450;
            cy = 200;
        } else {
            cx = (int) event.getX();
            cy = (int) event.getY();
        }

        // get the final radius for the clipping circle
        int dx = Math.max(cx, show.getWidth() - cx);
        int dy = Math.max(cy, show.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        // Android native animator
        if (fade == null) {
            animator = ViewAnimationUtils.createCircularReveal(show, cx, cy, 0, finalRadius);
            animator.setDuration(800);
        } else {
            animator = ViewAnimationUtils.createCircularReveal(fade, dx, dy, 1000, 0);
            animator.setDuration(350);
        }
        return this;
    }

    public AnimHelper setDuration(long duration) {
        animator.setDuration(duration);
        return this;
    }

    public void startAnimation() {
        if (animator != null) {
            animator.start();
        }
    }

}
