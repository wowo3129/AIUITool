package com.iflytek.aiui.demo.chat.repository.translation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.AIUIWrapper;
import com.iflytek.aiui.demo.chat.repository.chat.ChatRepo;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfig;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;
import com.iflytek.aiui.demo.chat.utils.SemanticUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.FromType.AIUI;
import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.MsgType.TEXT;

/**
 * 翻译处理
 */
@Singleton
public class Translation {
    private AIUIWrapper mAIUI;
    private ChatRepo mChatRepo;
    private AIUIConfigCenter mConfigManager;
    private AIUIConfig mConfig;

    private TranslationMode mTranslationMode;
    private MutableLiveData<TranslationMode> mLiveTranslationMode = new MutableLiveData<>();
    private MutableLiveData<Boolean> mTranslationEnable = new MutableLiveData<>();

    @Inject
    public Translation(AIUIWrapper wrapper, ChatRepo chatRepo, AIUIConfigCenter configManager) {
        mAIUI = wrapper;
        mChatRepo = chatRepo;
        mConfigManager = configManager;

        mTranslationMode = TranslationMode.getDefaultTranslationMode();
        mLiveTranslationMode.setValue(mTranslationMode);

        mConfigManager.getConfig().observeForever(config -> {
            mConfig = config;
            if (config.isTranslationEnable()) {
                turnOnTranMode();
            } else {
                turnOffTransMode();
            }
        });

        mAIUI.getLiveAIUIEvent().observeForever((event) -> {
            switch (event.eventType) {
                case AIUIConstant.EVENT_RESULT: {
                    processResult(event);
                }
                break;
            }
        });
    }

    /**
     * 关闭翻译模式
     */
    private void turnOffTransMode() {
        //关闭翻译模式，恢复默认参数
        JSONObject restoreParams = mTranslationMode.getRestoreParams(mConfig);
        sendMessage(new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, restoreParams.toString(), null));
        mConfigManager.mergeConfig(restoreParams);
    }

    /**
     * 打开翻译模式
     */
    private void turnOnTranMode() {
        setTransMode(mTranslationMode);
        mConfigManager.mergeConfig(mTranslationMode.getTransParams(mConfig));
    }

    /**
     * 设置翻译模式
     *
     * @param translationMode
     */
    private void setTransMode(TranslationMode translationMode) {
        sendMessage(new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, translationMode.getTransParams(mConfig).toString(), null));

        mTranslationMode = translationMode;
        mLiveTranslationMode.postValue(mTranslationMode);
        mConfigManager.mergeConfig(mTranslationMode.getTransParams(mConfig));
    }

    /**
     * 设置源语言
     *
     * @param language
     */
    public void setSrcLanguage(SrcLanguage language) {
        DestLanguage destLanguage = mTranslationMode.getDestLanguage();
        //找出与源语言不相同的目标语言，避免同语言之间的翻译
        while (language.getLanguage().equals(destLanguage.getLanguage())) {
            int next = (Arrays.asList(DestLanguage.values()).indexOf(destLanguage) + 1) % DestLanguage.values().length;
            destLanguage = DestLanguage.values()[next];
        }
        setTransMode(new TranslationMode(language, destLanguage));
    }

    /**
     * 设置目标语言
     *
     * @param language
     */
    public void setDestLanguage(DestLanguage language) {
        SrcLanguage srcLanguage = mTranslationMode.getSrcLanguage();
        //找出与目标语言不相同的源语言，避免同语言之间的翻译
        while (language.getLanguage().equals(srcLanguage.getLanguage())) {
            int next = (Arrays.asList(SrcLanguage.values()).indexOf(srcLanguage) + 1) % SrcLanguage.values().length;
            srcLanguage = SrcLanguage.values()[next];
        }
        setTransMode(new TranslationMode(srcLanguage, language));
    }

    /**
     * 当前的翻译模式
     * @return
     */
    public LiveData<TranslationMode> getTransMode() {
        return mLiveTranslationMode;
    }

    /**
     * 翻译是否开启
     * @return
     */
    public LiveData<Boolean> getLiveTranslationEnable() {
        return mConfigManager.isTransEnable();
    }

    /**
     * 向SDK发送AIUI消息
     * @param message
     */
    private void sendMessage(AIUIMessage message) {
        mAIUI.sendMessage(message);
    }

    /**
     * 处理AIUI结果事件（翻译结果）
     * @param event 结果事件
     */
    private void processResult(AIUIEvent event) {
        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);

            long rspTime = event.data.getLong("eos_rslt", -1);  //响应时间
            String sub = params.optString("sub");
            if (content.has("cnt_id") && !sub.equals("tts")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                 if("itrans".equals(sub)) {
                    String sid = event.data.getString("sid", "");
                    processTranslationResult(sid, params, cntJson, rspTime);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析处理翻译结果
     * @param sid
     * @param params
     * @param cntJson
     * @param rspTime
     */
    private void processTranslationResult(String sid, JSONObject params, JSONObject cntJson, long rspTime){
        String text = "";
        try{
            cntJson.put("sid", sid);

            JSONObject transResult = cntJson.optJSONObject("trans_result");
            if(transResult != null && transResult.length() != 0){
                text = transResult.optString("dst");
            }

            if(TextUtils.isEmpty(text)){
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("trans_data", cntJson.toString());

            String fakeSemanticResult = SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_TRANS, text, null, data);
            mChatRepo.addMessage(new RawMessage(
                    AIUI, TEXT,fakeSemanticResult.getBytes(),  null, rspTime
            ));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
