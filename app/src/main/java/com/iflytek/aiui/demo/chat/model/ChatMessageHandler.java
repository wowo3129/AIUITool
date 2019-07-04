package com.iflytek.aiui.demo.chat.model;

import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.NoTTSHandler;
import com.iflytek.aiui.demo.chat.handler.personality.DishSkillHandler;
import com.iflytek.aiui.demo.chat.handler.personality.DynamicEntityHandler;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.handler.HintHandler;
import com.iflytek.aiui.demo.chat.handler.NotificationHandler;
import com.iflytek.aiui.demo.chat.handler.personality.OrderMenuHandler;
import com.iflytek.aiui.demo.chat.handler.player.PlayerHandler;
import com.iflytek.aiui.demo.chat.handler.personality.TelephoneHandler;
import com.iflytek.aiui.demo.chat.handler.WeatherHandler;
import com.iflytek.aiui.demo.chat.handler.special.AnimalCryHandler;
import com.iflytek.aiui.demo.chat.handler.special.CookBookHandler;
import com.iflytek.aiui.demo.chat.handler.special.MapHandler;
import com.iflytek.aiui.demo.chat.handler.special.SMSHandler;
import com.iflytek.aiui.demo.chat.handler.special.StockHandler;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.zzhoujay.richtext.callback.OnUrlClickListener;
import com.zzhoujay.richtext.callback.OnUrlLongClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 聊天消息处理类
 * <p>
 * 通过service将解析分配到不同的IntentHandler，通过getFormatMessage返回处理后的格式化内容
 */

public class ChatMessageHandler implements OnUrlClickListener, OnUrlLongClickListener {
    private static final String KEY_SEMANTIC = "semantic";
    private static final String KEY_OPERATION = "operation";
    private static final String SLOTS = "slots";
    private static Map<String, Class> handlerMap = new HashMap<>();
    private static Answer latestTTSAnswer = null;

    static {
        handlerMap.put("telephone", TelephoneHandler.class);
        handlerMap.put("weather", WeatherHandler.class);
        handlerMap.put("cookbook", CookBookHandler.class);
        handlerMap.put("stock", StockHandler.class);
        handlerMap.put("mapU", MapHandler.class);
        handlerMap.put("animalCries", AnimalCryHandler.class);

        handlerMap.put("telephone", TelephoneHandler.class);
        handlerMap.put("message", SMSHandler.class);
        handlerMap.put(Constant.SERVICE_CONTACTS_UPLOAD, TelephoneHandler.class);

        //本地模拟的AIUI结果，方便统一处理显示
        handlerMap.put(Constant.SERVICE_DYNAMIC, DynamicEntityHandler.class);
        handlerMap.put(Constant.SERVICE_DYNAMIC_QUERY, NoTTSHandler.class);
        handlerMap.put(Constant.SERVICE_SPEAKABLE, NoTTSHandler.class);
        handlerMap.put(Constant.SERVICE_FAKE_LOC, NoTTSHandler.class);
        handlerMap.put(Constant.SERVICE_ERROR, NoTTSHandler.class);

        handlerMap.put(Constant.SERVICE_UNKNOWN, HintHandler.class);
        handlerMap.put(Constant.SERVICE_NOTIFICATION, NotificationHandler.class);

        //用户个性化技能处理示例
        handlerMap.put("FOOBAR.DishSkill", DishSkillHandler.class);
        handlerMap.put("FOOBAR.MenuSkill", OrderMenuHandler.class);
    }

    public static Answer getLatestTTSAnswer() {
        return latestTTSAnswer;
    }


    private ChatViewModel mViewModel;
    private PlayerViewModel mPlayer;
    private PermissionChecker mPermissionChecker;
    private RawMessage mMessage;
    private SemanticResult mSemanticResult;
    private IntentHandler mHandler;

    public ChatMessageHandler(ChatViewModel viewModel, PlayerViewModel player, PermissionChecker checker, RawMessage message) {
        this.mViewModel = viewModel;
        this.mPlayer = player;
        this.mPermissionChecker = checker;
        this.mMessage = message;
    }

