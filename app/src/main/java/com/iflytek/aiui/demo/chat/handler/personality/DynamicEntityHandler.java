package com.iflytek.aiui.demo.chat.handler.personality;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;


/**
 * 动态实体结果处理类，上传成功后自动查询
 */

public class DynamicEntityHandler extends IntentHandler {
    public DynamicEntityHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        try {
            int ret = Integer.valueOf(result.data.getString("ret"));
            if(ret != 0){
                return new Answer("动态实体数据上传失败", null, null);
            } else {
                //上传成功1s后自动查询打包结果
                mMessageViewModel.getExecutor().schedule(() -> {
                    try {
                        mMessageViewModel.queryDynamicStatus(result.data.getString("sid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },1000, TimeUnit.MILLISECONDS);

                return new Answer(result.answer, null, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new Answer("错误 " + e.getMessage());
        }
    }
}
