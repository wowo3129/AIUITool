package com.iflytek.aiui.demo.chat.ui.settings;

import javax.inject.Inject;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.common.Constant;

import dagger.android.support.AndroidSupportInjection;

/**
 * 设置界面Fragment
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private SettingViewModel mSettingModel;
    private EditTextPreference eosPreference;
    private ListPreference dialectPreference;
    private SwitchPreferenceCompat wakeupPreference;
    private SwitchPreferenceCompat transPreference;

    private Context mContext;
    private AlertDialog mTransDialog;
    private AlertDialog mClearLogDialog;
    private View mTransDialogView;

    private String mTranslationScene;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        mTranslationScene = getPreferenceManager().getSharedPreferences().getString(Constant.KEY_TRANS_SCENE, "trans");
        eosPreference = (EditTextPreference) (getPreferenceManager().findPreference(Constant.KEY_AIUI_EOS));
        eosPreference.setSummary(String.format("%sms", getPreferenceManager().getSharedPreferences().getString(Constant.KEY_AIUI_EOS, "1000")));
        wakeupPreference = (SwitchPreferenceCompat) getPreferenceManager().findPreference(Constant.KEY_AIUI_WAKEUP);
        transPreference = (SwitchPreferenceCompat) getPreferenceManager().findPreference(Constant.KEY_AIUI_TRANSLATION);
        dialectPreference = (ListPreference) getPreferenceManager().findPreference(Constant.KEY_ACCENT);

        String dialectCode = getPreferenceManager().getSharedPreferences().getString(Constant.KEY_ACCENT, "mandarin");
        setDialectSummary(dialectCode);

        if(wakeupPreference.isChecked()) {
            eosPreference.setEnabled(true);
        } else {
            eosPreference.setEnabled(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSettingModel = ViewModelProviders.of(this, mViewModelFactory).get(SettingViewModel.class);
        //根据唤醒是否可用决定设置界面的唤醒enable或者disable
        mSettingModel.isWakeUpAvailable().observe(this, enable -> wakeupPreference.setEnabled(enable));
        createTransSceneDialog();
        createClearAIUILogDialog();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("clear_aiui_log")) {
            mClearLogDialog.show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //监听后端点的设置变化
        if (Constant.KEY_AIUI_EOS.equals(s)) {
            String eos = sharedPreferences.getString(s, "1000");
            //判断设置值的合法性
            if (!isNumeric(eos)) {
                eosPreference.setText("1000");
                Snackbar.make(getView(), getString(R.string.eos_invalid_tip), Snackbar.LENGTH_LONG).show();
            } else {
                //根据新设置的后端点值更新设置项的summary展示
                eosPreference.setSummary(String.format("%sms", eos));
            }
        }

        //监听后端点的设置变化
        if (Constant.KEY_ACCENT.equals(s)) {
            String dialectCode = sharedPreferences.getString(s, "mandarin");
            setDialectSummary(dialectCode);
        }

        if (Constant.KEY_AIUI_TRANSLATION.equals(s)) {
            boolean openTrans = sharedPreferences.getBoolean(s, false);
            if (openTrans) {
                showTransSceneDialog();
            }
        }

        if(Constant.KEY_AIUI_WAKEUP.equals(s)) {
            boolean wakeUpEnable = sharedPreferences.getBoolean(s, false);
            if(wakeUpEnable) {
                eosPreference.setEnabled(true);
            } else {
                eosPreference.setEnabled(false);
            }
        }

    }

    private void setDialectSummary(String dialect_code) {
        String dialect = "普通话";
        switch (dialect_code) {
            case "mandarin": {
                dialect = "普通话";
            }
            break;

            case "lmz": {
                dialect = "四川话";
            }
            break;

            case "cantonese": {
                dialect = "粤语";
            }
            break;
        }
        dialectPreference.setSummary(dialect);
    }


    private boolean isNumeric(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        //设置界面退出时同步配置通知其他监听者
        mSettingModel.syncLastSetting();
        super.onStop();
    }

    private void createTransSceneDialog() {
        mTransDialogView = getLayoutInflater().inflate(R.layout.trans_scene_preference, null);
        mTransDialog = new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.trans_scene_tip))
                .setView(mTransDialogView)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancle), (dialog, which) -> {
                    transPreference.setChecked(false);
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.ok), null)
                .create();
        ((EditText) mTransDialogView.findViewById(R.id.trans_input)).setText(mTranslationScene);
    }

    private void createClearAIUILogDialog() {
        mClearLogDialog = new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.clear_aiui_log_title))
                .setNegativeButton(getString(R.string.clear_aiui_log_no), null)
                .setPositiveButton(getString(R.string.clear_aiui_log_yes), (dialogInterface, i) -> {
                    boolean deleted = mSettingModel.deleteAIUILog();
                    Snackbar.make(getView(), deleted ? getString(R.string.already_clear_aiui_log) :
                            getString(R.string.clear_aiui_log_fail), Snackbar.LENGTH_SHORT).show();
                })
                .create();
    }

    private void showTransSceneDialog() {
        if (mTransDialog != null) {
            mTransDialog.show();
            mTransDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String scene = ((EditText) mTransDialogView.findViewById(R.id.trans_input)).getText().toString().trim();
                if (!TextUtils.isEmpty(scene)) {
                    saveTranslationScene(scene);
                    mTransDialog.dismiss();
                } else {
                    Snackbar.make(getView(), R.string.trans_invalid_tip, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveTranslationScene(String scene) {
        mTranslationScene = scene;

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.KEY_TRANS_SCENE, scene);
        editor.apply();
    }

}
