package com.iflytek.aiui.demo.chat.repository;

import android.content.Context;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.repository.chat.ChatRepo;
import com.iflytek.location.PosLocator;
import com.iflytek.location.result.GPSLocResult;
import com.iflytek.location.result.NetLocResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 位置Repo
 */

@Singleton
public class Location {
    private Context mContext;
    private AIUIWrapper mAIUI;
    private ChatRepo mChatRepo;
    private boolean mHasLocateSuccess = false;

    @Inject
    public Location(Context context, AIUIWrapper wrapper, ChatRepo chatRepo) {
        mContext = context;
        mAIUI = wrapper;
        mChatRepo = chatRepo;

        autoLocate();
    }

    /**
     * 自动定位，内部调用高德地图API
     */
    public void autoLocate() {
        mHasLocateSuccess = false;

//        PosLocator.getInstance(mContext).asyncGetLocation(PosLocator.TYPE_GPS_LOCATION,  gpsLoc -> updateLocation(((GPSLocResult)gpsLoc).getLon(),((GPSLocResult)gpsLoc).getLat()));
        PosLocator.getInstance(mContext).asyncGetLocation(PosLocator.TYPE_NET_LOCATION, netLoc -> {
            NetLocResult netLocResult = (NetLocResult) netLoc;
            if(netLocResult.getErrorCode() != 0) {
                Map<String, String> data = new HashMap<>();
                data.put("error", netLocResult.getErrorInfo());
                mChatRepo.fakeAIUIResult(0, Constant.SERVICE_FAKE_LOC, "获取位置信息错误", null, data);
            } else {
                updateLocation(netLocResult.getLon(), netLocResult.getLat());
            }
        });
    }

    /**
     * 手动设置位置信息
     * @param lng
     * @param lat
     */
    private void setLoc(double lng, double lat){
        try {
            JSONObject audioParams = new JSONObject();
            audioParams.put("msc.lng", String.valueOf(lng));
            audioParams.put("msc.lat", String.valueOf(lat));

            JSONObject params = new JSONObject();
            params.put("audioparams", audioParams);

            //完成设置后，在随后的每次会话都会携带该位置信息
            sendMessage(new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0 , 0, params.toString(), null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新位置消息并设置到AIUI
     * @param lon
     * @param lat
     */
    private void updateLocation(double lon, double lat) {
        PosLocator.getInstance(mContext).asyncDestroy();

        if(mHasLocateSuccess) {
            return;
        } else {
            mHasLocateSuccess = true;
        }

        setLoc(lon, lat);

        Map<String, String> data = new HashMap<>();
        String location = String.format("location lon %f lat %f", lon, lat);
        data.put("location", location);
        mChatRepo.fakeAIUIResult(0, Constant.SERVICE_FAKE_LOC, "已获取最新的位置信息", null, data);
    }

    private void sendMessage(AIUIMessage message) {
        mAIUI.sendMessage(message);
    }

}
