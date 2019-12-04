/*
 * ************************************************************
 * 文件：CrashHandler.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:19:33
 * 上次修改时间：2019/11/22 15:45:31
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;

import com.example.hello.BuildConfig;
import com.example.hello.R;
import com.sdsmdg.tastytoast.TastyToast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * <p>
 * 需要在Application中注册，为了要在程序启动器就监控整个程序。
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private                 Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    @SuppressLint("StaticFieldLeak")
    private volatile static CrashHandler                    instance;
    //程序的Context对象
    private                 Context                         mContext;
    //用来存储设备信息和异常信息
    private                 Map<String, String>             infos = new HashMap<>();

    //用于格式化日期,作为日志文件名的一部分
    @SuppressLint("SimpleDateFormat")
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "error : ", e);
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            ActivityContainer.getInstance().finishAllActivity();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex：Throwable.
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (BuildConfig.DEBUG) {
            ToastUtils.toastLong(mContext, ex.getMessage(), TastyToast.ERROR);
        }
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        final String file = saveCatchInfo2File(ex);

        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtils.toastLong(mContext, mContext.getString(R.string.UncaughtException) + (file != null ? file : ""), TastyToast.ERROR);
                Looper.loop();
            }
        }.start();
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx：Context.
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), Objects.requireNonNull(field.get(null)).toString());
                LogUtils.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                LogUtils.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex：Throwable.
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCatchInfo2File(Throwable ex) {

        if (!verifyStoragePermissions(mContext)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        mContext.getPackageName(), 0);
                String filePath = Environment.getExternalStorageDirectory() + "/" + packageInfo.packageName + "/";
                File file = new File(filePath, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                //发送给开发人员
//                sendCrashLog2PM(path + fileName);
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            LogUtils.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }


    private boolean verifyStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (String str : permissions) {
                if (context.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

//    /**
//     * 将捕获的导致崩溃的错误信息发送给开发人员
//     * <p>
//     * 目前只将log日志保存在sdcard 和输出到LogCat中，并未发送给后台。
//     */
//    private void sendCrashLog2PM(String fileName) {
//        LogUtils.e(TAG, "sendCrashLog2PM: 路径：=-=" + fileName);
//        if (!new File(fileName).exists()) {
//            Toast.makeText(mContext, "日志文件不存在！", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        FileInputStream fis = null;
//        BufferedReader reader = null;
//        String s;
//        try {
//            fis = new FileInputStream(fileName);
//            reader = new BufferedReader(new InputStreamReader(fis, "GBK"));
//            while (true) {
//                s = reader.readLine();
//                if (s == null)
//                    break;
//                //由于目前尚未确定以何种方式发送，所以先打出log日志。
//                LogUtils.i("info", s);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {   // 关闭流
//            try {
//                assert reader != null;
//                reader.close();
//                fis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}