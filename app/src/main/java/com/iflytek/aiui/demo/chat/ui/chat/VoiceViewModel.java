package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.repository.VoiceManager;


import javax.inject.Inject;

public class VoiceViewModel extends ViewModel {
    private VoiceManager mVoiceManager;

    @Inject
    public VoiceViewModel(VoiceManager voiceManager) {
        mVoiceManager = voiceManager;
    }

    /**
     * 按住开始说话
     */
    public void startSpeak() {
        mVoiceManager.startSpeak();
    }

    /**
     * 松开停止
     */
    public void endSpeak() {
        mVoiceManager.endSpeak();
    }

    /**
     * 文字交互
     */
    public void sendText(String msg) {
        mVoiceManager.writeText(msg);
    }

    public void onChatResume() {
        mVoiceManager.onResume();
    }

    public void onChatPause() {
        mVoiceManager.onPause();
    }

    /**
     * 唤醒是否启用
     * @return
     */
    public LiveData<Boolean> isWakeUpEnable() {
        return mVoiceManager.isWakeUpEnable();
    }

    /**
     * 唤醒休眠状态消息
     * @return
     */
    public LiveData<Boolean> wakeUp() {
        return mVoiceManager.wakeUp();
    }

    /**
     * 音量信息
     * @return
     */
    public LiveData<Integer> volume() {
        return mVoiceManager.volume();
    }

    /**
     * 此次交互是否有效
     * @return
     */
    public LiveData<Boolean> isActiveInteract() {
        return mVoiceManager.isActiveInteract();
    }

}
