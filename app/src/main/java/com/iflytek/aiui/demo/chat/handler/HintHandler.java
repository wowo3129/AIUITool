package com.iflytek.aiui.demo.chat.handler;

import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

/**
 * 拒识（rc = 4）结果处理
 */

public class HintHandler extends IntentHandler {
    private final StringBuilder defaultAnswer;

    public HintHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
        defaultAnswer = new StringBuilder();
        defaultAnswer.append("你好，我不懂你的意思");
        defaultAnswer.append(IntentHandler.NEWLINE_NO_HTML);
        defaultAnswer.append(IntentHandler.NEWLINE_NO_HTML);
        defaultAnswer.append("在后台添加更多技能让我变得更聪明吧");
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        if(TextUtils.isEmpty(result.answer)) {
            return new Answer(defaultAnswer.toString());
        } else {
            return new Answer(result.answer);
        }
    }
}
