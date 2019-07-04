package com.iflytek.aiui.demo.chat.handler.personality;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


/**
 * 打电话技能处理类，用户级动态实体示例
 */

public class TelephoneHandler extends IntentHandler {
    private static List<String> numberRecords = new ArrayList<>();

    public TelephoneHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        //没有找到联系人，提示上传本地联系人
        if(result.answer.contains("没有为您找到")) {
            StringBuilder builder = new StringBuilder();
            builder.append(result.answer);
            builder.append(NEWLINE);
            builder.append(NEWLINE);
            builder.append("<a href=\"upload_contact\">上传本地联系人数据</a>");
            return new Answer(builder.toString(), result.answer);
        }

        //上传进度信息不合成
        if(result.service.equals(Constant.SERVICE_CONTACTS_UPLOAD))  {
            return new Answer(result.answer, null, null);
        }

        //处理电话技能
        String intent = result.semantic.optString("intent");
        switch (intent) {
            case "QUERY":
            case "DIAL": {
                //提取保存电话，便于CONFIRM时使用
                JSONArray data = result.data.optJSONArray("result");
                numberRecords.clear();
                if(data != null && data.length() > 0) {
                    for(int index = 0; index < data.length(); index++) {
                        String phoneNumber = data.optJSONObject(index).optString("phoneNumber");
                        if(!TextUtils.isEmpty(phoneNumber)) {
                            numberRecords.add(phoneNumber);
                        }
                    }
                }

                //多个号码显示
                if(numberRecords.size() > 1) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(result.answer);
                    builder.append(NEWLINE);
                    builder.append(NEWLINE);

                    for(int index = 0; index < numberRecords.size(); index++) {
                        builder.append((index + 1) + ". " + numberRecords.get(index));
                        builder.append(NEWLINE);
                    }

                    return new Answer(builder.toString(), result.answer);
                }
            }
            break;

            case "INSTRUCTION": {
                String insType = optSlotValue(result, "insType");
                switch (insType) {
                    case "CONFIRM": {
                        if(numberRecords.size() > 0) {
                            String phone = numberRecords.get(0);
                            dial(phone);
                        }
                    }
                    break;

                    case "SEQUENCE": {
                        String direct = optSlotValue(result, "posRank.direct");

                        //选择范围解析
                        int offset = 1;
                        try {
                            offset = Integer.parseInt(optSlotValue(result, "posRank.offset"));
                        } catch (NumberFormatException e) {
                            return new Answer("请在有效的范围内选择");
                        }

                        if(offset > numberRecords.size()) {
                            return new Answer("请在有效的范围内选择");
                        }

                        String phone = null;
                        switch (direct) {
                            case "+": {
                                phone = numberRecords.get(offset - 1);
                            }
                            break;

                            case "-": {
                                phone = numberRecords.get(numberRecords.size() - offset);
                            }
                            break;
                        }

                        if(phone != null) {
                            dial(phone);
                        }
                    }
                    break;
                }
            }
            break;
        }

        return new Answer(result.answer);
    }

    private void dial(String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        mMessageViewModel.startActivity(dialIntent);
    }

    @Override
    public boolean urlClicked(String url) {
        if ("upload_contact".equals(url)) {
            tryUploadContacts();
        }
        return super.urlClicked(url);
    }

}
