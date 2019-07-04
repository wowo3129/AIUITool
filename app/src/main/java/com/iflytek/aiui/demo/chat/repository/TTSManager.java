package com.iflytek.aiui.demo.chat.repository;

import android.text.TextUtils;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * TTS管理
 *
 * TTS使用语义后合成和主动语音合成结合(二者的介绍可以参见接入文档中语音合成一节的介绍）
 *
 * 主动语义合成主要使用在一些需要本地构造合成内容的情况，比如和本地播放列表相关的上一首，下一首，当前播放的歌曲名等操作
 * 需要结合播放列表的当前播放项构造最终的合成内容。
 *
 * 因为语义后合成的音频是云端根据语义的answer结果生成的，所以判断是否需要本地合成的条件是 最终处理结果的合成内容和下发
 * 的语义结果的answer的值是否相同。如果不同，则需要调用主动语音合成，如果相同则直接播放和语义结果一同下发的语义后合成结果。
 *
 */
@Singleton
public class TTSManager {
    private AIUIWrapper mAIUI;
    private AIUIConfigCenter mConfigCenter;

    private boolean mTTSEnable = false;

    //TTS结束回调
    private Runnable mTTSCallback;

    @Inject
    public TTSManager(AIUIWrapper wrapper, AIUIConfigCenter configCenter) {
        mAIUI = wrapper;
        mConfigCenter = configCenter;

        mConfigCenter.isTTSEnable().observeForever((v) -> mTTSEnable = v);

        mAIUI.getLiveAIUIEvent().observeForever(event -> {
            if (event.eventType == AIUIConstant.EVENT_TTS) {
                switch (event.arg1) {
                    case AIUIConstant.TTS_SPEAK_COMPLETED: {
                        Timber.d("TTS Complete");
                        if (mTTSCallback != null) {
                            mTTSCallback.run();
                            mTTSCallback = null;
                        }
                    }
                    break;
                }
            }
        });
    }


    /**
     * 开始合成
     *
     * @param text     合成文本
     * @param onComplete 合成完成回调
     */
    public void startTTS(String text, Runnable onComplete, boolean resume) {
        if(!resume) {
            sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.PAUSE, 0, null, null));
            Timber.d("start tts %s", text);
        } else {
            Timber.d("resume tts %s", text);
        }

        if(!mTTSEnable || TextUtils.isEmpty(text)) {
            if(onComplete != null) {
                onComplete.run();
            }
            return;
        }

        mTTSCallback = onComplete;
        if(!resume) {
            Timber.d("start TTS %s", text);
            String tag = "@" + System.currentTimeMillis();

            StringBuffer params = new StringBuffer();  //构建合成参数
            params.append("vcn=x_chongchong");  //合成发音人
            params.append(",speed=50");  //合成速度
            params.append(",pitch=50");  //合成音调
            params.append(",volume=50");  //合成音量
            params.append(",ent=x_tts");  //合成音量
            params.append(",tag=" + tag);  //合成tag，方便追踪合成结束，暂未实现

            AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0,
                    params.toString(), text.getBytes());
            sendMessage(startTts);
        }
    }

    /**
     * 暂停合成
     */
    public void pauseTTS() {
        Timber.d("Pause TTS");
        sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.PAUSE, 0, null, null));
    }

    /**
     * 停止合成播放
     */
    public void stopTTS() {
        Timber.d("Stop TTS");
        sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.CANCEL, 0, "", null));
    }


    private void sendMessage(AIUIMessage message) {
        mAIUI.sendMessage(message);
    }

}
