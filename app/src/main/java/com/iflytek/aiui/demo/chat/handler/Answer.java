package com.iflytek.aiui.demo.chat.handler;

/**
 * 技能处理返回结果，包含展示内容，合成内容和回调
 */
public class Answer {
    private String mAnswer;
    private String mTTSContent;
    private Runnable mAnswerCallback;

    public Answer(String answer, String ttsContent, Runnable answerCallback) {
        mAnswer = answer;
        mTTSContent = ttsContent;
        mAnswerCallback = answerCallback;
    }


    public Answer(String answer) {
        this(answer, answer, null);
    }

    public Answer(String answer, String ttsContent) {
        this(answer, ttsContent, null);
    }

    public Answer(String answer, Runnable answerCallback) {
        this(answer, answer, answerCallback);
    }

    public String getAnswer() {
        return mAnswer;
    }

    public String getTTSContent() { return mTTSContent; }

    public Runnable getAnswerCallback() {
        return mAnswerCallback;
    }
}
