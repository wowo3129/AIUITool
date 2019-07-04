package com.iflytek.aiui.demo.chat.repository.translation;

public enum SrcLanguage {
    CN_MANDARIN_NEAR("普通话", "zh-cn", "cn", "mandarin", false),
    CN_LMZ_NEAR("四川话", "zh-cn", "cn", "lmz", false),
    CN_CANTONESE_NEAR("粤语", "zh-cn", "cn", "cantonese", false),
    EN_MANDARIN_NEAR("英文", "en-us", "en", "mandarin", false);

    private String description;
    private String language;
    private String source;
    private String accent;

    private String domain = "sms";
    private Boolean isFar;

    SrcLanguage(String description, String source, String language, String accent, boolean isFar) {
        this.description = description;
        this.source = source;
        this.language = language;
        this.accent = accent;
        this.isFar = isFar;
    }

    public String getSource() {
        return source;
    }

    public String getLanguage() {
        return language;
    }

    public String getAccent() {
        return accent;
    }

    public Boolean isFar() {
        return isFar;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return description;
    }
}
