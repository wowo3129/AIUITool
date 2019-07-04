package com.iflytek.aiui.demo.chat.repository.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.utils.SemanticUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.FromType.AIUI;
import static com.iflytek.aiui.demo.chat.repository.chat.RawMessage.MsgType.TEXT;

@Singleton
public class ChatRepo {
    //当前消息列表
    private MutableLiveData<List<RawMessage>> mLiveInteractMsg = new MutableLiveData<>();
    private List<RawMessage> mInteractMsg = new ArrayList<>();
    private ExecutorService mExecutorService;

    @Inject
    public ChatRepo() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mLiveInteractMsg.setValue(mInteractMsg);
    }

    public LiveData<List<RawMessage>> getInteractMessages() {
        return Transformations.map(mLiveInteractMsg, messages -> {
            if (messages.size() == 0) {
                StringBuilder hello = new StringBuilder();
                hello.append("你好，很高兴见到你\n\n");
                hello.append("你可以文本或者语音跟我对话，更多的功能在左上角的设置里进行探索吧");
                fakeAIUIResult(0, Constant.SERVICE_HELLO, hello.toString(), null, null);

            }
            return messages;
        });
    }

    /**
     * 模拟消息列表中的结果信息
     *
     * @param rc       AIUI结果的rc字段
     * @param service  AIUI结果的service字段
     * @param answer   AIUI结果的answer
     * @param semantic AIUI结果的语义结构
     * @param mapData  AIUI结果的信源数据
     * @return 构造的聊天消息
     */
    public RawMessage fakeAIUIResult(int rc, String service, String answer,
                                     Map<String, String> semantic, Map<String, String> mapData) {
        RawMessage msg = new RawMessage(AIUI, TEXT,
                SemanticUtil.fakeSemanticResult(rc, service, answer, semantic, mapData).getBytes());
        addMessage(msg);
        return msg;
    }

    /**
     * 更新消息列表中的消息内容
     *
     * @param message
     */
    public void updateMessage(final RawMessage message) {
        if (message == null) return;
        mExecutorService.execute(() -> {
            message.versionUpdate();
            mLiveInteractMsg.postValue(mInteractMsg);
        });
    }

    public void addMessage(final RawMessage msg) {
        mExecutorService.execute(() -> {
            mInteractMsg.add(msg);
            mLiveInteractMsg.postValue(mInteractMsg);
        });
    }

}
