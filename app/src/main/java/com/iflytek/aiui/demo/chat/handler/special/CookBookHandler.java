package com.iflytek.aiui.demo.chat.handler.special;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;

import org.json.JSONArray;
import org.json.JSONObject;

public class CookBookHandler extends IntentHandler {
    public CookBookHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        JSONArray data = result.data.optJSONArray("result");

        int queryStep = -1;
        try {
            queryStep = Integer.valueOf(optSlotValue(result, "queryStep"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(data != null && data.length() != 0) {
            StringBuffer display;
            if(queryStep == -1) {
                display = buildFullSteps(data);
            } else {
                display = buildForStep(data, queryStep);
            }

            if(TextUtils.isEmpty(display.toString())) {
                return new Answer(result.answer);
            } else {
                return new Answer(display.toString(), result.answer);
            }
        } else {
            return new Answer(result.answer);
        }
    }

    @NonNull
    private StringBuffer buildForStep(JSONArray data, int stepNum) {
        StringBuffer display = new StringBuffer();
        JSONObject firstGuide = data.optJSONObject(0);

        //步骤
        if(firstGuide.has("stepsWithImg")) {
            display.append(NEWLINE);
            display.append("<b>步骤" + stepNum + "：</b>");
            display.append(NEWLINE);

            JSONArray stepsWithImg = firstGuide.optJSONArray("stepsWithImg");
            for(int index = 0; index < stepsWithImg.length(); index++) {
                JSONObject step = stepsWithImg.optJSONObject(index);
                String position = step.optString("position");
                try {
                    if(Integer.valueOf(position) == stepNum) {
                        String image = step.optString("image");
                        if(!TextUtils.isEmpty(image)) {
                            display.append(String.format("<img src=\"%s\"/>", image));
                            display.append(NEWLINE);
                        }

                        display.append(String.format("%s", step.optString("content")));
                        display.append(NEWLINE);
                        display.append(NEWLINE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return display;
    }
    @NonNull
    private StringBuffer buildFullSteps(JSONArray data) {
        StringBuffer display = new StringBuffer();
        JSONObject firstGuide = data.optJSONObject(0);

        display.append(String.format("<img src=\"%s\"/>", firstGuide.optString("imgUrl")));
        display.append(NEWLINE);

        display.append("<b>菜名： </b>" );
        display.append(NEWLINE);
        display.append(firstGuide.optString("title"));
        display.append(NEWLINE);
        display.append(NEWLINE);

        if(firstGuide.has("ingredient")) {
            display.append("<b>用料： </b>" );
            display.append(NEWLINE);
            String[] splitIngredient = firstGuide.optString("ingredient").split(";|；");
            for(String ingredient : splitIngredient) {
                //用料名 和 数量
                String[] ingredientAndQuantity = ingredient.split(",|：");
                display.append(String.format("%s", ingredientAndQuantity[0]));
                for(int index = 0; index < (10 - ingredientAndQuantity[0].length()); index++) {
                    display.append("　");
                }
                if(ingredientAndQuantity.length == 2) {
                    display.append(String.format("%s", ingredientAndQuantity[1]));
                }
                display.append(NEWLINE);
            }
            display.append(NEWLINE);
        }

        //步骤
        if(firstGuide.has("stepsWithImg")) {
            display.append(NEWLINE);
            display.append("<b>步骤：</b>");
            display.append(NEWLINE);

            JSONArray stepsWithImg = firstGuide.optJSONArray("stepsWithImg");
            for(int index = 0; index < stepsWithImg.length(); index++) {
                JSONObject step = stepsWithImg.optJSONObject(index);

                String image = step.optString("image");
                if(!TextUtils.isEmpty(image)) {
                    display.append(String.format("<img src=\"%s\"/>", image));
                    display.append(NEWLINE);
                }

                display.append(String.format("<b>%s</b>. %s", step.optString("position"), step.optString("content")));
                display.append(NEWLINE);
                display.append(NEWLINE);
            }
        }
        return display;
    }

}
