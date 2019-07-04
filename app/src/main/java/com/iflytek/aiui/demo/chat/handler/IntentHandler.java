package com.iflytek.aiui.demo.chat.handler;

import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.personality.DynamicEntityData;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.utils.SemanticUtil;
import com.zzhoujay.richtext.callback.OnUrlClickListener;
import com.zzhoujay.richtext.callback.OnUrlLongClickListener;

import org.json.JSONArray;

import java.util.List;

/**
 * 语义结果处理抽象类
 */

public abstract class IntentHandler implements OnUrlClickListener, OnUrlLongClickListener {
    public static final String NEWLINE = "<br/>";
    public static final String NEWLINE_NO_HTML = "\n";
    public static final String CONTROL_TIP = "你可以通过语音控制上一个，下一个哦";
    public static int controlTipReqCount = 0;

    protected ChatViewModel mMessageViewModel;
    protected PlayerViewModel mPlayer;
    protected PermissionChecker mPermissionChecker;

    public IntentHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker){
        this.mPlayer = player;
        this.mMessageViewModel = model;
        this.mPermissionChecker = checker;
    }

    public abstract Answer getFormatContent(SemanticResult result);

    @Override
    public boolean urlClicked(String url) {
        return true;
    }

    @Override
    public boolean urlLongClick(String url) {
        return true;
    }

    //减少播放控制类指令提示次数
    public static boolean isNeedShowControlTip() {
        return controlTipReqCount++ % 5 == 0;
    }

    protected String optSlotValue(SemanticResult result, String slotKey) {
        JSONArray slots = result.semantic.optJSONArray("slots");
        for(int index = 0; index < slots.length(); index++) {
            String key = slots.optJSONObject(index).optString("name");

            if(key.equalsIgnoreCase(slotKey)) {
               return  slots.optJSONObject(index).optString("value");
            }
        }

        return null;
    }

    protected void tryUploadContacts() {
        mPermissionChecker.checkPermission(android.Manifest.permission.READ_CONTACTS, () -> uploadContacts(), null);
    }

    private void uploadContacts() {
        // 上传进度消息，后续根据进度进行更新
        final RawMessage progressMsg = mMessageViewModel.fakeAIUIResult(0, Constant.SERVICE_CONTACTS_UPLOAD, "提取联系人数据中");
        mMessageViewModel.getExecutor().execute(() -> {
            List<String> contacts = mMessageViewModel.getContacts();

            if(contacts == null || contacts.size() == 0){
                mMessageViewModel.fakeAIUIResult(0, Constant.SERVICE_CONTACTS_UPLOAD, "请允许应用请求的联系人读取权限");
                return;
            }

            StringBuilder contactJson = new StringBuilder();
            for (String contact : contacts) {
                String[] nameNumber = contact.split("\\$\\$");
                //联系人空号码
                if(nameNumber.length == 1) {
                    contactJson.append(String.format("{\"name\": \"%s\"}\n",
                            nameNumber[0]));
                } else {
                    contactJson.append(String.format("{\"name\": \"%s\", \"phoneNumber\": \"%s\" }\n",
                            nameNumber[0], nameNumber[1]));
                }
            }
            updateProgress(progressMsg, "联系人数据提取完成, 开始上传");

            mMessageViewModel.syncDynamicData(new DynamicEntityData(
                    "IFLYTEK.telephone_contact", "uid", "", contactJson.toString()));
            mMessageViewModel.putPersParam("uid", "");
        });
    }

    // 上传进入信息更新
    private void updateProgress(RawMessage progressMsg, String update) {
        progressMsg.cacheContent = null;
        progressMsg.msgData = SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_CONTACTS_UPLOAD, update, null, null).getBytes();
        mMessageViewModel.updateMessage(progressMsg);
    }
}
