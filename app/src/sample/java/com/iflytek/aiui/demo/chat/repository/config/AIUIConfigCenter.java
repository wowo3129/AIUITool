package com.iflytek.aiui.demo.chat.repository.config;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Environment;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUISetting;
import com.iflytek.aiui.demo.chat.BuildConfig;
import com.iflytek.aiui.demo.chat.repository.UserPreference;
import com.iflytek.aiui.demo.chat.repository.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * AIUI配置管理
 *
 * 原始配置文件 assets/cfg/aiui_phone.cfg
 *
 *
 */
@Singleton
public class AIUIConfigCenter {
    static final String ASSETS_CONFIG_PATH = "cfg/aiui_phone.cfg";

    private UserPreference mUserPreference;
    private JSONObject mAIUIConfig;
    private MutableLiveData<AIUIConfig> mLiveAIUIConfig = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveWakeUpAvailable = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveWakeUpEnable = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveTransEnable = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveTTSEnable = new MutableLiveData<>();

    private UserPreference.Preference mSetting;
    private Storage mStorage;

    @Inject
    public AIUIConfigCenter(Storage storage, UserPreference userPreference) {
        mStorage = storage;
        mUserPreference = userPreference;

        try {
            String currentAssetConfig = mStorage.readAssetFile(ASSETS_CONFIG_PATH);
            mAIUIConfig = new JSONObject(currentAssetConfig);

            syncAIUIConfig();

        } catch (JSONException e) {
            Timber.e(e, "read assets config failed, please check!");
        }
    }



    public LiveData<AIUIConfig> getConfig() {
       return mLiveAIUIConfig;
    }

    public LiveData<Boolean> isWakeUpAvailable() {
        return mLiveWakeUpAvailable;
    }

    public LiveData<Boolean> isWakeUpEnable() {
        return mLiveWakeUpEnable;
    }

    public LiveData<Boolean> isTransEnable() {
        return mLiveTransEnable;
    }

    public LiveData<Boolean> isTTSEnable() {
        return mLiveTTSEnable;
    }

    /**
     * 设置appid
     * @param appid
     * @param key
     * @param scene
     */
    public void config(String appid, String key, String scene) {
        //未实现
    }

    /**
     * 配置合并
     * @param config
     */
    public void mergeConfig(JSONObject config) {
        merge(config, mAIUIConfig);
    }


    /**
     *  当前配置结合Settings中的设置选项，重新生成AIUI配置
     */
    public void syncAIUIConfig()  {
        UserPreference.Preference preference = retrieveCurrentSettings();
        if(preference.equals(mSetting)) return;

        mSetting = preference;
        //设置后可在/sdcard/msc/下生成aiui.log日志
        if(mSetting.saveAIUILog){
            AIUISetting.setAIUIDir(Environment.getExternalStorageDirectory() + "/AIUIMEETING/");
            AIUISetting.setDataLogDir(Environment.getExternalStorageDirectory() + "/AIUIMEETINGDATA/");
            AIUISetting.setNetLogLevel(AIUISetting.LogLevel.info);
        }else {
            AIUISetting.setNetLogLevel(AIUISetting.LogLevel.none);
        }

        mLiveWakeUpEnable.postValue(mSetting.wakeup);
        mLiveTransEnable.postValue(mSetting.translation);
        mLiveTTSEnable.postValue(mSetting.tts);
        try {
            mAIUIConfig.optJSONObject("log").put("debug_log", preference.debugLog?"1":"0");
            mAIUIConfig.optJSONObject("log").put("save_datalog", preference.saveDebugLog?"1":"0");
            mAIUIConfig.optJSONObject("log").put("datalog_path", preference.saveDebugLog?"1":"0");
            if(preference.wakeup){
                //唤醒配置
                mAIUIConfig.optJSONObject("speech").put("wakeup_mode", "ivw");
                mAIUIConfig.optJSONObject("vad").put(AIUIConstant.KEY_VAD_EOS, String.valueOf(preference.eos));

                if(!mAIUIConfig.has("ivw")) {
                    JSONObject ivw = new JSONObject();
                    ivw.put("res_path", "ivw/ivw.jet");
                    ivw.put("res_type", "assets");
                    ivw.put("ivw_threshold", "0:2000");
                    mAIUIConfig.put("ivw", ivw);
                }
            } else {
                mAIUIConfig.optJSONObject("speech").put("wakeup_mode", "off");
                mAIUIConfig.optJSONObject("vad").put("vad_eos", "60000");
            }

            mAIUIConfig.optJSONObject("tts").put("play_mode", preference.tts ? "sdk" : "user");
        } catch (JSONException e) {
            Timber.e(e, "merge setting with raw config failed");
        }

        mLiveAIUIConfig.postValue(new AIUIConfig(mAIUIConfig, preference));
    }

    /**
     * 获取当前有效配置
     * @return
     */
    private UserPreference.Preference retrieveCurrentSettings() {
        mLiveWakeUpAvailable.postValue(BuildConfig.WAKEUP_ENABLE);

        UserPreference.Preference preference = mUserPreference.currentPreference();
        return preference;
    }


    private void merge(JSONObject from, JSONObject to) {
        try {
            Iterator<String> keys = from.keys();
            Object obj1, obj2;
            while (keys.hasNext()) {
                String next = keys.next();
                if (from.isNull(next)) continue;
                obj1 = from.get(next);
                if (!to.has(next)) to.putOpt(next, obj1);
                obj2 = to.get(next);
                if (obj1 instanceof JSONObject && obj2 instanceof JSONObject) {
                    merge((JSONObject) obj1, (JSONObject) obj2);
                } else {
                    to.putOpt(next, obj1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

