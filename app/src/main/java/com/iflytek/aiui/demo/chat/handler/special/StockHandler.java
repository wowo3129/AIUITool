package com.iflytek.aiui.demo.chat.handler.special;

import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;

import org.json.JSONArray;
import org.json.JSONObject;

public class StockHandler extends IntentHandler {
    public StockHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        JSONArray slots = result.semantic.optJSONArray("slots");
        String imageURL = null;
        String answer = result.answer;
        for (int index = 0; index < slots.length(); index++) {
            JSONObject item = slots.optJSONObject(index);
            if (item.optString("name").equals("chartType")) {
                String chartType = item.optString("value");
                if (TextUtils.isEmpty(chartType)) break;
                JSONObject repo = result.data.optJSONArray("result").optJSONObject(0);
                if (repo == null || repo.length() == 0) break;
                answer += "  ";
                answer += NEWLINE;
                answer += NEWLINE;
                switch (chartType) {
                    case "分时图": {
                        answer += "分时图如下图所示: ";
                        imageURL = repo.optString("minurl");
                        break;
                    }
                    case "日k线": {
                        answer += "日k线如下图所示: ";
                        imageURL = repo.optString("dayurl");
                        break;
                    }
                    case "周k线": {
                        answer += "周k线如下图所示: ";
                        imageURL = repo.optString("weekurl");
                        break;
                    }
                    case "月k线": {
                        answer += "月k线如下图所示: ";
                        imageURL = repo.optString("monthurl");
                        break;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(imageURL)) {
            return new Answer(answer + NEWLINE +
                    String.format(String.format("<img src=\"%s\"/>", imageURL)),
                    answer.replace(NEWLINE, ""));
        } else {
            return new Answer(result.answer);
        }
    }
}
