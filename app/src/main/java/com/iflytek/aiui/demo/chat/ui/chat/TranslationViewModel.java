package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iflytek.aiui.demo.chat.repository.translation.DestLanguage;
import com.iflytek.aiui.demo.chat.repository.translation.SrcLanguage;
import com.iflytek.aiui.demo.chat.repository.translation.TranslationMode;
import com.iflytek.aiui.demo.chat.repository.translation.Translation;


import javax.inject.Inject;


public class TranslationViewModel extends ViewModel {
    private Translation mTranslationRepo;

    @Inject
    public TranslationViewModel(Translation translationRepo) {
        mTranslationRepo = translationRepo;
    }

    public LiveData<Boolean> isTranslationEnable() {
        return mTranslationRepo.getLiveTranslationEnable();
    }

    public LiveData<TranslationMode> getTranslationMode() {
        return mTranslationRepo.getTransMode();
    }

    public void setSrcLanguage(SrcLanguage language) {
        mTranslationRepo.setSrcLanguage(language);
    }

    public void setDestLanguage(DestLanguage language) {
        mTranslationRepo.setDestLanguage(language);
    }
}
