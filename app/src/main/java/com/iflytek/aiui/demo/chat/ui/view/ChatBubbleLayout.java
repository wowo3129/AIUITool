package com.iflytek.aiui.demo.chat.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;

/**
 * 聊天气泡布局
 */

public class ChatBubbleLayout extends BubbleLayout {
    public ChatBubbleLayout(Context context) {
        super(context);
    }

    public ChatBubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatBubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setChatBubbleColor(int color){
        setBubbleColor(color);
    }

    public void setChatArrowDirection(ArrowDirection direction) {
        setArrowDirection(direction);
    }
}
