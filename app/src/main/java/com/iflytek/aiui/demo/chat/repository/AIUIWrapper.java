package com.iflytek.aiui.demo.chat.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class AIUIWrapper {
    private Context mContext;
    private ScheduledExecutorService mExecutorService;
    private AIUIConfigCenter mConfigManager;
    private AIUIAgent mAgent;

    //等待清除历史的标志，在CONNECT_SERVER时处理
    private boolean mPendingHistoryClear = false;

    //AIUI当前录音状态，避免连续两次startRecordAudio时的录音失败
    private boolean mAudioRecording = false;
    private boolean mMSCInitialized = false;

    private boolean isWakeUpEnable = false;
    private boolean isTranslationEnable = false;

    //AIUI当前状态
    private int mCurrentState = AIUIConstant.STATE_IDLE;
    private MutableLiveData<AIUIEvent> mLiveAIUIEvent = new MutableLiveData<>();
    private MutableLiveData<String> mLiveUID = new MutableLiveData<>();

    private AIUIListener mAIUIListener = (event) -> {
        mLiveAIUIEvent.setValue(event);
        switch (event.eventType) {
            case AIUIConstant.EVENT_STATE: {
                mCurrentState = event.arg1;
                // 状态事件
                mCurrentState = event.arg1;

                if (AIUIConstant.STATE_IDLE == mCurrentState) {
                    // 闲置状态，AIUI未开启
                    Log.i("VoiceManager", "on event: " + event.eventType + "<--闲置状态，AIUI未开启[0]");
                } else if (AIUIConstant.STATE_READY == mCurrentState) {
                    AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                    mAgent.sendMessage(wakeupMsg);
                    // AIUI已就绪，等待唤醒
                    Log.i("VoiceManager", "on event: " + event.eventType + "<--AIUI已就绪，等待唤醒[1]");
                } else if (AIUIConstant.STATE_WORKING == mCurrentState) {
                    // AIUI工作中，可进行交互
                    Log.i("VoiceManager", "on event: " + event.eventType + "<--AIUI工作中，可进行交互[2]");
                }
            }
            break;

            case AIUIConstant.EVENT_ERROR: {
                int errorCode = event.arg1;
                if (errorCode == 20006) {
                    mAudioRecording = false;
                }
            }
            break;

            case AIUIConstant.EVENT_CONNECTED_TO_SERVER: {
                Log.i("VoiceManager", "on event: " + event.eventType + "EVENT_CONNECTED_TO_SERVER <--AIUI工作中，可进行交互[3]");
                startRecordAudio();
                if (mPendingHistoryClear) {
                    mPendingHistoryClear = false;
                    //需要在CONNECT_TO_SERVER后才能发送清除历史指令，清除云端的交互历史
                    sendMessage(new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null, null));
                }

                mLiveUID.setValue(event.data.getString("uid"));
            }
            break;
            default:
                break;
        }
    };


    @Inject
    public AIUIWrapper(Context context, AIUIConfigCenter configManager, ScheduledExecutorService executorService) {
        mContext = context;
        mConfigManager = configManager;
        mExecutorService = executorService;


        //监听设置变化
        mConfigManager.getConfig().observeForever(config -> {
            if (!mMSCInitialized) {
                initializeMSCIfExist(context, config.runtimeConfig());
                mMSCInitialized = true;
            }

            isTranslationEnable = config.isTranslationEnable();
            isWakeUpEnable = config.isWakeUpEnable();

            //销毁增加延迟，避免与onChatResume中startRecordAudio间隔过近（异步成功）导致的destroy时不能销毁录音机问题
            if (mAgent != null) {
                mExecutorService.schedule(() -> {
                    if (mAgent != null) {
                        AIUIAgent temp = mAgent;
                        mAgent = null;
                        Timber.d("start Destroy AIUIAgent");
                        temp.destroy();
                    }
                    initAIUIAgent(mContext, config.runtimeConfig());
                }, 300, TimeUnit.MILLISECONDS);
            } else {
                //首次启动，设置清除历史标记
                mPendingHistoryClear = true;
                initAIUIAgent(mContext, config.runtimeConfig());
            }
        });
    }

    /**
     * 根据AIUI配置创建AIUIAgent
     *
     * @param context
     * @param config
     */
    private void initAIUIAgent(Context context, JSONObject config) {
        mAudioRecording = false;
        mAgent = AIUIAgent.createAgent(context, config.toString(), mAIUIListener);

        AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
        mAgent.sendMessage(wakeupMsg);
        //唤醒模式自动开始录音
        /*if (isWakeUpEnable) {
            startRecordAudio();
        }*/

//        startRecordAudio();
    }


    /**
     * 发送AIUI消息
     *
     * @param message
     */
    public void sendMessage(AIUIMessage message) {
        if (mAgent != null) {
            //确保AIUI处于唤醒状态
            if (mCurrentState != AIUIConstant.STATE_WORKING && !isWakeUpEnable) {
                mAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null));
            }

            mAgent.sendMessage(message);
        }
    }

    /**
     * 开始录音
     */
    public void startRecordAudio() {
        if (!mAudioRecording) {
            String params = "data_type=audio,sample_rate=16000";
            //流式识别
            params += ",dwa=wpgs";
            sendMessage(new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null));
            mAudioRecording = true;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecordAudio() {
        if (mAudioRecording) {
            sendMessage(new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, "data_type=audio,sample_rate=16000", null));
            mAudioRecording = false;
        }
    }

    public LiveData<AIUIEvent> getLiveAIUIEvent() {
        return mLiveAIUIEvent;
    }

    public LiveData<String> getLiveUID() {
        return mLiveUID;
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

    private void initializeMSCIfExist(Context context, JSONObject config) {
        try {
            Class UtilityClass = getClass().getClassLoader().loadClass("com.iflytek.cloud.SpeechUtility");
            Method createMethod = UtilityClass.getDeclaredMethod("createUtility", Context.class, String.class);
            createMethod.invoke(null, context, "appid=" + config.optJSONObject("login").optString("appid") + ",engine_start=ivw");
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }
}
