package com.iflytek.aiui.demo.chat.handler.personality;

import android.support.annotation.NonNull;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.repository.personality.SpeakableSyncData;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 菜谱技能处理，所见即可说功能示例
 */

public class DishSkillHandler extends IntentHandler {
    private static Map<String, JSONObject> dishDetailCache = new HashMap<>();

    public DishSkillHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        String intent = result.semantic.optString("intent");
        if(intent.equals("DishSearchIntent")) {
            //菜谱搜索（示例：有哪些川菜）
            return new Answer(constructSearchResult(result));
        }else if(intent.equals("DishStepIntent")){
            //菜品做法详情
            return new Answer(constructStepResult(result));
        }
        return new Answer(result.answer);
    }

    @NonNull
    private String constructSearchResult(SemanticResult result) {
        StringBuilder dishesDisplay = new StringBuilder();
        dishesDisplay.append(result.answer);
        dishesDisplay.append(NEWLINE);
        dishesDisplay.append(NEWLINE);
        JSONArray dishes = result.data.optJSONArray("result");
        for(int index=0;index<dishes.length();index++){
            JSONObject dish = dishes.optJSONObject(index);
            dishesDisplay.append(String.format("%d. %s", index + 1, dish.optString("title")));
            dishesDisplay.append(NEWLINE);
            // 缓存对应菜品的详情数据，在展示菜品做法时使用
            dishDetailCache.put(dish.optString("title"), dish);
        }
        dishesDisplay.append(NEWLINE);

        // 同步菜谱所见即可说数据
        syncDishData(result);
        return dishesDisplay.toString();
    }

    @NonNull
    private String constructStepResult(SemanticResult result) {
        JSONObject slot = result.semantic.optJSONArray("slots").optJSONObject(0);
        StringBuilder detailDisplay = new StringBuilder();
        String dishName = slot.optString("value");
        // 从缓存中取得当前菜品的做法详情进行展示
        JSONObject dishDetail = dishDetailCache.get(dishName);
        if(dishDetail != null){
            detailDisplay.append(String.format("%s 是这么做滴", dishName));
            detailDisplay.append(NEWLINE);
            detailDisplay.append(NEWLINE);
            detailDisplay.append(String.format("<img src=\"%s\"/>",
                    "http://pic3.qqmofasi.com/2014/04/17/23_f5t5ttOr6KY4rQtdqMY6_large.jpg"));
            detailDisplay.append(NEWLINE);
            detailDisplay.append(NEWLINE);
            detailDisplay.append(String.format("配料: %s%s", NEWLINE, dishDetail.optString("accessory")));
            detailDisplay.append(NEWLINE);
            detailDisplay.append(NEWLINE);
            detailDisplay.append(String.format("原料: %s%s", NEWLINE, dishDetail.optString("ingredient")));
            detailDisplay.append(NEWLINE);
            detailDisplay.append(NEWLINE);
            // 根据序列号的正则添加换行,便于显示
            detailDisplay.append(String.format("步骤: %s",
                    dishDetail.optString("steps")
                            .replaceAll("(\\d+\\.)",  NEWLINE + "$1")).replaceAll(";", ""));
        }else {
            detailDisplay.append(String.format("没有为您找到%s对应做法", dishName));
        }

        return detailDisplay.toString();
    }

    private void syncDishData(SemanticResult result) {
        // 构造所见即可说数据
        StringBuilder speakableData = new StringBuilder();
        JSONArray dishes = result.data.optJSONArray("result");
        for(int index=0;index<dishes.length();index++){
            JSONObject dish = dishes.optJSONObject(index);
            speakableData.append(String.format("{\"title\":\"%s\"}\n", dish.optString("title")));
        }

        SpeakableSyncData speakableSyncData = new SpeakableSyncData(
                "FOOBAR.dishRes", "FOOBAR.DishSkill", speakableData.toString());

        // 进行所见即可说同步操作
        mMessageViewModel.syncSpeakableData(speakableSyncData);
        // 设置pers_params 生效所见即可说，生效详情参考接入文档中动态实体生效使用一节
        mMessageViewModel.putPersParam("uid", "");
        // 生成提示语
        mMessageViewModel.fakeAIUIResult(0,
                "FOOBAR.DishSkill", "同步成功后通过 xxx怎么做 查询具体做法");
    }
}
