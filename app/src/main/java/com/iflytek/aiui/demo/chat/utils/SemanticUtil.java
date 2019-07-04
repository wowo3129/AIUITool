package com.iflytek.aiui.demo.chat.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SemanticUtil {
    public static String fakeSemanticResult(int rc, String service, String answer,
                                      Map<String, String> semantic,
                                      Map<String, String> mapData){
        try {
            JSONObject data = new JSONObject();
            if(mapData != null){
                for(String key : mapData.keySet()){
                    data.put(key, mapData.get(key));
                }
            }

            JSONObject semanticData = new JSONObject();
            if(semantic != null){
                for(String key : semantic.keySet()){
                    semanticData.put(key, semantic.get(key));
                }
            }


            JSONObject answerData = new JSONObject();
            answerData.put("text", answer);


            JSONObject fakeResult = new JSONObject();
            fakeResult.put("rc", rc);
            fakeResult.put("answer", answerData);
            fakeResult.put("service", service);
            fakeResult.put("semantic", semanticData);
            fakeResult.put("data", data);
            String s = fakeResult.toString();
            return s;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