    public String getFormatMessage() {
        if (mMessage.fromType == RawMessage.FromType.USER) {
            //用户消息
            if (mMessage.msgType == RawMessage.MsgType.TEXT) {
                return new String(mMessage.msgData);
            } else {
                return "";
            }
        } else {
            initHandler();
//            if(!mSemanticResult.service.startsWith("fake.") && !TextUtils.isEmpty(mSemanticResult.answer)){
//                //这种情况下会下发语义后合成音频，先暂停播放，后面根据情况再进一步处理
//                mPlayer.pauseTTS();
//            }
            if (mHandler != null) {
                Answer answer =  mHandler.getFormatContent(mSemanticResult);
                //不是本地构造的结果并且处理后结果的合成内容和Answer相同，直接继续之前暂停语义后合成音频，不使用主动语音合成
                boolean justResumeTTS = !mSemanticResult.service.startsWith("fake.") && TextUtils.equals(answer.getTTSContent(), mSemanticResult.answer) && !TextUtils.equals(mSemanticResult.answer, "已为您完成操作");
                mPlayer.startTTS(answer.getTTSContent(), answer.getAnswerCallback(), justResumeTTS);
                mSemanticResult.answer = answer.getAnswer();

                //保存最后一次可播放的Answer，用于后面 再说一次 问法的处理
                if(!TextUtils.isEmpty(answer.getTTSContent())) {
                    latestTTSAnswer = answer;
                }
                return answer.getAnswer();
            } else {
                return "错误";
            }
        }
    }

    @Override
    public boolean urlClicked(String url) {
        initHandler();
        return mHandler != null && mHandler.urlClicked(url);
    }

    @Override
    public boolean urlLongClick(String url) {
        initHandler();
        return mHandler != null && mHandler.urlLongClick(url);
    }

    private void initHandler() {
        if (mMessage.fromType == RawMessage.FromType.USER) {
            return;
        }

        initSemanticResult();

        if (mHandler == null) {
            //根据语义结果的service查找对应的IntentHandler，并实例化
            Class handlerClass = handlerMap.get(mSemanticResult.service);
            if (handlerClass == null) {
                handlerClass = PlayerHandler.class;
            }
            try {
                Constructor constructor = handlerClass.getConstructor(ChatViewModel.class, PlayerViewModel.class, PermissionChecker.class);
                mHandler = (IntentHandler) constructor.newInstance(mViewModel, mPlayer, mPermissionChecker);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initSemanticResult() {
        if (mSemanticResult != null) return;
        // 解析语义结果
        JSONObject semanticResult;
        mSemanticResult = new SemanticResult();
        try {
            semanticResult = new JSONObject(new String(mMessage.msgData));
            mSemanticResult.rc = semanticResult.optInt("rc");
            if (mSemanticResult.rc == 4) {
                mSemanticResult.service = Constant.SERVICE_UNKNOWN;
            } else if (mSemanticResult.rc == 1) {
                mSemanticResult.service = semanticResult.optString("service");
                mSemanticResult.answer = "语义错误";
            } else {
                mSemanticResult.service = semanticResult.optString("service");
                mSemanticResult.answer = semanticResult.optJSONObject("answer") == null ?
                        "已为您完成操作" : semanticResult.optJSONObject("answer").optString("text");
                // 兼容3.1和4.0的语义结果，通过判断结果最外层的operation字段
                boolean isAIUI3_0 = semanticResult.has(KEY_OPERATION);
                if (isAIUI3_0) {
                    //将3.1语义格式的语义转换成4.1
                    JSONObject semantic = semanticResult.optJSONObject(KEY_SEMANTIC);
                    if (semantic != null) {
                        JSONObject slots = semantic.optJSONObject(SLOTS);
                        JSONArray fakeSlots = new JSONArray();
                        Iterator<String> keys = slots.keys();
                        while (keys.hasNext()) {
                            JSONObject item = new JSONObject();
                            String name = keys.next();
                            item.put("name", name);
                            item.put("value", slots.get(name));

                            fakeSlots.put(item);
                        }

                        semantic.put(SLOTS, fakeSlots);
                        semantic.put("intent", semanticResult.optString(KEY_OPERATION));
                        mSemanticResult.semantic = semantic;
                    }
                } else {
                    mSemanticResult.semantic = semanticResult.optJSONArray(KEY_SEMANTIC) == null ?
                            semanticResult.optJSONObject(KEY_SEMANTIC) :
                            semanticResult.optJSONArray(KEY_SEMANTIC).optJSONObject(0);
                }
                mSemanticResult.answer = mSemanticResult.answer.replaceAll("\\[[a-zA-Z0-9]{2}\\]", "");
                mSemanticResult.data = semanticResult.optJSONObject("data");
                if(mSemanticResult.data == null) {
                    mSemanticResult.data = new JSONObject();
                }
            }
        } catch (JSONException e) {
            mSemanticResult.rc = 4;
            mSemanticResult.service = Constant.SERVICE_UNKNOWN;
        }
    }
}
