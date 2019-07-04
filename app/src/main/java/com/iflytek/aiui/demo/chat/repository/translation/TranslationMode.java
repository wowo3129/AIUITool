package com.iflytek.aiui.demo.chat.repository.translation;


import com.iflytek.aiui.demo.chat.repository.config.AIUIConfig;

import org.json.JSONException;
import org.json.JSONObject;


public class TranslationMode {
    private static TranslationMode defaultTransParams = new TranslationMode(SrcLanguage.CN_MANDARIN_NEAR, DestLanguage.EN);


    private SrcLanguage srcLanguage;
    private DestLanguage destLanguage;


    public TranslationMode(SrcLanguage srcLanguage, DestLanguage destLanguage){
        this.srcLanguage = srcLanguage;
        this.destLanguage = destLanguage;
    }

    public SrcLanguage getSrcLanguage() {
        return srcLanguage;
    }

    public DestLanguage getDestLanguage() {
        return destLanguage;
    }

    public JSONObject getTransParams(AIUIConfig settings){
        try{
            JSONObject iatParams = new JSONObject();
            iatParams.put("language", srcLanguage.getSource());
            iatParams.put("accent", srcLanguage.getAccent());
            iatParams.put("domain", srcLanguage.getDomain());
            iatParams.put("isFar", srcLanguage.isFar()? "1" : "0");

            //新的翻译参数
            JSONObject trsParams = new JSONObject();
            trsParams.put("from", this.srcLanguage.getLanguage());
            trsParams.put("to", this.destLanguage.getLanguage());

            JSONObject attachParams = new JSONObject();
            attachParams.put("iat_params", iatParams.toString());
            attachParams.put("trs_params", trsParams.toString());

            JSONObject global = new JSONObject();
            global.put("scene", settings.translationScene());
            JSONObject transParams = new JSONObject();

            transParams.put("attachparams", attachParams);
            transParams.put("global", global);

            return transParams;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }


    public JSONObject getRestoreParams(AIUIConfig settings){
        try{
            JSONObject iatParams = new JSONObject();
            iatParams.put("language", SrcLanguage.CN_MANDARIN_NEAR.getSource());
            iatParams.put("accent", settings.accent());
            iatParams.put("domain", SrcLanguage.CN_MANDARIN_NEAR.getDomain());
            iatParams.put("isFar", SrcLanguage.CN_MANDARIN_NEAR.isFar()? "1" : "0");

            JSONObject attachParams = new JSONObject();
            attachParams.put("iat_params", iatParams);
            attachParams.put("trs_params", new JSONObject().toString());

            JSONObject global = new JSONObject();
            global.put("scene", settings.scene());

            JSONObject emptyParams = new JSONObject();
            emptyParams.put("attachparams", attachParams);
            emptyParams.put("global", global);

            return emptyParams;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static TranslationMode getDefaultTranslationMode(){
        return defaultTransParams;
    }

}
