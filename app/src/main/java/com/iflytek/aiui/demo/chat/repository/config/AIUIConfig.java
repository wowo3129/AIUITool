package com.iflytek.aiui.demo.chat.repository.config;

import com.iflytek.aiui.demo.chat.repository.UserPreference;

import org.json.JSONObject;

public class AIUIConfig {
    private JSONObject mConfig;
    private UserPreference.Preference mPreference;

   public AIUIConfig(JSONObject config, UserPreference.Preference setting) {
      mConfig = config;
      mPreference = setting;
   }

    public JSONObject runtimeConfig() {
        return mConfig;
    }

    public boolean isWakeUpEnable() {
       return mPreference.wakeup;
    }

    public boolean isTranslationEnable() {
        return mPreference.translation;
    }

    public String scene() {
        return mPreference.scene;
    }

    public String accent() {
        return mPreference.accent;
    }

    public String translationScene() {
        return mPreference.translationScene;
    }
}
