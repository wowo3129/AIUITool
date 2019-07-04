package com.iflytek.aiui.demo.chat.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.iflytek.aiui.demo.chat.common.Constant;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserPreference {
    public static final Preference defaultPreference;

    static {
        defaultPreference = new Preference();
        defaultPreference.wakeup =  false;
        defaultPreference.translation =  false;
        defaultPreference.translationScene =  "trans";
        defaultPreference.eos =  1000;
        defaultPreference.debugLog =  true;
        defaultPreference.saveDebugLog =  false;
        defaultPreference.appid =  "";
        defaultPreference.key =  "";
        defaultPreference.scene =  "main";
        defaultPreference.tts =  true;
        defaultPreference.saveAIUILog =  false;
        defaultPreference.accent =  "mandarin";
    }

    private final SharedPreferences mDefaultSharePreference;

    @Inject
    public UserPreference(Context context) {
        mDefaultSharePreference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public UserPreference(SharedPreferences sharedPreferences) {
        mDefaultSharePreference = sharedPreferences;
    }

    public void setWakeupEnable(boolean wakeupEnable) {
        putBoolean(Constant.KEY_AIUI_WAKEUP, wakeupEnable);
    }


    public void setTtsEnable(boolean ttsEnable) {
        putBoolean(Constant.KEY_AIUI_TTS, ttsEnable);
    }


    public void setDebugLogEnable(boolean debugLogEnable) {
        putBoolean(Constant.KEY_AIUI_DEBUG_LOG, debugLogEnable);
    }


    public void setSaveDataLogEnable(boolean saveDebugLogEnable) {
        putBoolean(Constant.KEY_AIUI_SAVE_DATALOG, saveDebugLogEnable);
    }

    public void setEos(int eos) {
        putString(Constant.KEY_AIUI_EOS, String.valueOf(eos));
    }

    public void setAppID(String appID) {
        putString(Constant.KEY_APPID, appID);
    }


    public void setKey(String key) {
        putString(Constant.KEY_APP_KEY, key);
    }


    public void setScene(String scene) {
        putString(Constant.KEY_SCENE, scene);
    }


    public void setAccent(String accent) {
        putString(Constant.KEY_ACCENT, accent);
    }


    public void setTranslationEnable(boolean translationEnable) {
        putBoolean(Constant.KEY_AIUI_TRANSLATION, translationEnable);
    }


    public void setTranslationScene(String translationScene) {
        putString(Constant.KEY_TRANS_SCENE, translationScene);
    }

    public Boolean isConfigInitialized() {
        return mDefaultSharePreference.getBoolean(Constant.CONFIG_INIT_FLAG, false);
    }

    public void setConfigInitialized(boolean initialized) {
        putBoolean(Constant.CONFIG_INIT_FLAG, initialized);
    }

    public String getLastAssetConfig() {
        return mDefaultSharePreference.getString(Constant.KEY_CONFIG_LAST_ASSET_CONFIG, "");
    }

    public void saveLastAssetConfig(String lastAssetConfig) {
        putString(Constant.KEY_CONFIG_LAST_ASSET_CONFIG, lastAssetConfig);
    }

    /**
     * 提取用户设置
     * @return
     */
    public UserPreference.Preference currentPreference() {
        UserPreference.Preference preference = new UserPreference.Preference();
        preference.wakeup = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_WAKEUP, defaultPreference.wakeup);
        preference.translation = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_TRANSLATION, defaultPreference.translation);
        preference.translationScene = mDefaultSharePreference.getString(Constant.KEY_TRANS_SCENE, defaultPreference.translationScene);
        preference.eos = Integer.valueOf(mDefaultSharePreference.getString(Constant.KEY_AIUI_EOS, String.valueOf(defaultPreference.eos)));
        preference.debugLog = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_DEBUG_LOG, defaultPreference.debugLog);
        preference.saveDebugLog = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_SAVE_DATALOG, defaultPreference.saveDebugLog);
        preference.appid = mDefaultSharePreference.getString(Constant.KEY_APPID, defaultPreference.appid);
        preference.key = mDefaultSharePreference.getString(Constant.KEY_APP_KEY, defaultPreference.key);
        preference.scene = mDefaultSharePreference.getString(Constant.KEY_SCENE, defaultPreference.scene);
        preference.tts = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_TTS, defaultPreference.tts);
        preference.saveAIUILog = mDefaultSharePreference.getBoolean(Constant.KEY_AIUI_LOG, defaultPreference.saveAIUILog);
        preference.accent = mDefaultSharePreference.getString(Constant.KEY_ACCENT, defaultPreference.accent);
        return preference;
    }

    private void putString(String key, String value) {
        SharedPreferences.Editor editor = mDefaultSharePreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mDefaultSharePreference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    /**
     * 设置
     */

    public static class Preference {
        public boolean wakeup;
        public boolean tts;
        public boolean saveAIUILog;

        public int eos;

        public boolean debugLog;
        public boolean saveDebugLog;

        public String appid;
        public String key;
        public String scene;

        public String accent;

        public boolean translation;
        public String translationScene;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Preference) {
                Preference other = (Preference) obj;
                return other.wakeup == wakeup &&
                        other.tts == tts &&
                        other.translation == translation &&
                        other.saveAIUILog == saveAIUILog &&
                        other.debugLog == debugLog &&
                        other.saveDebugLog == saveDebugLog &&
                        other.eos == eos &&
                        Objects.equals(other.appid, appid) &&
                        Objects.equals(other.key, key) &&
                        Objects.equals(other.scene, scene) &&
                        Objects.equals(other.accent, accent) &&
                        Objects.equals(other.translationScene, translationScene);

            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(wakeup, tts, translation, saveAIUILog, debugLog, saveDebugLog, eos,
                    appid, key, scene, accent, translationScene);
        }
    }
}
