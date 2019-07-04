package com.iflytek.aiui.demo.chat.repository.chat;

import android.text.TextUtils;

import java.nio.ByteBuffer;

/**
 * 交互消息原始数据
 */
public class RawMessage {
    private static int sMsgIDStore = 0;
    public enum MsgType {
        TEXT, Voice
    }

    public enum FromType {
        USER, AIUI
    }

    public long msgID;
    private int msgVersion;
    public long responseTime;
    public FromType fromType;
    public MsgType msgType;
    public String cacheContent;
    public byte[] msgData;

    public RawMessage(FromType fromType, MsgType msgType, byte[] msgData, String cacheContent
            , long responseTime) {
        this.msgID = sMsgIDStore++;
        this.fromType = fromType;
        this.msgType = msgType;
        this.msgData = msgData;
        this.responseTime = responseTime;
        this.msgVersion = 0;
        this.cacheContent = cacheContent;
    }

    public RawMessage(FromType fromType, MsgType msgType, byte[] msgData) {
        this(fromType, msgType, msgData, null, 0);
    }

    public boolean isText() {
        return msgType == MsgType.TEXT;
    }

    public boolean isEmptyContent() {return TextUtils.isEmpty(cacheContent);}

    public boolean isFromUser() {
        return fromType == FromType.USER;
    }

    public int version() {
        return msgVersion;
    }

    public void versionUpdate() {
       msgVersion++;
    }

    public int getAudioLen() {
        if(msgType == MsgType.Voice){
            return Math.round(ByteBuffer.wrap(msgData).getFloat());
        } else {
            return 0;
        }
    }
}
