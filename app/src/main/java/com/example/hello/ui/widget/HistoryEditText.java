/*
 * ************************************************************
 * 文件：DropEditText.java  模块：app  项目：hello
 * 当前修改时间：2019/11/23 17:10:13
 * 上次修改时间：2019/11/23 17:10:12
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.ListPopupWindow;

import com.example.hello.R;
import com.example.hello.util.ToastUtils;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;

public class HistoryEditText extends ClearEditText implements SkinCompatSupportable {

    private ListPopupWindow listPopupWindow;
    private String          historyFile = "history.txt";
    private List<String>    h;
    private String          clearText;

    public void setHistoryFile(String historyFile) {
        if (historyFile != null && historyFile.length() > 0) {
            this.historyFile = historyFile;
            init();
        }
    }

    public HistoryEditText(Context context) {
        this(context, null);
    }

    public HistoryEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HistoryEditText);
        clearText = a.getString(R.styleable.HistoryEditText_clear_text);
        a.recycle();
        init();
    }

    public HistoryEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HistoryEditText);
        clearText = a.getString(R.styleable.HistoryEditText_clear_text);
        a.recycle();
        init();
    }

    private void init() {
        if (listPopupWindow == null) {
            listPopupWindow = new ListPopupWindow(getContext());
            listPopupWindow.setAnchorView(this);//设置listpopowindow和哪个控件相邻
//        listPopupWindow.setModal(true);//指定listpopwindow是否阻止在显示的时候将内容输入其他窗口

            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listPopupWindow.dismiss();//如果已经选择，隐藏listpopwindow
                    if (position == h.size() - 1) {
                        clearHistory();
                    } else {
                        setText(h.get(position));
                        //光标移到末尾
                        Editable editable = getText();
                        if (editable != null) {
                            Selection.setSelection(editable, editable.length());
                        }
                    }
                }
            });
        }
        h = getHistory();
        if (h.size() != 0) {
            h.add(clearText == null ? "Clear All" : clearText);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_history, R.id.history, h);
        listPopupWindow.setAdapter(adapter);//设置适配器
        listPopupWindow.setBackgroundDrawable(SkinCompatResources.getInstance().getDrawable(R.drawable.infobox_bg));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        listPopupWindow.show();
        return super.onTouchEvent(event);
    }

    private List<String> getHistory() {
        List<String> h = new ArrayList<>();
        try {
            FileInputStream fin = getContext().openFileInput(historyFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = bf.readLine()) != null) {
                h.add(line);
            }
            bf.close();
            fin.close();
        } catch (IOException e) {
            return h;
        }
        return h;
    }

    public void addToHistory() {
        String line = Objects.requireNonNull(getText()).toString();
        try {
            if (h.contains(line)) {
                return;
            }
            FileOutputStream fou = getContext().openFileOutput(historyFile, Context.MODE_APPEND);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fou));
            bw.write(line + "\r\n");
            bw.close();
            fou.close();
        } catch (IOException e) {
            ToastUtils.toastLong(getContext(), e.getMessage(), TastyToast.ERROR);
        }
        init();
    }

    private void clearHistory() {
        getContext().deleteFile(historyFile);
        init();
    }

    @Override
    public void applySkin() {
        listPopupWindow.setBackgroundDrawable(SkinCompatResources.getInstance().getDrawable(R.drawable.infobox_bg));
    }

}
