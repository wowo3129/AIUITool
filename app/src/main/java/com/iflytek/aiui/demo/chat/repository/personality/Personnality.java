package com.iflytek.aiui.demo.chat.repository.personality;

import android.text.TextUtils;
import android.util.Base64;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.repository.AIUIWrapper;
import com.iflytek.aiui.demo.chat.repository.chat.ChatRepo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 用户个性化处理（所见即可说，动态实体功能）
 */
@Singleton
public class Personnality {
    private AIUIWrapper mAIUI;
    private ChatRepo mAIUIRepo;

    @Inject
    public Personnality(AIUIWrapper wrapper, ChatRepo chatRepo) {
        mAIUI = wrapper;
        mAIUIRepo = chatRepo;

        mAIUI.getLiveAIUIEvent().observeForever((event) -> {
            switch (event.eventType) {
                case AIUIConstant.EVENT_CMD_RETURN: {
                    processCmdReturnEvent(event);
                }
            }
        });
    }

    /**
     *  设置个性化(动态实体和所见即可说)生效参数
     * @param persParams
     */
    public void setPersParams(JSONObject persParams) {
        try {
            //参考文档动态实体生效使用一节
            JSONObject params = new JSONObject();
            JSONObject audioParams = new JSONObject();
            audioParams.put("pers_param", persParams.toString());
            params.put("audioparams", audioParams);

            sendMessage(new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0 , 0, params.toString(), null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 同步所见即可说
     *
     * @param data 所见即可说数据
     */
    public void syncSpeakableData(SpeakableSyncData data) {
        try {
            JSONObject syncSpeakableJson = new JSONObject();

            //从所见即可说数据中根据key获取识别热词信息
            List<String> hotWords = new ArrayList<>();
            String[] dataItems = data.speakableData.split("\r?\n");
            for (String item : dataItems) {
                JSONObject dataItem = new JSONObject(item);
                Iterator<String> hotKeysIterator;
                if (data.masterKey == null) {
                    hotKeysIterator = dataItem.keys();
                } else {
                    List<String> hotKeys = new ArrayList<>();
                    hotKeys.add(data.masterKey);
                    hotKeys.add(data.subKeys);
                    hotKeysIterator = hotKeys.iterator();
                }

                while (hotKeysIterator.hasNext()) {
                    String hotKey = hotKeysIterator.next();
                    hotWords.add(dataItem.getString(hotKey));
                }
            }

            // 识别用户数据
            JSONObject iatUserDataJson = new JSONObject();
            iatUserDataJson.put("recHotWords", TextUtils.join("|", hotWords));
            iatUserDataJson.put("sceneInfo", new JSONObject());
            syncSpeakableJson.put("iat_user_data", iatUserDataJson);

            // 语义理解用户数据
            JSONObject nlpUserDataJson = new JSONObject();

            JSONArray resArray = new JSONArray();
            JSONObject resDataItem = new JSONObject();
            resDataItem.put("res_name", data.resName);
            resDataItem.put("data", Base64.encodeToString(
                    data.speakableData.getBytes(), Base64.NO_WRAP));
            resArray.put(resDataItem);

            nlpUserDataJson.put("res", resArray);
            nlpUserDataJson.put("skill_name", data.skillName);

            syncSpeakableJson.put("nlp_user_data", nlpUserDataJson);

            // 传入的数据一定要为utf-8编码
            byte[] syncData = syncSpeakableJson.toString().getBytes("utf-8");

            AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC,
                    AIUIConstant.SYNC_DATA_SPEAKABLE, 0, "", syncData);

            sendMessage(syncAthenaMessage);
        } catch (Exception e) {
            mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_SPEAKABLE, String.format("同步所见即可说数据出错 %s", e.getMessage()), null, null);
        }
    }


    /**
     * 同步动态实体
     *
     * @param data 动态实体数据
     */
    public void syncDynamicEntity(DynamicEntityData data) {
        try {
            // 构造动态实体数据
            JSONObject syncSchemaJson = new JSONObject();
            JSONObject paramJson = new JSONObject();

            paramJson.put("id_name", data.idName);
            paramJson.put("id_value", data.idValue);
            paramJson.put("res_name", data.resName);

            syncSchemaJson.put("param", paramJson);
            syncSchemaJson.put("data", Base64.encodeToString(
                    data.syncData.getBytes(), Base64.DEFAULT | Base64.NO_WRAP));

            // 传入的数据一定要为utf-8编码
            byte[] syncData = syncSchemaJson.toString().getBytes("utf-8");

            AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0, "", syncData);
            sendMessage(syncAthenaMessage);
        } catch (Exception e) {
            mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_DYNAMIC, String.format("上传动态实体数据出错 %s", e.getMessage()), null, null);
        }
    }


    /**
     * 查询动态实体打包状态
     *
     * @param sid 上传动态实体通过CMD_RETURN返回的查询sid
     */
    public void queryDynamicSyncStatus(String sid) {
        JSONObject paramsJson = new JSONObject();
        try {
            paramsJson.put("sid", sid);
            AIUIMessage querySyncMsg = new AIUIMessage(AIUIConstant.CMD_QUERY_SYNC_STATUS,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0,
                    paramsJson.toString(), null);
            sendMessage(querySyncMsg);

        } catch (JSONException e) {
            mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_DYNAMIC_QUERY, String.format("查询动态实体数据同步状态出错 %s", e.getMessage()), null, null);
        }
    }

    private void processCmdReturnEvent(AIUIEvent event) {
        int cmdType = event.arg1;
        switch (cmdType) {
            case AIUIConstant.CMD_SYNC: {
                int syncType = event.data.getInt("sync_dtype");
                int resultCode = event.arg2;

                if (AIUIConstant.SYNC_DATA_SCHEMA == syncType) {
                    //动态实体上传结果，保存sid便于后面查询
                    String sid = event.data.getString("sid");
                    Map<String, String> dynamicRet = new HashMap<>();
                    dynamicRet.put("sid", sid);
                    dynamicRet.put("ret", String.valueOf(resultCode));
                    mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_DYNAMIC,
                            String.format("上传动态实体数据%s",
                                    resultCode == 0 ? "成功" : "失败"),
                            null, dynamicRet);

                } else if (AIUIConstant.SYNC_DATA_SPEAKABLE == syncType) {
                    //所见即可说上传结果
                    mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_SPEAKABLE,
                            String.format("可见即可说数据同步 %s", resultCode == 0 ? "成功" : "失败"),
                            null, null);
                }
            }
            break;

            case AIUIConstant.CMD_QUERY_SYNC_STATUS: {
                int syncType = event.data.getInt("sync_dtype");

                if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
                    //动态实体打包状态查询结果
                    String result = event.data.getString("result");
                    int resultCode = event.arg2;

                    Map<String, String> mapData = new HashMap<>();
                    mapData.put("ret", String.valueOf(resultCode));
                    mapData.put("result", result);
                    mAIUIRepo.fakeAIUIResult(0, Constant.SERVICE_DYNAMIC_QUERY,
                            String.format("动态实体数据状态查询结果 %s", resultCode == 0 ? "成功" : "失败"),
                            null, null);
                }
            }
        }
    }

    private void sendMessage(AIUIMessage message) {
        mAIUI.sendMessage(message);
    }

}
