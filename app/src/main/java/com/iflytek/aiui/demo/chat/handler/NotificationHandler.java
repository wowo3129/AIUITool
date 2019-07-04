package com.iflytek.aiui.demo.chat.handler;

import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;


/**
 * 极光推送消息处理类(目前sample没有使用)
 */

public class NotificationHandler extends IntentHandler {
    private SemanticResult result;

    public NotificationHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        this.result = result;
        StringBuilder answer = new StringBuilder(result.answer);

        if(result.data.has("appid")) {
            answer.append(NEWLINE);
            answer.append(NEWLINE);
            answer.append("<a href=\"use\">马上体验一下</a>");

            return new Answer(answer.toString().replaceAll("\n", NEWLINE), result.answer);
        }

        return new Answer(answer.toString());
    }

    @Override
    public boolean urlClicked(String url) {
        if("use".equals(url)) {
            //每次获取，getFormatContent只会调用一次
            String appid = result.data.optString("appid", "");
            String key = result.data.optString("key", "");
            String scene = result.data.optString("scene", "main");

            mMessageViewModel.useNewAppID(appid, key, scene);
            mMessageViewModel.fakeAIUIResult(0, "notification",
                    "已切换使用新的AppID，赶快开始体验吧\n\n若需要恢复，可在设置中重新配置");
            return true;
        } else {
            return super.urlClicked(url);
        }
    }
}
