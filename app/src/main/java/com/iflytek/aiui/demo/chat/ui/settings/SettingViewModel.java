package com.iflytek.aiui.demo.chat.ui.settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by PR on 2017/12/14.
 */

public class SettingViewModel extends ViewModel {
    private AIUIConfigCenter mConfigManager;
    @Inject
    public SettingViewModel(AIUIConfigCenter configManager) {
        mConfigManager = configManager;
    }

    /**
     * 从preference中同步最新的setting设置
     */
    public void syncLastSetting() {
        mConfigManager.syncAIUIConfig();
    }

    /**
     * 唤醒是否可用
     */
    public LiveData<Boolean> isWakeUpAvailable() {
        return mConfigManager.isWakeUpAvailable();
    }

    public boolean deleteAIUILog(){
        File log = new File(Constant.AIUI_LOG_PATH);
        return !log.exists() || log.delete();
    }
}

