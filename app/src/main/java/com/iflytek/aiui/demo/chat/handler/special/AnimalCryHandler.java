package com.iflytek.aiui.demo.chat.handler.special;

import com.iflytek.aiui.demo.chat.handler.player.PlayerHandler;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;


/**
 * 动物叫声
 */
public class AnimalCryHandler extends PlayerHandler {
    public AnimalCryHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    /**
     * 动物叫声返回多个可选的音频结果，只取第一个进行播报
     * @return
     */
    @Override
    protected int retrieveCount() {
        return 1;
    }
}
