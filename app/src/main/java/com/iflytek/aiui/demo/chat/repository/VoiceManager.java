package com.iflytek.aiui.demo.chat.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.chat.ChatRepo;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;
import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayerWrapper;
import com.iflytek.aiui.demo.chat.ui.common.SingleLiveEvent;
import com.iflytek.aiui.demo.chat.utils.SemanticUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.FromType.AIUI;
import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.FromType.USER;
import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.MsgType.TEXT;
import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.MsgType.Voice;

/**
 * AIUI交互功能处理
 */

@Singleton
public class VoiceManager {
    private ChatRepo mChatRepo;
    private AIUIWrapper mAgent;
    private AIUIConfigCenter mConfigManager;
    private TTSManager mTTSManager;
    private AIUIPlayerWrapper mAIUIPlayer;
    //记录自开始录音是否有vad前端点事件抛出
    private boolean mHasBOSBeforeEnd = false;
    //当前未结束的语音交互消息，更新语音消息的听写内容时使用
    private RawMessage mAppendVoiceMsg = null;
    //当前未结束的翻译结果消息
    private RawMessage mAppendTransMsg = null;

    //语音消息开始时间，用于计算语音消息持续长度
    private long mAudioStart = System.currentTimeMillis();

    //当前应用设置
    private boolean mWakeUpEnable = false;

    private SingleLiveEvent<Boolean> isActiveInteract = new SingleLiveEvent<>();
    private MutableLiveData<Integer> mLiveVolume = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLiveWakeup = new SingleLiveEvent<>();

    //处理PGS听写(流式听写）的数组
    private String[] mIATPGSStack = new String[256];
    private List<String> mInterResultStack = new ArrayList<>();
    private String TAG = "VoiceManager";

    @Inject
    public VoiceManager(AIUIWrapper wrapper, ChatRepo chatRepo, AIUIConfigCenter configManager, TTSManager ttsManager, AIUIPlayerWrapper aiuiPlayer) {
        mChatRepo = chatRepo;
        mAgent = wrapper;
        mConfigManager = configManager;
        mTTSManager = ttsManager;
        mAIUIPlayer = aiuiPlayer;

        mConfigManager.isWakeUpEnable().observeForever((enable -> {
            mWakeUpEnable = enable;
        }));

        //AIUI事件回调监听器
        mAgent.getLiveAIUIEvent().observeForever(event -> {
            switch (event.eventType) {
                case AIUIConstant.EVENT_RESULT: {
                    processResult(event);
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    processVADEvent(event);
                }
                break;

                case AIUIConstant.EVENT_ERROR: {
                    processError(event);
                }
                break;

                case AIUIConstant.EVENT_WAKEUP: {
                    //唤醒添加语音消息
                    if (mWakeUpEnable) {
                        //唤醒自动停止播放
                        mAIUIPlayer.autoPause();
                        mTTSManager.stopTTS();

                        beginAudio();
                        mLiveWakeup.setValue(true);
                    }
                }
                break;

                case AIUIConstant.EVENT_SLEEP: {
//                    LogUtils.d(TAG, "on event" + event.eventType + "-->休眠结束语音");
                    //休眠结束语音
                    if (mWakeUpEnable) {
                        endAudio();
                        mLiveWakeup.setValue(false);
                    }
                    startSpeak();
                }
                break;
                default:
                    break;
            }
        });
    }

    /**
     * 交互是否有效(从开始到结束是否有vad前端点事件）
     *
     * @return
     */
    public LiveData<Boolean> isActiveInteract() {
        return isActiveInteract;
    }

    /**
     * 唤醒是否开启
     *
     * @return
     */
    public LiveData<Boolean> isWakeUpEnable() {
        return mConfigManager.isWakeUpEnable();
    }

    /**
     * 音量变化
     *
     * @return
     */
    public LiveData<Integer> volume() {
        return mLiveVolume;
    }

    /**
     * 唤醒和休眠变化
     *
     * @return
     */
    public LiveData<Boolean> wakeUp() {
        return mLiveWakeup;
    }

