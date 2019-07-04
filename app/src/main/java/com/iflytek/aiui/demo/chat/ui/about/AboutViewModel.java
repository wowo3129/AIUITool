package com.iflytek.aiui.demo.chat.ui.about;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.repository.AIUIWrapper;

import javax.inject.Inject;


public class AboutViewModel extends ViewModel {
    private AIUIWrapper mAIUI;

    @Inject
    public AboutViewModel(AIUIWrapper wrapper) {
        mAIUI = wrapper;
    }

    public LiveData<String> getUID() {
        return mAIUI.getLiveUID();
    }
}
