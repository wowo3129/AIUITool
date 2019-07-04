package com.iflytek.aiui.demo.chat.common;


public class Constant {

    public static final int STATE_WAITING_WAKEUP = 0;
    public static final int STATE_WAKEUP = 1;
    public static final int STATE_TEXT = 2;
    public static final int STATE_VOICE = 3;
    public static final int STATE_VOICE_INPUTTING = 4;

    public static final String AIUI_LOG_PATH = "/sdcard/msc/aiui.log";

    public static final String SERVICE_HELLO = "fake.hello";
    public static final String SERVICE_SPEAKABLE = "fake.speakable";
    public static final String SERVICE_DYNAMIC = "fake.dynamic";
    public static final String SERVICE_DYNAMIC_QUERY = "fake.dynamic_query";
    public static final String SERVICE_FAKE_LOC = "fake.loc";
    public static final String SERVICE_UNKNOWN = "fake.unknown";
    public static final String SERVICE_NOTIFICATION = "fake.notification";
    public static final String SERVICE_TRANS = "fake.trans";
    public static final String SERVICE_ERROR = "fake.error";
    public static final String SERVICE_CONTACTS_UPLOAD = "fake.contacts_upload";


    public static final String KEY_APPID = "appid";
    public static final String KEY_APP_KEY = "key";
    public static final String KEY_SCENE = "scene";
    public static final String KEY_ACCENT = "aiui_accent";

    public static final String KEY_AIUI_WAKEUP = "aiui_wakeup";
    public static final String KEY_AIUI_TTS = "aiui_tts";
    public static final String KEY_AIUI_LOG = "aiui_log";

    public static final String KEY_AIUI_TRANSLATION = "aiui_translation";
    public static final String KEY_TRANS_SCENE = "trans_scene";

    public static final String KEY_AIUI_EOS = "aiui_eos";
    public static final String KEY_AIUI_DEBUG_LOG = "aiui_debug_log";
    public static final String KEY_AIUI_SAVE_DATALOG = "aiui_save_datalog";

    public static final String CONFIG_INIT_FLAG = "config_init_flag";
    public static final String KEY_CONFIG_LAST_ASSET_CONFIG = "last_asset_config";
}