    /**
     * 文本语义
     *
     * @param message 输入文本
     */
    public void writeText(String message) {
        mTTSManager.stopTTS();
        mAIUIPlayer.autoPause();

        if (mAppendVoiceMsg != null) {
            //更新上一条未完成的语音消息内容
            updateChatMessage(mAppendVoiceMsg);
            mInterResultStack.clear();
            mAppendVoiceMsg = null;
        }
        //pers_param用于启用动态实体和所见即可说功能
        String params = "data_type=text,pers_param={\"appid\":\"\",\"uid\":\"\"}";
        sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0,
                params, message.getBytes()));
        addChatMessage(new RawMessage(USER, TEXT, message.getBytes()));
    }

    /**
     * 开始说话
     */
    public void startSpeak() {
        mTTSManager.stopTTS();
        mAIUIPlayer.autoPause();

        mAgent.startRecordAudio();
        if (!mWakeUpEnable) {
            beginAudio();
        }
        mHasBOSBeforeEnd = false;
    }

    /**
     * 停止说话
     */
    public void endSpeak() {
        mAgent.stopRecordAudio();
        if (!mWakeUpEnable) {
            endAudio();
        }

        isActiveInteract.postValue(mHasBOSBeforeEnd);
    }


    /**
     * 继续
     * 恢复到前台后，如果是唤醒模式下重新开启录音
     */
    public void onResume() {
        if (mWakeUpEnable) {
            mAgent.startRecordAudio();
        }
    }

    /**
     * 暂停
     * 唤醒模式下录音常开，pause时停止录音，避免不再前台时占用录音
     */
    public void onPause() {
        if (mWakeUpEnable) {
            mAgent.stopRecordAudio();
        }
    }


    /**
     * 录音开始，生成新的录音消息
     */
    private void beginAudio() {
        mAudioStart = System.currentTimeMillis();
        if (mAppendVoiceMsg != null) {
            //更新上一条未完成的语音消息内容
            updateChatMessage(mAppendVoiceMsg);
            mAppendVoiceMsg = null;
            mInterResultStack.clear();
        }
        if (mAppendTransMsg != null) {
            updateChatMessage(mAppendTransMsg);
            mAppendTransMsg = null;
        }

        //清空PGS听写中间结果
        for (int index = 0; index < mIATPGSStack.length; index++) {
            mIATPGSStack[index] = null;
        }

        mAppendVoiceMsg = new RawMessage(USER, Voice, new byte[]{});
        mAppendVoiceMsg.cacheContent = "";
        //语音消息msgData为录音时长
        mAppendVoiceMsg.msgData = ByteBuffer.allocate(4).putFloat(0).array();
        addChatMessage(mAppendVoiceMsg);
    }

    /**
     * 录音结束，更新录音消息内容
     */
    private void endAudio() {
        if (mAppendVoiceMsg != null) {
            mAppendVoiceMsg.msgData = ByteBuffer.allocate(4).putFloat((System.currentTimeMillis() - mAudioStart) / 1000.0f).array();
            updateChatMessage(mAppendVoiceMsg);
        }
    }


    /**
     * 发送AIUI消息
     *
     * @param message
     */
    private void sendMessage(AIUIMessage message) {
        mAgent.sendMessage(message);
    }

    /**
     * 新增聊天消息
     *
     * @param rawMessage
     */
    private void addChatMessage(RawMessage rawMessage) {
        mChatRepo.addMessage(rawMessage);
    }

    /**
     * 更新聊天界面消息
     *
     * @param message
     */
    private void updateChatMessage(RawMessage message) {
        mChatRepo.updateMessage(message);
    }


    /**
     * 处理vad事件，音量更新
     *
     * @param aiuiEvent
     */
    private void processVADEvent(AIUIEvent aiuiEvent) {
        if (aiuiEvent.arg1 == AIUIConstant.VAD_BOS) {
            mHasBOSBeforeEnd = true;
        }
        if (aiuiEvent.eventType == AIUIConstant.EVENT_VAD) {
            if (aiuiEvent.arg1 == AIUIConstant.VAD_VOL) {
                mLiveVolume.setValue(5000 + 8000 * aiuiEvent.arg2 / 100);
            }
        }
    }

    /**
     * 处理AIUI结果事件（听写结果和语义结果）
     *
     * @param event 结果事件
     */
    private void processResult(AIUIEvent event) {
        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);
            Log.d(TAG, "识别结果ydong-->" + bizParamJson.toString());
            long rspTime = event.data.getLong("eos_rslt", -1);  //响应时间
            String sub = params.optString("sub");
            if (content.has("cnt_id") && !sub.equals("tts")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                LogUtils.d(TAG, "ydong 识别结果【1】-->" + cntJson.toString());

                if ("nlp".equals(sub)) {//语义结果(nlp)
                    JSONObject semanticResult = cntJson.optJSONObject("intent");
                    if (semanticResult != null && semanticResult.length() != 0) {
                        //解析得到语义结果，将语义结果作为消息插入到消息列表中
                        RawMessage rawMessage = new RawMessage(AIUI, TEXT,
                                semanticResult.toString().getBytes(), null, rspTime);
                        addChatMessage(rawMessage);
                        LogUtils.d(TAG, "识别结果 NLP ydong-->2222222222" + semanticResult.toString());
                    }
                } else if ("iat".equals(sub)) {//听写结果(iat)
                    Log.d(TAG, "识别结果 IAT ydong-->2222222222" + cntJson.toString());
                    processIATResult(cntJson);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    long allcount = 0;

    private String initTtsParams() {

        String tag = "@" + System.currentTimeMillis();
        //构建合成参数
        StringBuilder params = new StringBuilder();
        //合成发音人
        params.append("vcn=jiajia");
        //合成速度
        params.append(",speed=50");
        //合成音调
        params.append(",pitch=50");
        //合成音量
        params.append(",volume=50");
        //合成音量
        params.append(",ent=x_tts");
        //合成tag，方便追踪合成结束，暂未实现
        params.append(",tag=").append(tag);
        return params.toString();
    }

    /**
     * 解析听写结果更新当前语音消息的听写内容
     */
    private void processIATResult(JSONObject cntJson) throws JSONException {
        if (mAppendVoiceMsg == null) return;

        JSONObject text = cntJson.optJSONObject("text");
        // 解析拼接此次听写结果
        StringBuilder iatText = new StringBuilder();
        JSONArray words = text.optJSONArray("ws");
        boolean lastResult = text.optBoolean("ls");
        Log.d(TAG, "识别结果ydong-->333333");
        for (int index = 0; index < words.length(); index++) {
            JSONArray charWord = words.optJSONObject(index).optJSONArray("cw");
            for (int cIndex = 0; cIndex < charWord.length(); cIndex++) {
                iatText.append(charWord.optJSONObject(cIndex).opt("w"));
            }
        }

        String voiceIAT = "";
        String pgsMode = text.optString("pgs");
        //非PGS模式结果
        if (TextUtils.isEmpty(pgsMode)) {
            if (TextUtils.isEmpty(iatText)) return;
            AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, initTtsParams(), iatText.toString().getBytes());
            sendMessage(startTts);
            LogUtils.d(TAG, "FINAL_RESULT---->" + voiceIAT + "<----【" + ++allcount + "】");
            //和上一次结果进行拼接
            if (!TextUtils.isEmpty(mAppendVoiceMsg.cacheContent)) {
                voiceIAT = mAppendVoiceMsg.cacheContent;//+ "\n";
            }
            voiceIAT += iatText;
        } else {
            int serialNumber = text.optInt("sn");
            mIATPGSStack[serialNumber] = iatText.toString();
            //pgs结果两种模式rpl和apd模式（替换和追加模式）
            if ("rpl".equals(pgsMode)) {
                //根据replace指定的range，清空stack中对应位置值
                JSONArray replaceRange = text.optJSONArray("rg");
                int start = replaceRange.getInt(0);
                int end = replaceRange.getInt(1);

                for (int index = start; index <= end; index++) {
                    mIATPGSStack[index] = null;
                }
            }

            StringBuilder PGSResult = new StringBuilder();
            //汇总stack经过操作后的剩余的有效结果信息
            for (int index = 0; index < mIATPGSStack.length; index++) {
                if (TextUtils.isEmpty(mIATPGSStack[index])) continue;

//                if(!TextUtils.isEmpty(PGSResult.toString())) PGSResult.append("\n");
                PGSResult.append(mIATPGSStack[index]);
                //如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mIATPGSStack[index] = null;
                }
            }
            voiceIAT = /*join(mInterResultStack) +*/  PGSResult.toString();

            if (lastResult) {
                LogUtils.d(TAG, "FINAL_RESULT---->" + voiceIAT + "<----【" + ++allcount + "】");
                mInterResultStack.add(PGSResult.toString());
            }
        }

        if (!TextUtils.isEmpty(voiceIAT)) {
            mAppendVoiceMsg.cacheContent = voiceIAT;
            updateChatMessage(mAppendVoiceMsg);
        }
    }

    /**
     * 错误处理
     * <p>
     * 在聊天对话消息中添加错误消息提示
     *
     * @param aiuiEvent
     */
    private void processError(AIUIEvent aiuiEvent) {
        //向消息列表中添加AIUI错误消息
        int errorCode = aiuiEvent.arg1;
        //AIUI网络异常，不影响交互，可以作为排查问题的线索和依据
        if (errorCode >= 10200 && errorCode <= 10215) {
            LogUtils.e("AIUI Network Warning %d, Don't Panic", errorCode);
            return;
        }

        Map<String, String> semantic = new HashMap<>();
        semantic.put("errorInfo", aiuiEvent.info);
        switch (errorCode) {
            case 10120: {
                mChatRepo.addMessage(new RawMessage(AIUI, TEXT,
                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "网络有点问题 :(", semantic, null).getBytes()));
                break;
            }

            case 20006: {
                addChatMessage(new RawMessage(AIUI, TEXT,
                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "录音启动失败 :(，请检查是否有其他应用占用录音", semantic, null).getBytes()));
                break;
            }

            default: {
                addChatMessage(new RawMessage(AIUI, TEXT,
                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, aiuiEvent.arg1 + " 错误", semantic, null).getBytes()));
            }
        }
    }

    private String join(List<String> data) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < data.size(); index++) {
            builder.append(data.get(index));
        }

        return builder.toString();
    }

}
