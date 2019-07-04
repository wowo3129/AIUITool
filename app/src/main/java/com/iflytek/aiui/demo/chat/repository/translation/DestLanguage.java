package com.iflytek.aiui.demo.chat.repository.translation;

public enum DestLanguage {
    EN("英文", "en"),
    CN("中文", "cn");
    //翻译服务暂未支持
//    JA("日语", "ja"),
//    KO("韩语", "ko"),
//    FR("法语", "fr"),
//    ES("西班牙语", "es"),
//    RU("俄语", "ru");

    private String description;
    private String language;

    DestLanguage(String description, String language) {
        this.description = description;
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return description;
    }
}
