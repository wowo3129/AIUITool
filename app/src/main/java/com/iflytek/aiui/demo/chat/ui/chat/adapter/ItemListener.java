package com.iflytek.aiui.demo.chat.ui.chat.adapter;

import com.iflytek.aiui.demo.chat.model.ChatMessage;

/**
 * 聊天交互内容点击监听
 */
public interface ItemListener {
    public void onMessageClick(ChatMessage msg);
}
