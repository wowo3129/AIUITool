package com.iflytek.aiui.demo.chat.repository.personality;

import android.text.TextUtils;

/**
 * 所见即可说数据
 */

public class SpeakableSyncData {
    public String resName;
    public String skillName;
    public String masterKey;
    public String subKeys;
    public String speakableData;


    public SpeakableSyncData(String resName, String skillName, String speakableData,
                             String masterKey, String... subKeys){
        this.resName = resName;
        this.skillName = skillName;
        this.speakableData = speakableData;
        this.masterKey = masterKey;
        this.subKeys = TextUtils.join(",", subKeys);
    }

    public SpeakableSyncData(String resName, String skillName, String speakableData){
        this.resName = resName;
        this.skillName = skillName;
        this.speakableData = speakableData;
        this.masterKey = null;
        this.subKeys = null;
    }
}
