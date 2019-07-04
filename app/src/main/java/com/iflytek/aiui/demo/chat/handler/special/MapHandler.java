package com.iflytek.aiui.demo.chat.handler.special;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;

public class MapHandler extends IntentHandler {
    public MapHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        if(!isNativeMapSupport()) {
            return new Answer("亲，你好像还没有安装启用高德地图或百度地图，建议先安装启用该应用，再使用导航技能");
        }

        String intent = result.semantic.optString("intent");

        switch (intent) {
            case "LOCATE": {
                String location = optSlotValue(result, "endLoc.ori_loc");
                String landmark = optSlotValue(result, "landmark.ori_loc");

                if(location.equals("CURRENT_ORI_LOC")) {
                    //当前位置查询
                    return currentLocation();
                } else if(!TextUtils.isEmpty(landmark)) {
                    //xxx附近的xxx
                    if(landmark.equals("CURRENT_ORI_LOC")) {
                        //当前位置附近的xxx
                        return nearby(location);
                    } else {
                        //xxx附近的xxx
                        return search(landmark + "附近的" + location);
                    }
                } else {
                    //xxx在哪
                    return search(location);
                }
            }

            case "QUERY": {
                String startLocation = optSlotValue(result, "startLoc.ori_loc");
                if("CURRENT_ORI_LOC".equals(startLocation)) {
                    //当前位置出发
                    startLocation = null;
                }

                String endLocation = optSlotValue(result, "endLoc.ori_loc");
                String landmark = optSlotValue(result, "landmark.ori_loc");

                if(!TextUtils.isEmpty(landmark) && !landmark.equals("CURRENT_ORI_LOC"))  {
                   endLocation = landmark + "附近的" + endLocation;
                }

                return navigation(startLocation, endLocation);
            }
        }

        return new Answer(result.answer);
    }

    private boolean isNativeMapSupport() {
        return isBaiduMapSupport() || isAMapSupport();
    }

    private boolean isBaiduMapSupport() {
        return mMessageViewModel.isAvailable("com.baidu.BaiduMap");
    }

    private boolean isAMapSupport() {
        return mMessageViewModel.isAvailable("com.autonavi.minimap");
    }

    private Answer currentLocation() {
        String uri = null;
        String answer = null;
        if(isAMapSupport()) {
            uri = "androidamap://myLocation?sourceApplication=aiui";
            answer = "即将为你打开高德地图";
        } else if(isBaiduMapSupport()) {
            uri = "baidumap://map/show";
            answer = "即将为你打开百度地图";
        }
        mMessageViewModel.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer nearby(String type) {
        String uri = null;
        String answer = null;
        if(isAMapSupport()) {
            uri = "androidamap://arroundpoi?sourceApplication=softname&keywords=" + type;
            answer = "即将为你打开高德地图";
        } else if(isBaiduMapSupport()) {
            uri = "baidumap://map/place/nearby?query=" + type;
            answer = "即将为你打开百度地图";
        }
        mMessageViewModel.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer search(String location) {
        String uri = null;
        String answer = null;
        if(isAMapSupport()) {
            uri = "androidamap://poi?sourceApplication=aiui&keywords=" + location;
            answer = "即将为你打开高德地图";
        } else if(isBaiduMapSupport()) {
            uri = "baidumap://map/place/search?query=" + location;
            answer = "即将为你打开百度地图";
        }
        mMessageViewModel.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer navigation(String startLocation, String endLocation) {
        String uri = null;
        String answer = null;
        if(isAMapSupport()) {
            //不支持设置起点名称
            uri = "androidamap://keywordNavi?sourceApplication=aiui&keyword=" + endLocation + "&style=2";
            answer = "即将为你打开高德地图进行导航";
        } else if(isBaiduMapSupport()) {
            uri = String.format("baidumap://map/direction?%sdestination=%s" , startLocation == null? "" : "origin=" + startLocation + "&", endLocation);
            answer = "即将为你打开百度地图进行导航";
        }
        mMessageViewModel.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }
}
