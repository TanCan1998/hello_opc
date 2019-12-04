/*
 * ************************************************************
 * 文件：SettingsActivity.java  模块：app  项目：hello
 * 当前修改时间：2019/11/22 16:22:47
 * 上次修改时间：2019/11/22 15:45:31
 * 作者：Diesel
 * Copyright (c) 2019
 * ************************************************************
 */

package com.example.hello.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.hello.R;
import com.example.hello.util.LogUtils;
import com.example.hello.util.ToastUtils;

import java.util.Locale;
import java.util.Objects;

import skin.support.SkinCompatManager;
import skin.support.utils.SkinPreference;


public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("asdf", "setting oncreate");
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onStart() {
            super.onStart();
            if (getPreferenceManager().getSharedPreferences().getBoolean("theme_night", false)) {
                Objects.requireNonNull(findPreference("theme_night")).setIcon(R.drawable.ic_moon);
            }
            setThemeIcon();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("mysetting");
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Objects.requireNonNull(findPreference("theme_night")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(true)) {
                        preference.setIcon(R.drawable.ic_moon);
                        ToastUtils.toasty(Objects.requireNonNull(getContext()), "夜间主题开启", Objects.requireNonNull(getActivity()).getDrawable(R.drawable.ic_mode_night), getResources().getColor(R.color.nodeBackground_night), getResources().getColor(R.color.yellow));
                        SkinCompatManager.getInstance()
                                .loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                    } else {
                        preference.setIcon(R.drawable.ic_sun);
                        ToastUtils.toasty(Objects.requireNonNull(getContext()), "日间主题开启", Objects.requireNonNull(getActivity()).getDrawable(R.drawable.ic_mode_day), getResources().getColor(R.color.nodeBackground), getResources().getColor(R.color.yellow));
                        String skin = getSkinName(1);
                        if (skin.equals("default")) {
                            SkinCompatManager.getInstance().restoreDefaultTheme();
                        } else {
                            SkinCompatManager.getInstance()
                                    .loadSkin(skin, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                        }
                    }
                    setThemeIcon();
                    return true;
                }
            });
            Objects.requireNonNull(findPreference("theme_color")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, final Object newValue) {
                    if (!newValue.equals("default")) {
                        SkinCompatManager.getInstance()
                                .loadSkin(newValue.toString(), SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                    } else {
                        SkinCompatManager.getInstance().restoreDefaultTheme();
                    }
                    setThemeIcon();
                    return true;
                }
            });
            EditTextPreference ep = findPreference("locale");
            assert ep != null;
            ep.setPersistent(false);
            Locale locale = getResources().getConfiguration().locale;
            ep.setTitle(locale.getDisplayLanguage() + (locale.getDisplayLanguage().equals("中文")?"\b\b\uD83C\uDDE8\uD83C\uDDF3":""));
            ep.setText("("+locale.getLanguage()+")");
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            ep.setIntent(intent);
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (!(preference instanceof EditTextPreference)) {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        private String getSkinName(int mode) {
            if (mode == 1) {
                return getPreferenceManager().getSharedPreferences()
                        .getString("theme_color", "default");
            } else {
                return SkinPreference.getInstance().getSkinName();
            }
        }

        private void setThemeIcon() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int icon;
                    switch (getSkinName(1)) {
                        case "pink":
                            icon = R.drawable.ic_theme_pink;
                            break;
                        case "yellow":
                            icon = R.drawable.ic_theme_yellow;
                            break;
                        case "green":
                            icon = R.drawable.ic_theme_green;
                            break;
                        case "purple":
                            icon = R.drawable.ic_theme_purple;
                            break;
                        default:
                            icon = R.drawable.ic_theme;
                            break;
                    }
                    if (getSkinName(2).equals("night")) {
                        icon = R.drawable.ic_theme_night;
                    }
                    ListPreference preference = findPreference("theme_color");
                    assert preference != null;
                    preference.setIcon(icon);
                    preference.setDialogIcon(icon);
                }
            }, 50);
        }
    }
}